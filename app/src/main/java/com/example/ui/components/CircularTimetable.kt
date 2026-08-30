package com.example.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduleItem
import com.example.viewmodel.DailyStats
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CircularTimetable(
    schedules: List<ScheduleItem>,
    selectedItem: ScheduleItem?,
    currentMinute: Int,
    isToday: Boolean,
    dailyStats: DailyStats,
    onSelectItem: (ScheduleItem?) -> Unit,
    onToggleComplete: (ScheduleItem) -> Unit,
    onEditItem: (ScheduleItem) -> Unit,
    onDeleteItem: (ScheduleItem) -> Unit,
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timedSchedules = remember(schedules) {
        schedules.filter { !it.isAllDay }
    }
    val allDayTodos = remember(schedules) {
        schedules.filter { it.isAllDay }
    }

    // Infinite transition for current time pulsing needle dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle guide
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "시간표 조각을 터치하면 완료 체크 및 상세 확인이 가능합니다",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = onSurfaceVariantColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Circular Clock Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, outlineColor.copy(alpha = 0.3f), CircleShape)
                    .testTag("circular_timetable_canvas_container"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .pointerInput(timedSchedules) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val distFromCenter = kotlin.math.sqrt(dx * dx + dy * dy)
                                val maxRadius = size.width / 2f
                                val innerDonutRadius = maxRadius * 0.38f

                                if (distFromCenter < innerDonutRadius) {
                                    // Tapped center hub - clear selection
                                    onSelectItem(null)
                                    return@detectTapGestures
                                }

                                if (distFromCenter > maxRadius) {
                                    onSelectItem(null)
                                    return@detectTapGestures
                                }

                                // Calculate angle in degrees from -90 deg (top = 00:00)
                                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                // Align so that top (-90 deg) is 0 deg
                                angleDeg = (angleDeg + 90f + 360f) % 360f

                                val tappedMinute = ((angleDeg / 360f) * 1440f).toInt()

                                val clicked = timedSchedules.firstOrNull { item ->
                                    if (item.endMinutes >= item.startMinutes) {
                                        tappedMinute in item.startMinutes..item.endMinutes
                                    } else {
                                        // cross midnight
                                        tappedMinute >= item.startMinutes || tappedMinute <= item.endMinutes
                                    }
                                }

                                onSelectItem(if (selectedItem?.id == clicked?.id) null else clicked)
                            }
                        }
                ) {
                    drawCircularTimetable(
                        timedSchedules = timedSchedules,
                        selectedItem = selectedItem,
                        currentMinute = currentMinute,
                        isToday = isToday,
                        pulseAlpha = pulseAlpha,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        primaryColor = primaryColor,
                        outlineColor = outlineColor
                    )
                }

                // Center Hub Information
                CenterHub(
                    selectedItem = selectedItem,
                    timedSchedules = timedSchedules,
                    currentMinute = currentMinute,
                    isToday = isToday,
                    dailyStats = dailyStats,
                    onToggleComplete = onToggleComplete
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Selected Schedule Detail / Quick Action Card
        item {
            AnimatedVisibility(
                visible = selectedItem != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                selectedItem?.let { item ->
                    SelectedItemDetailCard(
                        item = item,
                        onToggleComplete = { onToggleComplete(item) },
                        onEdit = { onEditItem(item) },
                        onDelete = { onDeleteItem(item) },
                        onClose = { onSelectItem(null) }
                    )
                }
            }
        }

        // Untimed To-Dos Section (오늘의 할 일 체크리스트)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "오늘의 할 일 체크리스트",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${allDayTodos.count { it.isCompleted }}/${allDayTodos.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAddNewItem,
                    modifier = Modifier.height(34.dp).testTag("quick_add_todo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("일정/할일 추가", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (allDayTodos.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "등록된 종일 할 일이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ 버튼을 눌러 체크할 일정을 추가해보세요!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            items(allDayTodos, key = { it.id }) { todo ->
                AllDayTodoCard(
                    todo = todo,
                    onToggleComplete = { onToggleComplete(todo) },
                    onEdit = { onEditItem(todo) },
                    onDelete = { onDeleteItem(todo) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun DrawScope.drawCircularTimetable(
    timedSchedules: List<ScheduleItem>,
    selectedItem: ScheduleItem?,
    currentMinute: Int,
    isToday: Boolean,
    pulseAlpha: Float,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color,
    outlineColor: Color
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.width / 2f
    val innerRadius = outerRadius * 0.42f // donut inner hole
    val dialRadius = outerRadius - 2.dp.toPx()

    // 1. Background Donut
    drawCircle(
        color = outlineColor.copy(alpha = 0.12f),
        radius = outerRadius,
        center = center
    )

    // 2. Draw Schedule Slices
    timedSchedules.forEach { item ->
        val startMins = item.startMinutes
        val durMins = item.durationMinutes()
        val startAngle = (startMins / 1440f) * 360f - 90f
        val sweepAngle = (durMins / 1440f) * 360f

        val baseColor = try {
            Color(android.graphics.Color.parseColor(item.colorHex))
        } catch (e: Exception) {
            primaryColor
        }

        val isSelected = selectedItem?.id == item.id

        // Slice color: if completed, distinct visual style
        val sliceColor = if (item.isCompleted) {
            baseColor.copy(alpha = 0.85f)
        } else {
            baseColor.copy(alpha = 0.95f)
        }

        // Draw Arc Slice
        val arcRect = Size(outerRadius * 2, outerRadius * 2)
        val arcTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)

        drawArc(
            color = sliceColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = arcTopLeft,
            size = arcRect
        )

        // Draw Completed Checkmark Overlay & Stippling Pattern on segment
        if (item.isCompleted) {
            // Radial overlay for completed items
            drawArc(
                color = Color.Black.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = arcTopLeft,
                size = arcRect
            )

            // Draw Checkmark Badge on the arc
            val midAngleDeg = startAngle + sweepAngle / 2f
            val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
            val badgeDist = (innerRadius + outerRadius) / 2f
            val badgeX = (center.x + badgeDist * cos(midAngleRad)).toFloat()
            val badgeY = (center.y + badgeDist * sin(midAngleRad)).toFloat()

            // Badge circle
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = Offset(badgeX, badgeY)
            )
            drawCircle(
                color = Color(0xFF10B981), // Emerald check badge
                radius = 9.dp.toPx(),
                center = Offset(badgeX, badgeY)
            )

            // Draw checkmark symbol with Path
            val checkPath = Path().apply {
                val cx = badgeX
                val cy = badgeY
                val s = 5.dp.toPx()
                moveTo(cx - s * 0.8f, cy)
                lineTo(cx - s * 0.2f, cy + s * 0.7f)
                lineTo(cx + s * 0.9f, cy - s * 0.7f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Selection Highlight Border
        if (isSelected) {
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 3.5.dp.toPx())
            )
        }

        // Arc Divider line at start
        val startRad = Math.toRadians(startAngle.toDouble())
        val p1 = Offset(
            (center.x + innerRadius * cos(startRad)).toFloat(),
            (center.y + innerRadius * sin(startRad)).toFloat()
        )
        val p2 = Offset(
            (center.x + outerRadius * cos(startRad)).toFloat(),
            (center.y + outerRadius * sin(startRad)).toFloat()
        )
        drawLine(
            color = surfaceColor,
            start = p1,
            end = p2,
            strokeWidth = 2.dp.toPx()
        )

        // Draw Schedule Title text along arc midpoint if angle is wide enough
        if (sweepAngle >= 14f) {
            val midAngleDeg = startAngle + sweepAngle / 2f
            val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
            val textDist = (innerRadius + outerRadius) / 2f + if (item.isCompleted) 12.dp.toPx() else 0f

            val textX = (center.x + textDist * cos(midAngleRad)).toFloat()
            val textY = (center.y + textDist * sin(midAngleRad)).toFloat()

            // If not completed, draw text label
            if (!item.isCompleted || sweepAngle >= 25f) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        isAntiAlias = true
                        textSize = if (sweepAngle >= 30f) 28f else 22f
                        color = android.graphics.Color.WHITE
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        if (item.isCompleted) {
                            isStrikeThruText = true
                        }
                    }

                    // Trim title to fit
                    val maxLen = if (sweepAngle > 40f) 8 else if (sweepAngle > 20f) 5 else 3
                    val displayTitle = if (item.title.length > maxLen) {
                        item.title.take(maxLen) + ".."
                    } else {
                        item.title
                    }

                    save()
                    translate(textX, textY)
                    // Rotate text to follow slice direction
                    var rot = midAngleDeg + 90f
                    if (rot > 90f && rot < 270f) {
                        rot += 180f
                    }
                    rotate(rot)
                    drawText(displayTitle, 0f, if (item.isCompleted) 6f else 4f, paint)
                    restore()
                }
            }
        }
    }

    // 3. Clear Center Donut Hole
    drawCircle(
        color = surfaceColor,
        radius = innerRadius,
        center = center
    )
    drawCircle(
        color = outlineColor.copy(alpha = 0.25f),
        radius = innerRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 4. Draw 24-Hour Dial Markings & Labels
    for (hour in 0 until 24) {
        val hourAngleDeg = (hour / 24f) * 360f - 90f
        val hourRad = Math.toRadians(hourAngleDeg.toDouble())

        val isMajor = hour % 3 == 0 // 0, 3, 6, 9, 12, 15, 18, 21
        val tickLength = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
        val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

        val tickStart = Offset(
            (center.x + (outerRadius - tickLength) * cos(hourRad)).toFloat(),
            (center.y + (outerRadius - tickLength) * sin(hourRad)).toFloat()
        )
        val tickEnd = Offset(
            (center.x + outerRadius * cos(hourRad)).toFloat(),
            (center.y + outerRadius * sin(hourRad)).toFloat()
        )

        drawLine(
            color = if (isMajor) onSurfaceColor.copy(alpha = 0.6f) else onSurfaceVariantColor.copy(alpha = 0.3f),
            start = tickStart,
            end = tickEnd,
            strokeWidth = tickStroke
        )

        // Draw Major Hour Labels
        if (isMajor) {
            val labelDist = outerRadius - 18.dp.toPx()
            val labelX = (center.x + labelDist * cos(hourRad)).toFloat()
            val labelY = (center.y + labelDist * sin(hourRad)).toFloat()

            val hourText = when (hour) {
                0 -> "24시"
                12 -> "12시"
                else -> "${hour}시"
            }

            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    isAntiAlias = true
                    textSize = 24f
                    color = onSurfaceColor.toArgb()
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    alpha = 220
                }
                drawText(hourText, labelX, labelY + 8f, paint)
            }
        }
    }

    // 5. Current Time Needle / Indicator (if today)
    if (isToday) {
        val currAngleDeg = (currentMinute / 1440f) * 360f - 90f
        val currRad = Math.toRadians(currAngleDeg.toDouble())

        val needleTip = Offset(
            (center.x + (outerRadius - 2.dp.toPx()) * cos(currRad)).toFloat(),
            (center.y + (outerRadius - 2.dp.toPx()) * sin(currRad)).toFloat()
        )
        val needleBase = Offset(
            (center.x + (innerRadius - 4.dp.toPx()) * cos(currRad)).toFloat(),
            (center.y + (innerRadius - 4.dp.toPx()) * sin(currRad)).toFloat()
        )

        // Needle Line
        drawLine(
            color = Color(0xFFEF4444), // Vivid Red
            start = needleBase,
            end = needleTip,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Pulsing glowing circle at needle tip
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = pulseAlpha * 0.4f),
            radius = 10.dp.toPx(),
            center = needleTip
        )
        drawCircle(
            color = Color(0xFFEF4444),
            radius = 4.5.dp.toPx(),
            center = needleTip
        )
    }
}

