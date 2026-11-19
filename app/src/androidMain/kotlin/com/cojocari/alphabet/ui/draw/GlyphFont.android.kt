package com.cojocari.alphabet.ui.draw

import android.graphics.Paint
import android.graphics.Region
import android.graphics.Typeface
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath

actual fun glyphFont(size: Float): GlyphFont = object : GlyphFont {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = size
    }

    override fun path(text: String, x: Float): Path =
        android.graphics.Path().also { paint.getTextPath(text, 0, text.length, x, 0f, it) }.asComposePath()

    override fun advance(text: String): Float = paint.measureText(text)
}

actual fun insideTester(path: Path, width: Int, height: Int): (x: Int, y: Int) -> Boolean {
    val region = Region().apply { setPath(path.asAndroidPath(), Region(0, 0, width, height)) }
    return { x, y -> region.contains(x, y) }
}
