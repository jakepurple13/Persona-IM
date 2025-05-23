package codes.chrishorner.personasns

import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur.NORMAL
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/**
 * Draws a black line from `entry` to `entry2` (if it exists).
 */
fun Modifier.drawConnectingLine(entry1: Entry, entry2: Entry?): Modifier {
    if (entry2 == null) return this

    return composed {
        val animatedEntry1Left by animateOffsetAsState(entry1.lineCoordinates.leftPoint)
        val animatedEntry1Right by animateOffsetAsState(entry1.lineCoordinates.rightPoint)
        val animatedEntry2Left by animateOffsetAsState(entry2.lineCoordinates.leftPoint)
        val animatedEntry2Right by animateOffsetAsState(entry2.lineCoordinates.rightPoint)

        drawWithCache {
            val linePath = Path()
            val topOffset = TranscriptSizes.getTopDrawingOffset(this, entry1)
            val topLeft = animatedEntry1Left + topOffset
            val topRight = animatedEntry1Right + topOffset

            val bottomOffset = TranscriptSizes.getBottomDrawingOffset(this, entry2)
            val bottomLeft = animatedEntry2Left + bottomOffset
            val bottomRight = animatedEntry2Right + bottomOffset

            val shadowPaint = Paint().apply {
                color = Color.Black
                alpha = 0.5f
                asFrameworkPaint().maskFilter = BlurMaskFilter(4.dp.toPx(), NORMAL)
            }

            onDrawBehind {
                val currentBottomLeft =
                    lerp(topLeft, bottomLeft, fraction = entry1.lineProgress.value)
                val currentBottomRight =
                    lerp(topRight, bottomRight, fraction = entry1.lineProgress.value)

                with(linePath) {
                    rewind()
                    moveTo(topLeft.x, topLeft.y)
                    lineTo(topRight.x, topRight.y)
                    lineTo(currentBottomRight.x, currentBottomRight.y)
                    lineTo(currentBottomLeft.x, currentBottomLeft.y)
                    close()
                }

                translate(top = 16.dp.toPx()) {
                    drawIntoCanvas {
                        it.drawPath(linePath, shadowPaint)
                    }
                }

                drawPath(linePath, Color.Black)
            }
        }
    }
}