@Composable
private fun CenterHub(
    selectedItem: ScheduleItem?,
    timedSchedules: List<ScheduleItem>,
    currentMinute: Int,
    isToday: Boolean,
    dailyStats: DailyStats,
    onToggleComplete: (ScheduleItem) -> Unit
) {
    // Current ongoing activity (if any)
    val ongoingItem = remember(timedSchedules, currentMinute) {
        timedSchedules.firstOrNull { item ->
            if (item.endMinutes >= item.startMinutes) {
                currentMinute in item.startMinutes..item.endMinutes
            } else {
                currentMinute >= item.startMinutes || currentMinute <= item.endMinutes
            }
        }
    }

    val curHour = (currentMinute / 60) % 24
    val curMin = currentMinute % 60
    val formattedTime = String.format("%02d:%02d", curHour, curMin)

    Column(
        modifier = Modifier
            .fillMaxSize(0.40f)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selectedItem != null) {
            // Selected slice overview
            Text(
                text = selectedItem.formattedTimeSpan(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = selectedItem.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textDecoration = if (selectedItem.isCompleted) TextDecoration.LineThrough else null
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Quick Complete button
            Surface(
                onClick = { onToggleComplete(selectedItem) },
                shape = CircleShape,
                color = if (selectedItem.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(30.dp).testTag("hub_complete_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (selectedItem.isCompleted) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "완료 토글",
                        tint = if (selectedItem.isCompleted) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Default center state: Current time & ongoing task or overall completion
            if (isToday) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            if (ongoingItem != null && isToday) {
                Text(
                    text = "지금 진행 중",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ongoingItem.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        textDecoration = if (ongoingItem.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "오늘 달성률",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${dailyStats.completionPercentage}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    color = if (dailyStats.completionPercentage == 100 && dailyStats.totalCount > 0)
                        Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${dailyStats.completedCount}/${dailyStats.totalCount}개",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectedItemDetailCard(
    item: ScheduleItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("selected_item_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = itemColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = itemColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.formattedTimeSpan(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (item.hasReminder && !item.isAllDay) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "알림",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.formattedReminderText(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
                ),
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )

            if (item.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Complete toggle button
                Button(
                    onClick = onToggleComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("detail_complete_btn")
                ) {
                    Icon(
                        imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isCompleted) "완료됨 (취소)" else "완료 체크",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("detail_edit_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .testTag("detail_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AllDayTodoCard(
    todo: ScheduleItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val todoColor = try {
        Color(android.graphics.Color.parseColor(todo.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("allday_todo_${todo.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (todo.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (todo.isCompleted) Color(0xFF10B981).copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox button
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(32.dp).testTag("todo_check_${todo.id}")
                ) {
                    Icon(
                        imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "체크",
                        tint = if (todo.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(todoColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = todo.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = todoColor
                        )
                    }
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (todo.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (todo.note.isNotBlank()) {
                        Text(
                            text = todo.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
