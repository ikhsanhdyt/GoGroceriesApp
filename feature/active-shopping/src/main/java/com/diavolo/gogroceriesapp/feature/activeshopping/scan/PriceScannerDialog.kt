package com.diavolo.gogroceriesapp.feature.activeshopping.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.diavolo.gogroceriesapp.core.util.PriceCandidate
import com.diavolo.gogroceriesapp.core.util.parseRupiahPrices
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// How long a price stays in the chip row after it was last seen, so the chips don't flicker
// every time the camera moves.
private const val RECENT_PRICE_WINDOW_MS = 1_500L
private const val MAX_PRICE_CHIPS = 4

/** Whether the device has any camera, so callers can hide the scan entry point. */
@Composable
internal fun rememberHasCamera(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}

/**
 * Full-screen camera that reads rupiah prices from a price tag with on-device OCR.
 *
 * Detected prices are outlined on the preview and listed as chips; tapping one calls
 * [onPriceSelected]. Nothing is saved here, so the caller should let the user confirm the value.
 * Asks for the camera permission itself and offers a way out when it is denied.
 *
 * @param formatPrice formats an amount for display, so the scanner matches the rest of the app.
 */
@Composable
internal fun PriceScannerDialog(
    onPriceSelected: (rupiah: Long) -> Unit,
    onDismiss: () -> Unit,
    formatPrice: (rupiah: Long) -> String
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    // The user may grant the permission from system settings and come back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!hasPermission && context.hasCameraPermission()) {
            hasPermission = true
            permissionDenied = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        // Edge to edge: the status and navigation bars are padded inside the content.
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.coverWholeScreen() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when {
                hasPermission -> PriceScannerContent(
                    onPriceSelected = onPriceSelected,
                    formatPrice = formatPrice
                )
                permissionDenied -> CameraPermissionContent(
                    onOpenSettings = { context.openAppSettings() },
                    onEnterManually = onDismiss
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close scanner")
            }
        }
    }
}

private data class DetectedPrice(
    val candidate: PriceCandidate,
    val bounds: Rect
)

@Composable
private fun PriceScannerContent(
    onPriceSelected: (Long) -> Unit,
    formatPrice: (Long) -> String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var detections by remember { mutableStateOf(emptyList<DetectedPrice>()) }
    // Insertion-ordered so chips keep their position while the prices stay in view.
    var recentPrices by remember { mutableStateOf(LinkedHashMap<Long, RecentPrice>()) }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val executor = ContextCompat.getMainExecutor(context)
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(
                listOf(recognizer),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor
            ) { result ->
                val text = result.getValue(recognizer) ?: return@MlKitAnalyzer
                detections = text.toDetectedPrices()
                recentPrices = recentPrices.updatedWith(detections, SystemClock.elapsedRealtime())
            }
        )
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
            controller.clearImageAnalysisAnalyzer()
            recognizer.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { viewContext ->
                PreviewView(viewContext).apply {
                    // TextureView-backed: a SurfaceView punches through the dialog window and
                    // shows the screen behind it wherever the overlay is translucent.
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        detections.forEach { detection ->
            DetectedPriceBox(
                detection = detection,
                label = formatPrice(detection.candidate.rupiah),
                onClick = { onPriceSelected(detection.candidate.rupiah) }
            )
        }

        ScannerBottomPanel(
            prices = recentPrices.values
                .sortedByDescending { it.candidate.hasCurrencyPrefix }
                .take(MAX_PRICE_CHIPS)
                .map(RecentPrice::candidate),
            formatPrice = formatPrice,
            onPriceSelected = onPriceSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DetectedPriceBox(
    detection: DetectedPrice,
    label: String,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val highlight = if (detection.candidate.hasCurrencyPrefix) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.White
    }
    val bounds = detection.bounds

    Column(
        modifier = Modifier
            .offset { IntOffset(bounds.left, bounds.top) }
            .clickable(onClickLabel = "Use $label", onClick = onClick)
    ) {
        Box(
            modifier = with(density) {
                Modifier.size(bounds.width().toDp(), bounds.height().toDp())
            }.border(BorderStroke(2.dp, highlight), RoundedCornerShape(4.dp))
        )
        Surface(
            color = highlight,
            contentColor = if (detection.candidate.hasCurrencyPrefix) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                Color.Black
            },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ScannerBottomPanel(
    prices: List<PriceCandidate>,
    formatPrice: (Long) -> String,
    onPriceSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.7f),
        contentColor = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (prices.isEmpty()) {
                    "Point the camera at the price tag."
                } else {
                    "Tap the correct price."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (prices.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prices.forEach { price ->
                        AssistChip(
                            onClick = { onPriceSelected(price.rupiah) },
                            label = { Text(formatPrice(price.rupiah)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (price.hasCurrencyPrefix) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White
                                },
                                labelColor = if (price.hasCurrencyPrefix) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    Color.Black
                                }
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onOpenSettings: () -> Unit,
    onEnterManually: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Allow camera access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Camera access is needed to scan prices. You can still type the price yourself.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onOpenSettings) {
            Text("Open settings")
        }
        TextButton(onClick = onEnterManually) {
            Text("Enter manually")
        }
    }
}

private data class RecentPrice(
    val candidate: PriceCandidate,
    val lastSeenAt: Long
)

private fun LinkedHashMap<Long, RecentPrice>.updatedWith(
    detections: List<DetectedPrice>,
    now: Long
): LinkedHashMap<Long, RecentPrice> {
    val updated = LinkedHashMap<Long, RecentPrice>()
    forEach { (rupiah, recent) ->
        if (now - recent.lastSeenAt <= RECENT_PRICE_WINDOW_MS) updated[rupiah] = recent
    }
    detections.forEach { detection ->
        val rupiah = detection.candidate.rupiah
        val hadPrefix = updated[rupiah]?.candidate?.hasCurrencyPrefix == true
        updated[rupiah] = RecentPrice(
            candidate = detection.candidate.copy(
                hasCurrencyPrefix = hadPrefix || detection.candidate.hasCurrencyPrefix
            ),
            lastSeenAt = now
        )
    }
    return updated
}

/**
 * Parses each OCR line on its own so every price gets its own box. OCR often puts "Rp" on a
 * separate line from the digits, so a block that mentions "Rp" marks all of its prices.
 */
private fun Text.toDetectedPrices(): List<DetectedPrice> = textBlocks.flatMap { block ->
    val blockHasRp = block.text.contains("rp", ignoreCase = true)
    block.lines.flatMap { line ->
        val bounds = line.boundingBox ?: return@flatMap emptyList()
        parseRupiahPrices(line.text).map { candidate ->
            DetectedPrice(
                candidate = candidate.copy(
                    hasCurrencyPrefix = candidate.hasCurrencyPrefix || blockHasRp
                ),
                bounds = bounds
            )
        }
    }
}

/**
 * A full-size dialog window is still positioned below the status bar by default, which pushes
 * its bottom off screen. Let it cover the system bars; the content pads for them itself.
 */
private fun Window.coverWholeScreen() {
    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        attributes = attributes.apply { setFitInsetsTypes(0) }
    } else {
        addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
