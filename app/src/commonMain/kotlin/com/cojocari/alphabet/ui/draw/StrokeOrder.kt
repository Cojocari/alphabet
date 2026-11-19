package com.cojocari.alphabet.ui.draw

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * How to write each letter and digit: numbered strokes, in order, each a list of points.
 * Coordinates are relative to the character's own outline: (0,0) is its top-left corner and
 * (1,1) its bottom-right, so the arrows follow the letter at any size and in any font.
 *
 * Letters with Romanian diacritics reuse their base letter ([StrokeSpec.base]) and add the
 * mark's stroke, laid out over the mark's own outline.
 */
internal class StrokeSpec(
    val strokes: List<List<Offset>>,
    val base: Char? = null,
    val mark: Mark? = null,
)

internal enum class Mark(val above: Boolean, val stroke: List<Offset>) {
    Breve(true, s { arc(0.5, 0.1, 0.38, 0.75, 180.0, -180.0) }),
    Circumflex(true, s { to(0.12, 0.9); to(0.5, 0.1); to(0.88, 0.9) }),
    CommaBelow(false, s { to(0.55, 0.1); to(0.42, 0.9) }),
}

/** Builds one stroke from straight segments and elliptical arcs. */
internal class StrokeBuilder {
    val points = mutableListOf<Offset>()

    fun to(x: Double, y: Double) {
        points += Offset(x.toFloat(), y.toFloat())
    }

    /**
     * Arc of the ellipse centred at ([cx], [cy]) with radii [rx], [ry]. Angles in degrees on
     * screen: 0 = right, 90 = down, 180 = left, -90 = up; a positive [sweep] turns clockwise.
     */
    fun arc(cx: Double, cy: Double, rx: Double, ry: Double, from: Double, sweep: Double) {
        val steps = ceil(abs(sweep) / 6).toInt().coerceAtLeast(2)
        for (k in 0..steps) {
            val a = (from + sweep * k / steps) * PI / 180
            to(cx + rx * cos(a), cy + ry * sin(a))
        }
    }
}

internal fun s(block: StrokeBuilder.() -> Unit): List<Offset> = StrokeBuilder().apply(block).points

private fun line(vararg xy: Double): List<Offset> = s { for (i in xy.indices step 2) to(xy[i], xy[i + 1]) }

/** A dot (the i and j tittle): drawn as a numbered tap point. */
private fun dot(x: Double, y: Double): List<Offset> = s { to(x, y) }

private fun spec(vararg strokes: List<Offset>) = StrokeSpec(strokes.toList())

internal fun strokeSpec(c: Char): StrokeSpec? = Specs[c]

