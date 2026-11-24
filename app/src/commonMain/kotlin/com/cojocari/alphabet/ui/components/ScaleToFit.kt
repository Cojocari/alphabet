package com.cojocari.alphabet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Lays [content] out at its natural size and, if that is wider than the space available (a phone
 * instead of a tablet), shrinks it evenly until it fits, so nothing ends up off the screen.
 */
@Composable
fun ScaleToFit(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val placeable = measurables.first().measure(
            Constraints(maxHeight = constraints.maxHeight)
        )
        val scale = if (constraints.hasBoundedWidth && placeable.width > constraints.maxWidth) {
            constraints.maxWidth.toFloat() / placeable.width
        } else 1f
        val width = (placeable.width * scale).roundToInt().coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeable.height * scale).roundToInt().coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            // Scaled around its centre, so the content stays centred in the smaller box.
            placeable.placeWithLayer((width - placeable.width) / 2, (height - placeable.height) / 2) {
                scaleX = scale
                scaleY = scale
            }
        }
    }
}
