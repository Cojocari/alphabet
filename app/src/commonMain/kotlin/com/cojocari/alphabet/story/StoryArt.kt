package com.cojocari.alphabet.story

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate

/**
 * The magpie (coțofana), drawn in code like Hupi: black and white, with a long tail and
 * something shiny in her beak. She faces right; [flap] (0–1) moves the wing.
 */
@Composable
fun Magpie(flap: Float, modifier: Modifier = Modifier, shiny: Boolean = true) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun p(x: Float, y: Float) = Offset(w * x, h * y)
        val black = Color(0xFF1A1A1A)
        // Long tail with a blue-green shine.
        val tail = Path().apply {
            moveTo(w * 0.36f, h * 0.52f)
            lineTo(w * 0.02f, h * 0.40f)
            lineTo(w * 0.04f, h * 0.52f)
            lineTo(w * 0.38f, h * 0.62f)
            close()
        }
        drawPath(tail, Color(0xFF1B3B4B))
        drawPath(tail, Color(0xFF2E86AB), style = Stroke(w * 0.012f))
        // Body and white belly.
        drawOval(black, topLeft = p(0.30f, 0.40f), size = Size(w * 0.44f, h * 0.30f))
        drawOval(Color.White, topLeft = p(0.40f, 0.53f), size = Size(w * 0.28f, h * 0.15f))
        // Wing, flapping around its root on the back.
        rotate(-35f + flap * 55f, pivot = p(0.52f, 0.47f)) {
            drawOval(Color(0xFF22313F), topLeft = p(0.26f, 0.38f), size = Size(w * 0.30f, h * 0.15f))
            drawOval(Color.White, topLeft = p(0.40f, 0.41f), size = Size(w * 0.13f, h * 0.08f))
            drawOval(Color(0xFF2E86AB), topLeft = p(0.26f, 0.38f), size = Size(w * 0.30f, h * 0.15f), style = Stroke(w * 0.01f))
        }
        // Head, eye and beak.
        drawCircle(black, radius = w * 0.13f, center = p(0.74f, 0.40f))
        drawCircle(Color.White, radius = w * 0.04f, center = p(0.78f, 0.37f))
        drawCircle(black, radius = w * 0.02f, center = p(0.79f, 0.37f))
        val beak = Path().apply {
            moveTo(w * 0.84f, h * 0.38f)
            lineTo(w * 0.98f, h * 0.43f)
            lineTo(w * 0.84f, h * 0.46f)
            close()
        }
        drawPath(beak, Color(0xFF4A4A4A))
        // Legs.
        drawLine(Color(0xFF4A4A4A), p(0.50f, 0.69f), p(0.47f, 0.80f), strokeWidth = w * 0.018f)
        drawLine(Color(0xFF4A4A4A), p(0.58f, 0.69f), p(0.60f, 0.80f), strokeWidth = w * 0.018f)
        if (shiny) {
            drawCircle(Color(0xFFFFD60A), radius = w * 0.05f, center = p(0.95f, 0.47f))
            drawCircle(Color.White, radius = w * 0.015f, center = p(0.94f, 0.455f))
        }
    }
}

/** A wooden treasure chest with gold bands; [open] (0–1) lifts the lid and lets the light out. */
@Composable
fun TreasureChest(open: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val wood = Color(0xFF8D5524)
        val dark = Color(0xFF5E3512)
        val gold = Color(0xFFFFB703)
        // Light shining out of the open chest.
        if (open > 0f) {
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFFFFF3B0).copy(alpha = 0.9f * open), Color.Transparent), center = Offset(w * 0.5f, h * 0.42f), radius = w * 0.55f),
                radius = w * 0.55f,
                center = Offset(w * 0.5f, h * 0.42f),
            )
        }
        // Base.
        drawRoundRect(wood, topLeft = Offset(w * 0.12f, h * 0.47f), size = Size(w * 0.76f, h * 0.43f), cornerRadius = CornerRadius(w * 0.04f))
        for (y in listOf(0.62f, 0.76f)) drawLine(dark, Offset(w * 0.13f, h * y), Offset(w * 0.87f, h * y), strokeWidth = w * 0.012f)
        for (x in listOf(0.2f, 0.74f)) drawRect(gold, topLeft = Offset(w * x, h * 0.47f), size = Size(w * 0.06f, h * 0.43f))
        // Lid, tilting back around its hinge as it opens.
        rotate(-28f * open, pivot = Offset(w * 0.12f, h * 0.47f)) {
            translate(top = -h * 0.08f * open) {
                drawRoundRect(wood, topLeft = Offset(w * 0.1f, h * 0.26f), size = Size(w * 0.8f, h * 0.22f), cornerRadius = CornerRadius(w * 0.12f, w * 0.12f))
                drawLine(dark, Offset(w * 0.11f, h * 0.46f), Offset(w * 0.89f, h * 0.46f), strokeWidth = w * 0.014f)
                for (x in listOf(0.2f, 0.74f)) drawRect(gold, topLeft = Offset(w * x, h * 0.27f), size = Size(w * 0.06f, h * 0.2f))
            }
        }
        // Lock.
        if (open < 0.5f) {
            drawRoundRect(gold, topLeft = Offset(w * 0.44f, h * 0.43f), size = Size(w * 0.12f, h * 0.13f), cornerRadius = CornerRadius(w * 0.02f))
            drawCircle(dark, radius = w * 0.018f, center = Offset(w * 0.5f, h * 0.49f))
        }
    }
}
