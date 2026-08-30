package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduleItem

@Composable
fun AddEditScheduleDialog(
    itemToEdit: ScheduleItem? = null,
    initialIsAllDay: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit,
    onDelete: ((ScheduleItem) -> Unit)? = null
) {
    var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
    var isAllDay by remember { mutableStateOf(itemToEdit?.isAllDay ?: initialIsAllDay) }
    var startHour by remember { mutableIntStateOf((itemToEdit?.startMinutes ?: 540) / 60) } // Default 09:00
    var startMin by remember { mutableIntStateOf((itemToEdit?.startMinutes ?: 540) % 60) }
    var endHour by remember { mutableIntStateOf((itemToEdit?.endMinutes ?: 660) / 60) } // Default 11:00
    var endMin by remember { mutableIntStateOf((itemToEdit?.endMinutes ?: 660) % 60) }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "공부/업무") }
    var colorHex by remember { mutableStateOf(itemToEdit?.colorHex ?: "#3B82F6") }
    var note by remember { mutableStateOf(itemToEdit?.note ?: "") }
    var priority by remember { mutableIntStateOf(itemToEdit?.priority ?: 0) }
    var hasReminder by remember { mutableStateOf(itemToEdit?.hasReminder ?: true) }
    var reminderMinutesBefore by remember { mutableIntStateOf(itemToEdit?.reminderMinutesBefore ?: 15) }

    val categories = listOf(
        Pair("공부/업무", "#3B82F6"),
        Pair("수면", "#6366F1"),
        Pair("식사", "#10B981"),
        Pair("운동", "#EF4444"),
        Pair("여가/취미", "#EC4899"),
        Pair("루틴", "#F59E0B"),
        Pair("건강", "#06B6D4"),
        Pair("기타", "#64748B")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemToEdit == null) "새 일정 / 할 일 추가" else "일정 수정",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (itemToEdit != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(itemToEdit)
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp).testTag("dialog_delete_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Switcher: 시간표 일정 vs 할 일 체크리스트
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isAllDay) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isAllDay = false }
                            .padding(vertical = 8.dp)
                            .testTag("dialog_type_timed"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (!isAllDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "시간대 일정",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (!isAllDay) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (!isAllDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isAllDay) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isAllDay = true }
                            .padding(vertical = 8.dp)
                            .testTag("dialog_type_allday"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = if (isAllDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "체크리스트 할 일",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isAllDay) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isAllDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("할 일 / 일정 제목") },
                    placeholder = { Text("예: 수학 문제집 풀기, 저녁 조깅") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_title_input")
                )

                // Time Pickers (if timed)
                AnimatedVisibility(visible = !isAllDay) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "시간 설정 (24시간 기준)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Start Time & End Time Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Start Time Block
                            TimeSelectorBlock(
                                label = "시작 시간",
                                hour = startHour,
                                min = startMin,
                                onHourChange = { startHour = (it + 24) % 24 },
                                onMinChange = { startMin = (it + 60) % 60 },
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "▶",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // End Time Block
                            TimeSelectorBlock(
                                label = "종료 시간",
                                hour = endHour,
                                min = endMin,
                                onHourChange = { endHour = (it + 24) % 24 },
                                onMinChange = { endMin = (it + 60) % 60 },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Quick Duration presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30 to "+30분", 60 to "+1시간", 90 to "+1.5시간", 120 to "+2시간").forEach { (mins, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val totalStart = startHour * 60 + startMin
                                            val newEnd = (totalStart + mins) % 1440
                                            endHour = newEnd / 60
                                            endMin = newEnd % 60
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Category & Color Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "카테고리 및 색상",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { (catName, hex) ->
                            val isSelected = category == catName
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) catColor else catColor.copy(alpha = 0.15f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        category = catName
                                        colorHex = hex
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else catColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "중요도",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "보통", 1 to "중요 ⭐", 2 to "매우 중요 🔥").forEach { (pri, label) ->
                            val isSelected = priority == pri
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { priority = pri }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Reminder Notification Section (for timed schedules)
                AnimatedVisibility(visible = !isAllDay) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (hasReminder) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "시작 15분 전 미리 알림",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (hasReminder) "일정 시작 ${reminderMinutesBefore}분 전에 푸시 알림 수신" else "알림 꺼짐",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = hasReminder,
                                    onCheckedChange = { hasReminder = it },
                                    modifier = Modifier.testTag("dialog_reminder_switch")
                                )
                            }

                            if (hasReminder) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        5 to "5분 전",
                                        10 to "10분 전",
                                        15 to "15분 전 (기본)",
                                        30 to "30분 전"
                                    ).forEach { (mins, label) ->
                                        val isSelected = reminderMinutesBefore == mins
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { reminderMinutesBefore = mins }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 10.sp
                                                ),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Notes / Memo Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("메모 및 세부 내용 (선택)") },
                    placeholder = { Text("참고사항이나 준비물 등") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val startMinutes = if (isAllDay) 0 else startHour * 60 + startMin
                        val endMinutes = if (isAllDay) 0 else endHour * 60 + endMin
                        val updatedItem = ScheduleItem(
                            id = itemToEdit?.id ?: 0L,
                            title = title.trim(),
                            startMinutes = startMinutes,
                            endMinutes = endMinutes,
                            isAllDay = isAllDay,
                            isCompleted = itemToEdit?.isCompleted ?: false,
                            category = category,
                            colorHex = colorHex,
                            note = note.trim(),
                            date = itemToEdit?.date ?: "",
                            priority = priority,
                            hasReminder = hasReminder,
                            reminderMinutesBefore = reminderMinutesBefore
                        )
                        onSave(updatedItem)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("dialog_save_btn")
            ) {
                Text(if (itemToEdit == null) "추가하기" else "저장하기", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_btn")) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun TimeSelectorBlock(
    label: String,
    hour: Int,
    min: Int,
    onHourChange: (Int) -> Unit,
    onMinChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour Adjuster
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▲",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clickable { onHourChange(hour + 1) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%02d", hour),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clickable { onHourChange(hour - 1) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = ":",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Minute Adjuster (10-minute steps)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▲",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clickable { onMinChange(min + 10) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%02d", min),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clickable { onMinChange(min - 10) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
