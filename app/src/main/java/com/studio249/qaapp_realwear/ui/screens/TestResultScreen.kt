package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.*
import org.json.JSONObject
import java.io.BufferedReader

// ─── Data Models ─────────────────────────────────────────────────────────────

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

data class Detection(
    val box: BoundingBox,
    val score: Float,
    val label: String
)

// ─── JSON Parser ─────────────────────────────────────────────────────────────

/**
 * Parses detections from a JSON string.
 * Expected structure:
 * {
 *   "detections": [
 *     { "box": { "x1": …, "y1": …, "x2": …, "y2": … }, "score": …, "label": … }
 *   ]
 * }
 */
fun parseDetections(jsonString: String): List<Detection> {
    val root = JSONObject(jsonString)
    val detectionsArray = root.getJSONArray("detections")
    return (0 until detectionsArray.length()).map { i ->
        val obj = detectionsArray.getJSONObject(i)
        val box = obj.getJSONObject("box")
        Detection(
            box = BoundingBox(
                x1 = box.getDouble("x1").toFloat(),
                y1 = box.getDouble("y1").toFloat(),
                x2 = box.getDouble("x2").toFloat(),
                y2 = box.getDouble("y2").toFloat()
            ),
            score = obj.getDouble("score").toFloat(),
            label = obj.getString("label")
        )
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

/**
 * TestResultScreen
 *
 * Follows the ProceduresScreen Review pattern:
 * - Top Bar: RealWearTopBar with defect status badge
 * - Main 70/30 Split:
 *    - Left 70%: Captured image (Layer 1) with Bounding Box and Label overlay (Layer 2)
 *    - Right 30%: Side bar with Detect List and AI detection results
 * - Bottom Bar: RealWearBottomBar with navigation/action buttons
 */
@Composable
fun TestResultScreen(
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val context = LocalContext.current

    // ── Load & parse result JSON from assets ──────────────────────────────
    val detections = remember {
        try {
            val jsonString = context.assets.open("SampleResult/result2.json")
                .bufferedReader()
                .use(BufferedReader::readText)
            parseDetections(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedDetectionIndex by remember { mutableIntStateOf(0) }

    // ── Track rendered image size in the 70% area for coordinate scaling ──
    var renderedImageSize by remember { mutableStateOf(IntSize.Zero) }

    // ── Read intrinsic dimensions of the asset image (no full decode) ──────
    val intrinsicSize: IntSize = remember {
        try {
            val opts = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.assets.open("SampleResult/RustImage2.jpeg").use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, opts)
            }
            IntSize(opts.outWidth, opts.outHeight)
        } catch (e: Exception) {
            IntSize.Zero
        }
    }

    // ── Colours ───────────────────────────────────────────────────────────
    val boxColor = Color(0xFFFFD600)        // vivid yellow for bounding box
    val labelBgColor = Color(0xFFFFD600)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .statusBarsPadding()
    ) {
        // ══ TOP BAR ══════════════════════════════════════════════════════════
        Surface(shadowElevation = 4.dp, color = BgPrimary) {
            RealWearTopBar(
                title = "AI DETECTION REVIEW",
                rightContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (detections.isEmpty()) AccentGreen else AccentRed,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (detections.isEmpty()) "NO DEFECTS" else "${detections.size} DEFECT FOUND",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        // ══ MAIN CONTENT (70% Image & Bounding Box / 30% Side Bar) ═══════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // ── Left 70%: Image + Bounding Box Layer ────────────────────────
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .background(BgSurface)
                    .onGloballyPositioned { coords ->
                        renderedImageSize = coords.size
                    }
            ) {
                // Layer 1: Image
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/SampleResult/RustImage2.jpeg")
                        .build(),
                    contentDescription = "AI Detection Result Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Layer 2: Bounding Box & Label Overlay
                if (renderedImageSize != IntSize.Zero && intrinsicSize != IntSize.Zero) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // ContentScale.Fit math
                        val scaleX = size.width / intrinsicSize.width.toFloat()
                        val scaleY = size.height / intrinsicSize.height.toFloat()
                        val scale = minOf(scaleX, scaleY)

                        val offsetX = (size.width - intrinsicSize.width * scale) / 2f
                        val offsetY = (size.height - intrinsicSize.height * scale) / 2f

                        for ((index, det) in detections.withIndex()) {
                            val b = det.box
                            val left = (b.x1.coerceAtLeast(0f) * scale) + offsetX
                            val top = (b.y1 * scale) + offsetY
                            val right = (b.x2 * scale) + offsetX
                            val bottom = (b.y2 * scale) + offsetY

                            val isSelected = index == selectedDetectionIndex

                            drawRect(
                                color = if (isSelected) boxColor else boxColor.copy(alpha = 0.6f),
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx())
                            )
                        }
                    }

                    // Label chips over bounding boxes
                    val density = context.resources.displayMetrics.density
                    for ((index, det) in detections.withIndex()) {
                        val b = det.box

                        val scaleX = renderedImageSize.width / intrinsicSize.width.toFloat()
                        val scaleY = renderedImageSize.height / intrinsicSize.height.toFloat()
                        val scale = minOf(scaleX, scaleY)

                        val offsetX = (renderedImageSize.width - intrinsicSize.width * scale) / 2f
                        val offsetY = (renderedImageSize.height - intrinsicSize.height * scale) / 2f

                        val leftPx = (b.x1.coerceAtLeast(0f) * scale) + offsetX
                        val topPx = (b.y1 * scale) + offsetY

                        val leftDp = (leftPx / density).dp
                        val topDp = (topPx / density).dp

                        val pct = (det.score * 100).toInt()
                        val isSelected = index == selectedDetectionIndex

                        Box(
                            modifier = Modifier
                                .offset(x = leftDp, y = (topDp - 26.dp).coerceAtLeast(4.dp))
                                .background(
                                    color = if (isSelected) labelBgColor else labelBgColor.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${det.label.uppercase()}  $pct%",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ── Right 30%: Side Bar (Detect List & Details) ─────────────────
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .background(BgPrimary)
                    .padding(8.dp)
            ) {
                Text(
                    text = "DETECT LIST",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (detections.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No defects found",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        itemsIndexed(detections) { index, detection ->
                            val isSelected = selectedDetectionIndex == index
                            val scorePct = (detection.score * 100).toInt()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentBlue.copy(alpha = 0.15f) else BgSurface)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = AccentBlue,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                        } else Modifier
                                    )
                                    .clickable { selectedDetectionIndex = index }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = detection.label.uppercase(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$scorePct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (detection.score >= 0.5f) AccentRed else Color(0xFFFFB300)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Box: [${detection.box.x1.toInt()}, ${detection.box.y1.toInt()} - ${detection.box.x2.toInt()}, ${detection.box.y2.toInt()}]",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            HorizontalDivider(color = DividerColor)
                        }
                    }
                }
            }
        }

        // ══ BOTTOM BAR ═══════════════════════════════════════════════════════
        RealWearBottomBar {
            RealWearButton(
                label = "BACK",
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            RealWearButton(
                label = "CONFIRM DEFECTS",
                onClick = onConfirm,
                containerColor = AccentGreen,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun TestResultScreenPreview() {
    QAApp_RealwearTheme {
        TestResultScreen()
    }
}
