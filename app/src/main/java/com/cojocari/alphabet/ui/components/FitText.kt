package com.cojocari.alphabet.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * One line of text that shrinks until it fits, instead of being cut off with "…"
 * (e.g. "Hupi's Adventure" on a narrow phone). Drawn only once the size fits, so it
 * never flickers.
 */
@Composable
fun FitText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current,
    minSize: TextUnit = 12.sp,
) {
    var size by remember(text, fontSize) { mutableStateOf(fontSize) }
    var ready by remember(text, fontSize) { mutableStateOf(false) }
    Text(
        text,
        modifier.drawWithContent { if (ready) drawContent() },
        color = color,
        fontSize = size,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        style = style,
        onTextLayout = { r ->
            if (r.didOverflowWidth && size.value > minSize.value) size = (size.value * 0.92f).sp else ready = true
        },
    )
}
