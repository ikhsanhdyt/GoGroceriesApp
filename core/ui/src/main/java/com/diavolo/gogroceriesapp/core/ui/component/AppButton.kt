package com.diavolo.gogroceriesapp.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.diavolo.gogroceriesapp.core.ui.Constant.ContentDescription.END_ICON
import com.diavolo.gogroceriesapp.core.ui.Constant.ContentDescription.START_ICON
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonSize
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonSize.Medium
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonType
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonType.Primary
import com.diavolo.gogroceriesapp.core.ui.extentions.orDefault
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp1
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp32
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp4
import com.diavolo.gogroceriesapp.core.ui.util.disableRipple
import com.diavolo.gogroceriesapp.core.ui.util.ignoreInteractiveComponentSize
import com.diavolo.gogroceriesapp.core.ui.util.rememberDebounceHandler
import com.diavolo.gogroceriesapp.core.util.Constants.EMPTY_STRING

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    text: String = EMPTY_STRING,
    iconTint: Color? = null,
    startIcon: ImageVector? = null,
    size: ButtonSize = Medium,
    endIcon: ImageVector? = null,
    type: ButtonType = Primary,
    textComposable: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val debounceHandler = rememberDebounceHandler()
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonShape = RoundedCornerShape(if (size is ButtonSize.Custom) size.borderRadius else Dp32)

    CompositionLocalProvider(
        ignoreInteractiveComponentSize(),
        disableRipple()
    ) {
        Button(
            onClick = { debounceHandler.processClick(onClick) },
            enabled = !disabled,
            contentPadding = size.paddingValues,
            interactionSource = buttonInteractionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(Dp1, type.outlineColor(disabled, isButtonPressed), buttonShape),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isButtonPressed -> type.pressedContainerColor
                    else -> type.containerColor
                },
                contentColor = when {
                    isButtonPressed -> type.pressedContentColor
                    else -> type.contentColor
                },
                disabledContainerColor = type.disabledContainerColor,
                disabledContentColor = type.disabledContentColor
            )
        ) {
            Row(
                horizontalArrangement = spacedBy(space = Dp4, alignment = CenterHorizontally),
                verticalAlignment = CenterVertically
            ) {
                startIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = START_ICON,
                        tint = iconTint.orDefault(LocalContentColor.current)                    )
                }
                textComposable?.invoke()
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        textAlign = Center,
                        overflow = Ellipsis
                    )
                }
                endIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = END_ICON,
                        modifier = Modifier
                            .requiredSize(size.iconSize)
                    )
                }
            }
        }
    }
}

private fun ButtonType.outlineColor(disabled: Boolean, pressed: Boolean) = when {
    disabled -> disabledOutlineColor
    pressed -> pressedOutlineColor
    else -> outlineColor
}