package com.diavolo.gogroceriesapp.core.ui.attr

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonSecondaryColor.PressedContainerColor
import com.diavolo.gogroceriesapp.core.ui.attr.AppButtonAttr.ButtonType.Primary
import com.diavolo.gogroceriesapp.core.ui.theme.Black
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp12
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp16
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp24
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp32
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp48
import com.diavolo.gogroceriesapp.core.ui.theme.Dimen.Dp8
import com.diavolo.gogroceriesapp.core.ui.theme.GraniteGray
import com.diavolo.gogroceriesapp.core.ui.theme.Gray
import com.diavolo.gogroceriesapp.core.ui.theme.Green300
import com.diavolo.gogroceriesapp.core.ui.theme.Green400
import com.diavolo.gogroceriesapp.core.ui.theme.SnowWhite
import com.diavolo.gogroceriesapp.core.util.Constants.EMPTY_STRING

object AppButtonAttr {

    object ButtonSecondaryColor {
        val PressedContainerColor = Color(0x0E0E0E08)
    }

    data class AppButtonArgs(
        val text: String = EMPTY_STRING,
        val modifier: Modifier = Modifier,
        val startIcon: ImageVector? = null,
        val endIcon: ImageVector? = null,
        val disabled: Boolean = false,
        val type: ButtonType = Primary,
        val onClick: () -> Unit
    )

    sealed class ButtonType(
        val containerColor: Color = Green300,
        val outlineColor: Color = Green300,
        val contentColor: Color = Black,
        val pressedContainerColor: Color = Green400,
        val pressedOutlineColor: Color = Green400,
        val pressedContentColor: Color = Black,
        val disabledContainerColor: Color = SnowWhite,
        val disabledOutlineColor: Color = SnowWhite,
        val disabledContentColor: Color = Gray
    ) {

        object Primary : ButtonType()

        object Secondary : ButtonType(
            containerColor = Transparent,
            outlineColor = GraniteGray,
            pressedContainerColor = PressedContainerColor,
            pressedOutlineColor = Black,
            disabledContainerColor = Transparent,
            disabledOutlineColor = Gray
        )

        data class Custom(
            val container: Color = Green300,
            val outline: Color = Green300,
            val content: Color = Black,
            val pressedContainer: Color = Green400,
            val pressedOutline: Color = Green400,
            val pressedContent: Color = Black,
            val disabledContainer: Color = SnowWhite,
            val disabledOutline: Color = SnowWhite,
            val disabledContent: Color = GraniteGray
        ) : ButtonType(
            containerColor = container,
            outlineColor = outline,
            contentColor = content,
            pressedContainerColor = pressedContainer,
            pressedOutlineColor = pressedOutline,
            pressedContentColor = pressedContent,
            disabledContainerColor = disabledContainer,
            disabledOutlineColor = disabledOutline,
            disabledContentColor = disabledContent
        )
    }

    sealed class ButtonSize(
        val paddingValues: PaddingValues = PaddingValues(Dp32, Dp12),
        val minHeight: Dp = Dp32,
        val iconSize: Dp = Dp24
    ) {
        object Medium : ButtonSize(
            minHeight = Dp48
        )

        object Small : ButtonSize(
            paddingValues = PaddingValues(Dp24, Dp8),
            minHeight = Dp32,
            iconSize = Dp16
        )

        data class Custom(
            val padding: PaddingValues = Medium.paddingValues,
            val height: Dp = Medium.iconSize,
            val borderRadius: Dp = Dp32,
            val textStyle: TextStyle? = null,
            val icSize: Dp = Medium.iconSize
        ) : ButtonSize(padding, height, icSize)
    }
}
