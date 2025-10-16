package com.achmadichzan.deteksibenda.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.deteksibenda.components.BoundingBox
import kotlin.collections.forEach

@Composable
fun BoundingBoxOverlay(boundingBoxes: List<BoundingBox>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val imageAspectRatio = 3f / 4f
        val canvasAspectRatio = size.width / size.height

        val renderedImageWidth: Float
        val renderedImageHeight: Float
        val offsetX: Float
        val offsetY: Float

        if (canvasAspectRatio > imageAspectRatio) {
            renderedImageHeight = size.height
            renderedImageWidth = size.height * imageAspectRatio
            offsetX = (size.width - renderedImageWidth) / 2f
            offsetY = 0f
        } else {
            renderedImageWidth = size.width
            renderedImageHeight = size.width / imageAspectRatio
            offsetX = 0f
            offsetY = (size.height - renderedImageHeight) / 2f
        }

        drawRect(
            color = Color.Yellow,
            topLeft = Offset(offsetX, offsetY),
            size = Size(renderedImageWidth, renderedImageHeight),
            style = Stroke(width = 4.dp.toPx())
        )

        boundingBoxes.forEach { box ->
            val left = box.x1 * renderedImageWidth + offsetX
            val top = box.y1 * renderedImageHeight + offsetY
            val right = box.x2 * renderedImageWidth + offsetX
            val bottom = box.y2 * renderedImageHeight + offsetY

            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 2.dp.toPx())
            )

            val text = "${box.clsName} ${"%.1f".format(box.cnf * 100)}%"
            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = TextStyle(color = Color.White, fontSize = 16.sp)
            )
            val textTopLeft = Offset(left, top - textLayoutResult.size.height - 4.dp.toPx())
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = textTopLeft,
                size = Size(textLayoutResult.size.width + 8.dp.toPx(), textLayoutResult.size.height + 4.dp.toPx())
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textTopLeft.copy(x = textTopLeft.x + 4.dp.toPx())
            )
        }
    }
}