package com.cojocari.alphabet.ui.draw

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asSkiaPath
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface

/** Letters the tracing font must have (Romanian ones included). */
private const val MustHave = "AZaz09ĂăÂâÎîȘșȚț"

/** A bold sans-serif with every letter of both alphabets. */
private val tracingTypeface: Typeface by lazy {
    val fonts = FontMgr.default
    listOf("Helvetica Neue", "Helvetica", "Arial")
        .firstNotNullOfOrNull { family ->
            fonts.matchFamilyStyle(family, FontStyle.BOLD)
                ?.takeIf { tf -> MustHave.all { tf.getUTF32Glyph(it.code) != 0.toShort() } }
        }
        ?: fonts.legacyMakeTypeface("", FontStyle.BOLD)
        ?: error("No system font")
}

actual fun glyphFont(size: Float): GlyphFont = object : GlyphFont {
    private val font = Font(tracingTypeface, size)

    override fun path(text: String, x: Float): Path {
        val glyphs = font.getStringGlyphs(text)
        val widths = font.getWidths(glyphs)
        val out = org.jetbrains.skia.Path()
        var pen = x
        glyphs.forEachIndexed { i, g ->
            font.getPath(g)?.let { out.addPath(it, pen, 0f) }
            pen += widths[i]
        }
        return out.asComposePath()
    }

    override fun advance(text: String): Float = font.measureTextWidth(text)
}

actual fun insideTester(path: Path, width: Int, height: Int): (x: Int, y: Int) -> Boolean {
    val skia = path.asSkiaPath()
    return { x, y -> skia.contains(x.toFloat(), y.toFloat()) }
}