private val Specs: Map<Char, StrokeSpec> = buildMap {
    // ---------- Capitals ----------
    put('A', spec(line(0.5, 0.05, 0.1, 0.95), line(0.5, 0.05, 0.9, 0.95), line(0.28, 0.66, 0.72, 0.66)))
    put(
        'B', spec(
            line(0.15, 0.06, 0.15, 0.94),
            s { to(0.15, 0.07); to(0.55, 0.07); arc(0.55, 0.27, 0.3, 0.2, -90.0, 180.0); to(0.15, 0.47) },
            s { to(0.15, 0.48); to(0.6, 0.48); arc(0.6, 0.705, 0.3, 0.225, -90.0, 180.0); to(0.15, 0.93) },
        )
    )
    put('C', spec(s { arc(0.55, 0.5, 0.43, 0.44, -40.0, -280.0) }))
    put(
        'D', spec(
            line(0.15, 0.06, 0.15, 0.94),
            s { to(0.15, 0.07); to(0.45, 0.07); arc(0.45, 0.5, 0.42, 0.43, -90.0, 180.0); to(0.15, 0.93) },
        )
    )
    put('E', spec(line(0.15, 0.06, 0.15, 0.94), line(0.15, 0.07, 0.88, 0.07), line(0.15, 0.5, 0.8, 0.5), line(0.15, 0.93, 0.9, 0.93)))
    put('F', spec(line(0.15, 0.06, 0.15, 0.94), line(0.15, 0.07, 0.88, 0.07), line(0.15, 0.5, 0.8, 0.5)))
    put('G', spec(s { arc(0.55, 0.5, 0.43, 0.44, -40.0, -270.0); to(0.88, 0.55) }, line(0.55, 0.55, 0.88, 0.55)))
    put('H', spec(line(0.13, 0.06, 0.13, 0.94), line(0.87, 0.06, 0.87, 0.94), line(0.13, 0.5, 0.87, 0.5)))
    put('I', spec(line(0.5, 0.05, 0.5, 0.95)))
    put('J', spec(s { to(0.8, 0.05); to(0.8, 0.65); arc(0.45, 0.65, 0.35, 0.3, 0.0, 180.0) }))
    put('K', spec(line(0.15, 0.06, 0.15, 0.94), line(0.88, 0.06, 0.18, 0.6), line(0.38, 0.45, 0.9, 0.94)))
    put('L', spec(line(0.15, 0.06, 0.15, 0.93, 0.9, 0.93)))
    put('M', spec(line(0.1, 0.06, 0.1, 0.94), line(0.1, 0.06, 0.5, 0.7, 0.9, 0.06, 0.9, 0.94)))
    put('N', spec(line(0.13, 0.06, 0.13, 0.94), line(0.13, 0.06, 0.87, 0.94, 0.87, 0.06)))
    put('O', spec(s { arc(0.5, 0.5, 0.42, 0.44, -90.0, -360.0) }))
    put(
        'P', spec(
            line(0.15, 0.06, 0.15, 0.94),
            s { to(0.15, 0.07); to(0.55, 0.07); arc(0.55, 0.3, 0.33, 0.23, -90.0, 180.0); to(0.15, 0.53) },
        )
    )
    put('Q', spec(s { arc(0.5, 0.48, 0.42, 0.42, -90.0, -360.0) }, line(0.58, 0.72, 0.92, 0.97)))
    put(
        'R', spec(
            line(0.15, 0.06, 0.15, 0.94),
            s { to(0.15, 0.07); to(0.55, 0.07); arc(0.55, 0.3, 0.33, 0.23, -90.0, 180.0); to(0.15, 0.53) },
            line(0.45, 0.53, 0.88, 0.94),
        )
    )
    put('S', spec(s { arc(0.5, 0.27, 0.38, 0.22, -20.0, -250.0); arc(0.5, 0.72, 0.4, 0.23, -90.0, 250.0) }))
    put('T', spec(line(0.5, 0.07, 0.5, 0.95), line(0.06, 0.07, 0.94, 0.07)))
    put('U', spec(s { to(0.13, 0.06); to(0.13, 0.62); arc(0.5, 0.62, 0.37, 0.32, 180.0, -180.0); to(0.87, 0.06) }))
    put('V', spec(line(0.07, 0.06, 0.5, 0.94, 0.93, 0.06)))
    put('W', spec(line(0.05, 0.06, 0.27, 0.94, 0.5, 0.3, 0.73, 0.94, 0.95, 0.06)))
    put('X', spec(line(0.1, 0.06, 0.9, 0.94), line(0.9, 0.06, 0.1, 0.94)))
    put('Y', spec(line(0.08, 0.06, 0.5, 0.5), line(0.92, 0.06, 0.5, 0.5, 0.5, 0.94)))
    put('Z', spec(line(0.1, 0.07, 0.88, 0.07, 0.1, 0.93, 0.9, 0.93)))
    put('Ă', StrokeSpec(emptyList(), base = 'A', mark = Mark.Breve))
    put('Â', StrokeSpec(emptyList(), base = 'A', mark = Mark.Circumflex))
    put('Î', StrokeSpec(emptyList(), base = 'I', mark = Mark.Circumflex))
    put('Ș', StrokeSpec(emptyList(), base = 'S', mark = Mark.CommaBelow))
    put('Ț', StrokeSpec(emptyList(), base = 'T', mark = Mark.CommaBelow))

    // ---------- Small letters (each relative to its own outline, ascenders included) ----------
    put(
        'a', spec(
            s { arc(0.5, 0.3, 0.36, 0.24, 200.0, 160.0); to(0.86, 0.95) },
            s { to(0.84, 0.5); to(0.45, 0.5); arc(0.45, 0.72, 0.33, 0.22, -90.0, -180.0); to(0.84, 0.85) },
        )
    )
    put('b', spec(line(0.13, 0.03, 0.13, 0.97), s { arc(0.55, 0.64, 0.36, 0.3, 200.0, 340.0) }))
    put('c', spec(s { arc(0.55, 0.5, 0.42, 0.43, -40.0, -280.0) }))
    put('d', spec(s { arc(0.45, 0.64, 0.36, 0.3, -30.0, -300.0) }, line(0.87, 0.03, 0.87, 0.97)))
    put('e', spec(s { to(0.12, 0.52); to(0.88, 0.52); arc(0.5, 0.52, 0.38, 0.42, 0.0, -320.0) }))
    put('f', spec(s { arc(0.6, 0.2, 0.28, 0.17, -20.0, -160.0); to(0.32, 0.97) }, line(0.05, 0.38, 0.85, 0.38)))
    put('g', spec(s { arc(0.45, 0.33, 0.35, 0.27, -30.0, -300.0) }, s { to(0.86, 0.03); to(0.86, 0.75); arc(0.5, 0.75, 0.36, 0.2, 0.0, 150.0) }))
    put('h', spec(line(0.13, 0.03, 0.13, 0.97), s { to(0.13, 0.55); arc(0.5, 0.52, 0.37, 0.22, 180.0, 180.0); to(0.87, 0.97) }))
    put('i', spec(line(0.5, 0.33, 0.5, 0.97), dot(0.5, 0.08)))
    put('j', spec(s { to(0.62, 0.3); to(0.62, 0.75); arc(0.27, 0.75, 0.35, 0.2, 0.0, 150.0) }, dot(0.62, 0.07)))
    put('k', spec(line(0.13, 0.03, 0.13, 0.97), line(0.85, 0.3, 0.15, 0.72), line(0.42, 0.58, 0.88, 0.97)))
    put('l', spec(line(0.5, 0.03, 0.5, 0.97)))
    put(
        'm', spec(
            line(0.08, 0.05, 0.08, 0.97),
            s { to(0.08, 0.35); arc(0.29, 0.35, 0.21, 0.29, 180.0, 180.0); to(0.5, 0.97) },
            s { to(0.5, 0.35); arc(0.71, 0.35, 0.21, 0.29, 180.0, 180.0); to(0.92, 0.97) },
        )
    )
    put('n', spec(line(0.12, 0.05, 0.12, 0.97), s { to(0.12, 0.4); arc(0.5, 0.4, 0.38, 0.34, 180.0, 180.0); to(0.88, 0.97) }))
    put('o', spec(s { arc(0.5, 0.5, 0.4, 0.42, -90.0, -360.0) }))
    put('p', spec(line(0.13, 0.03, 0.13, 0.98), s { arc(0.55, 0.36, 0.36, 0.3, 200.0, 340.0) }))
    put('q', spec(s { arc(0.45, 0.36, 0.36, 0.3, -30.0, -300.0) }, line(0.87, 0.03, 0.87, 0.98)))
    put('r', spec(line(0.15, 0.05, 0.15, 0.97), s { to(0.15, 0.45); arc(0.6, 0.4, 0.45, 0.32, 180.0, 120.0) }))
    put('s', spec(s { arc(0.5, 0.27, 0.38, 0.22, -20.0, -250.0); arc(0.5, 0.72, 0.4, 0.23, -90.0, 250.0) }))
    put('t', spec(s { to(0.4, 0.03); to(0.4, 0.8); arc(0.65, 0.8, 0.25, 0.16, 180.0, -140.0) }, line(0.05, 0.3, 0.88, 0.3)))
    put('u', spec(s { to(0.12, 0.03); to(0.12, 0.6); arc(0.5, 0.6, 0.38, 0.36, 180.0, -180.0); to(0.88, 0.03) }, line(0.88, 0.03, 0.88, 0.97)))
    put('v', spec(line(0.07, 0.05, 0.5, 0.95, 0.93, 0.05)))
    put('w', spec(line(0.04, 0.05, 0.27, 0.95, 0.5, 0.3, 0.73, 0.95, 0.96, 0.05)))
    put('x', spec(line(0.1, 0.05, 0.9, 0.95), line(0.9, 0.05, 0.1, 0.95)))
    put('y', spec(line(0.08, 0.03, 0.48, 0.7), line(0.92, 0.03, 0.35, 0.97)))
    put('z', spec(line(0.1, 0.06, 0.88, 0.06, 0.1, 0.93, 0.9, 0.93)))
    put('ă', StrokeSpec(emptyList(), base = 'a', mark = Mark.Breve))
    put('â', StrokeSpec(emptyList(), base = 'a', mark = Mark.Circumflex))
    put('î', StrokeSpec(emptyList(), base = 'ı', mark = Mark.Circumflex))
    put('ș', StrokeSpec(emptyList(), base = 's', mark = Mark.CommaBelow))
    put('ț', StrokeSpec(emptyList(), base = 't', mark = Mark.CommaBelow))
    put('ı', spec(line(0.5, 0.05, 0.5, 0.97)))

    // ---------- Digits ----------
    put('0', spec(s { arc(0.5, 0.5, 0.4, 0.45, -90.0, -360.0) }))
    put('1', spec(line(0.15, 0.25, 0.7, 0.05, 0.7, 0.95)))
    put('2', spec(s { arc(0.5, 0.3, 0.38, 0.25, 195.0, 195.0); to(0.1, 0.93); to(0.92, 0.93) }))
    put('3', spec(s { arc(0.48, 0.27, 0.36, 0.22, -160.0, 250.0); arc(0.48, 0.72, 0.4, 0.23, -90.0, 250.0) }))
    put('4', spec(line(0.65, 0.05, 0.07, 0.7, 0.95, 0.7), line(0.7, 0.05, 0.7, 0.95)))
    put('5', spec(s { to(0.22, 0.07); to(0.17, 0.47); arc(0.5, 0.68, 0.38, 0.27, -150.0, 300.0) }, line(0.22, 0.07, 0.85, 0.07)))
    put('6', spec(s { arc(0.55, 0.55, 0.42, 0.5, -60.0, -120.0); arc(0.52, 0.68, 0.38, 0.27, 180.0, -360.0) }))
    put('7', spec(line(0.08, 0.07, 0.92, 0.07, 0.38, 0.95)))
    put(
        '8', spec(
            s {
                arc(0.5, 0.27, 0.34, 0.22, -20.0, -250.0)
                arc(0.5, 0.72, 0.4, 0.23, -90.0, 360.0)
                arc(0.5, 0.27, 0.34, 0.22, 90.0, -110.0)
            }
        )
    )
    put('9', spec(s { arc(0.48, 0.32, 0.37, 0.28, 0.0, -360.0); arc(0.4, 0.32, 0.45, 0.6, 0.0, 110.0) }))
}
