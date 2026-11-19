package com.cojocari.alphabet.ui.draw

import androidx.compose.ui.graphics.Path

/**
 * Character outlines in the system's bold sans-serif font, used to draw the big letter to
 * trace and to place the stroke guides on it. Size is in pixels; the baseline is at y = 0.
 */
interface GlyphFont {
    /** The outline of [text], drawn starting at x = [x]. */
    fun path(text: String, x: Float): Path

    /** How far [text] moves the pen (its width when typed). */
    fun advance(text: String): Float
}

/** The tracing font at [size] pixels (the system sans-serif, bold). */
expect fun glyphFont(size: Float): GlyphFont

/** A test of whether a pixel of a [width] × [height] canvas lies inside [path]. */
expect fun insideTester(path: Path, width: Int, height: Int): (x: Int, y: Int) -> Boolean
