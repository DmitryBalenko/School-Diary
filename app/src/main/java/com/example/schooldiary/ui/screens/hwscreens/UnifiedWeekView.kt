package com.example.schooldiary.ui.screens.hwscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bymrd1mm.schooldiary.GeminiClient
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.bymrd1mm.schooldiary.SimpleAudioPlayer
import com.bymrd1mm.schooldiary.getNextLessonDay
import com.example.schooldiary.BellTime
import com.example.schooldiary.CardDark
import com.example.schooldiary.Homework
import com.example.schooldiary.Lesson
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.ImageViewer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@Composable
fun WeekViewContent(
    hwList: MutableList<Homework>,
    currentSchedule: Map<String, List<Lesson>>,
    bells: List<BellTime>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTransfer: (Homework) -> Unit,
    onTransferCustom: (Homework) -> Unit
) {
    val daysKeys =
        listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    val todayDateObj = java.time.LocalDate.now()
    val mondayDateObj = todayDateObj.minusDays((todayDateObj.dayOfWeek.value - 1).toLong())
    val thisSaturdayStr = mondayDateObj.plusDays(5).toString()
    val thisSundayStr = mondayDateObj.plusDays(6).toString()

    val hasSat by remember(hwList) {
        derivedStateOf { hwList.any { !it.isArchived && it.targetDate?.trim()?.substringBefore("T") == thisSaturdayStr } }
    }
    val hasSun by remember(hwList) {
        derivedStateOf { hwList.any { !it.isArchived && it.targetDate?.trim()?.substringBefore("T") == thisSundayStr } }
    }

    val (targetIndex, todayIndex, isAfter16, startOfWeek) = remember(hasSat, hasSun) {
        val now = java.time.LocalDateTime.now()
        val currentDayStr = now.dayOfWeek.name
        val currentDayOfWeek = now.toLocalDate().dayOfWeek.value
        val currentHour = now.hour
        val todayDate = now.toLocalDate()

        val todayIdx = daysKeys.indexOf(currentDayStr)

        val targetIdx = when (currentDayStr) {
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY" -> if (currentHour < 16) todayIdx else todayIdx + 1
            "FRIDAY" -> {
                if (currentHour < 16) todayIdx
                else if (hasSat) 5
                else if (hasSun) 6
                else 0
            }
            "SATURDAY" -> {
                // После 16:00 переводим фокус на воскресенье ТОЛЬКО если там есть ДЗ.
                // Иначе - остаемся на субботе (чтобы карточка не пропала), если на ней есть ДЗ.
                if (currentHour >= 16 && hasSun) 6
                else if (hasSat) 5
                else 0
            }
            "SUNDAY" -> {
                // В воскресенье всегда остаемся на месте, если есть ДЗ, чтобы оно не исчезло вечером.
                if (hasSun) 6
                else 0
            }
            else -> 0
        }

        // Неделя сдвигается вперед ТОЛЬКО если фокус перешел на понедельник (0)
        // в пятницу, субботу или воскресенье. В противном случае остаемся на текущей неделе!
        val shiftToNextWeek = when (currentDayStr) {
            "FRIDAY", "SATURDAY", "SUNDAY" -> targetIdx == 0
            else -> false
        }

        val baseStart = if (shiftToNextWeek) {
            todayDate.plusDays((8 - currentDayOfWeek).toLong())
        } else {
            todayDate.minusDays((currentDayOfWeek - 1).toLong())
        }

        Tuple4(targetIdx, todayIdx, currentHour >= 16, baseStart)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = targetIndex)

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val hwManager = remember { HomeworkManager(context.applicationContext) }

    val hwMap by remember {
        derivedStateOf {
            hwList.filter { !it.isArchived }.groupBy { it.subject.trim() }
        }
    }

    var viewingImages by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var playingAudioId by remember { mutableStateOf<Long?>(null) }
    var transcribingId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            top = topPadding,
            start = 16.dp,
            end = 16.dp,
            bottom = bottomPadding
        )
    ) {
        itemsIndexed(items = daysKeys, key = { _, day -> day }) { index, dayKey ->
            val isWeekend = dayKey == "SATURDAY" || dayKey == "SUNDAY"
            val isVisible =
                !isWeekend || (dayKey == "SATURDAY" && hasSat) || (dayKey == "SUNDAY" && hasSun)

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(animationSpec = tween(400)) +
                        shrinkVertically(
                            animationSpec = tween(400),
                            shrinkTowards = Alignment.Top
                        )
            ) {
                Box(modifier = Modifier.padding(bottom = 24.dp)) {
                    val dayOffset = index.toLong()
                    val sectionDate = startOfWeek.plusDays(dayOffset)
                    val realToday = todayDateObj.toEpochDay()
                    val sectionEpoch = sectionDate.toEpochDay()
                    val daysDiff = sectionEpoch - realToday

                    DaySection(
                        dayName = Tr.get(dayKey, lang),
                        lessons = currentSchedule[dayKey] ?: emptyList(),
                        hwMap = hwMap,
                        dayKey = dayKey,
                        sectionDate = sectionDate,
                        currentSchedule = currentSchedule,
                        bells = bells,
                        daysDiff = daysDiff,
                        isFocused = (index == targetIndex),
                        isAfter16 = isAfter16,
                        playingAudioId = playingAudioId,
                        transcribingId = transcribingId,
                        onPlayAudio = { id, path ->
                            if (playingAudioId == id) {
                                SimpleAudioPlayer.stop(); playingAudioId = null
                            } else {
                                playingAudioId = id; SimpleAudioPlayer.play(path) {
                                    playingAudioId = null
                                }
                            }
                        },
                        onTranscribe = { id, path, currentText ->
                            if (settingsManager.apiKey.isBlank()) {
                                Toast.makeText(
                                    context,
                                    Tr.get("no_api_key", lang),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            } else {
                                transcribingId = id
                                coroutineScope.launch {
                                    val transcript =
                                        GeminiClient.transcribeAudio(
                                            File(path),
                                            settingsManager.apiKey
                                        )
                                    transcribingId = null
                                    if (!transcript.isNullOrBlank()) {
                                        val separator = if (currentText.isNotEmpty()) "\n" else ""
                                        val indexHw = hwList.indexOfFirst { it.id == id }
                                        if (indexHw != -1) {
                                            val newHw =
                                                hwList[indexHw].copy(text = hwList[indexHw].text + separator + transcript)
                                            hwList[indexHw] = newHw
                                            hwManager.updateHomework(newHw)
                                        }
                                    }
                                }
                            }
                        },
                        onDeleteHw = { id ->
                            coroutineScope.launch {
                                if (playingAudioId == id) {
                                    SimpleAudioPlayer.stop(); playingAudioId = null
                                }
                                delay(300)
                                hwManager.archiveHomework(id)
                                val idx = hwList.indexOfFirst { it.id == id }
                                if (idx != -1) hwList[idx] = hwList[idx].copy(isArchived = true)
                            }
                        },
                        onViewImage = { paths, idx -> viewingImages = Pair(paths, idx) },
                        onTransfer = onTransfer,
                        onTransferCustom = onTransferCustom,
                        lang = lang
                    )
                }
            }
        }
    }
    if (viewingImages != null) {
        ImageViewer(viewingImages!!.first, viewingImages!!.second) { viewingImages = null }
    }
}

@Composable
fun DaySection(
    dayName: String,
    lessons: List<Lesson>,
    hwMap: Map<String, List<Homework>>,
    dayKey: String,
    sectionDate: LocalDate,
    currentSchedule: Map<String, List<Lesson>>,
    bells: List<BellTime>,
    daysDiff: Long,
    isFocused: Boolean,
    isAfter16: Boolean,
    playingAudioId: Long?,
    transcribingId: Long?,
    onPlayAudio: (Long, String) -> Unit,
    onTranscribe: (Long, String, String) -> Unit,
    onDeleteHw: (Long) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
    onTransfer: (Homework) -> Unit,
    onTransferCustom: (Homework) -> Unit,
    lang: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val lessonSubjects = lessons.map { it.subject.trim() }.filter { it.isNotEmpty() }

    fun findHomeworks(subject: String): List<Homework> {
        val subjectHwList = hwMap[subject.trim()] ?: return emptyList()
        return subjectHwList.filter { hw ->
            val isCorrectDay =
                hw.targetDay == dayKey || Tr.data.values.any { langMap -> langMap[dayKey] == hw.targetDay }
            if (!isCorrectDay) return@filter false

            if (isOverdue(hw)) {
                val nextDay = getNextLessonDay(hw.subject, currentSchedule, isTransfer = true)
                if (nextDay != "UNKNOWN") {
                    return@filter false
                }
            }
            true
        }
    }

    val extraHomeworks = hwMap.values.flatten().filter { hw ->
        if (hw.isArchived) return@filter false
        val tDateStr = hw.targetDate?.trim() ?: ""
        if (tDateStr.isNotEmpty()) {
            try {
                val cleanStr =
                    if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(
                        " "
                    )
                cleanStr == sectionDate.toString() && hw.subject.trim() !in lessonSubjects
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    val extraBySubject = extraHomeworks.groupBy { it.subject.trim() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .border(
                if (isFocused) 1.dp else 0.dp,
                if (isFocused) Color.White else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .background(CardDark, RoundedCornerShape(16.dp))
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dayName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                val badgeText = if (isFocused) {
                    when {
                        daysDiff == 0L && !isAfter16 -> Tr.get("today", lang)
                        daysDiff == 1L -> Tr.get("tomorrow", lang)
                        daysDiff == 2L -> Tr.get("day_after_tomorrow", lang)
                        dayKey == "MONDAY" -> Tr.get("start_of_week", lang)
                        else -> null
                    }
                } else null

                if (badgeText != null) {
                    Text(
                        text = " $badgeText",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    val sb = StringBuilder().append(dayName.uppercase()).append("\n")
                    lessons.forEach { l ->
                        if (l.subject.isNotEmpty()) {
                            sb.append("• ${l.subject}")
                            val hws = findHomeworks(l.subject)
                            if (hws.isNotEmpty()) {
                                val combinedText =
                                    hws.mapNotNull { it.text.takeIf { t -> t.isNotBlank() } }
                                        .joinToString(", ")
                                if (combinedText.isNotBlank()) sb.append(" — $combinedText")
                            }
                            sb.append("\n")
                        }
                    }

                    if (extraBySubject.isNotEmpty()) {
                        sb.append("\n${Tr.get("extra_tasks", lang)}\n")
                        extraBySubject.forEach { (subj, hws) ->
                            sb.append("• $subj")
                            val combinedText =
                                hws.mapNotNull { it.text.takeIf { t -> t.isNotBlank() } }
                                    .joinToString(", ")
                            if (combinedText.isNotBlank()) sb.append(" — $combinedText")
                            sb.append("\n")
                        }
                    }

                    clipboardManager.setText(AnnotatedString(sb.toString()))
                    Toast.makeText(context, Tr.get("copied", lang), Toast.LENGTH_SHORT).show()
                }, modifier = Modifier
                    .size(32.dp)
                    .background(Zinc900, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    "Copy",
                    tint = Zinc500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        val maxIndex =
            if (lessons.isNotEmpty()) lessons.indexOfLast { it.subject.isNotEmpty() } else -1
        if (maxIndex != -1) {
            for (i in 0..maxIndex) {
                val lesson = if (i < lessons.size) lessons[i] else Lesson("", "", "", "", "")
                val timeStart = if (i < bells.size) bells[i].start else ""
                val timeEnd = if (i < bells.size) bells[i].end else ""

                if (lesson.subject.isNotEmpty()) {
                    val homeworks = findHomeworks(lesson.subject)

                    val overdueHomeworks = hwMap[lesson.subject.trim()]?.filter { hw ->
                        isOverdue(hw) && getNextLessonDay(
                            hw.subject,
                            currentSchedule,
                            isTransfer = true
                        ) == dayKey
                    } ?: emptyList()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(40.dp)) {
                            Text(timeStart, color = Color.White, fontSize = 13.sp)
                            Text(timeEnd, color = Zinc500, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (lesson.icon.isNotBlank() && lesson.icon != "📌") {
                            Text(lesson.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lesson.subject, color = Color.White, fontSize = 16.sp)
                        }
                        if (lesson.room.isNotEmpty()) Text(
                            lesson.room,
                            color = Zinc500,
                            fontSize = 13.sp
                        )
                    }

                    AnimatedVisibility(
                        visible = overdueHomeworks.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val countText =
                                if (overdueHomeworks.size > 1) " (${overdueHomeworks.size})" else ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Tr.get("uncompleted_hw", lang) + countText,
                                    color = Zinc500,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Tr.get("transfer_q", lang),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            overdueHomeworks.forEach { onTransfer(it) }
                                        }
                                        .padding(vertical = 2.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = homeworks.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        HomeworkListCard(
                            homeworks = homeworks,
                            currentSchedule = currentSchedule,
                            playingAudioId = playingAudioId,
                            transcribingId = transcribingId,
                            onPlayAudio = onPlayAudio,
                            onTranscribe = onTranscribe,
                            onDeleteHw = onDeleteHw,
                            onViewImage = onViewImage,
                            onTransferCustom = onTransferCustom,
                            lang = lang
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(0.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(40.dp)) {
                            Text(timeStart, color = Zinc500, fontSize = 13.sp)
                            Text(timeEnd, color = Zinc500, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("☕", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "--- " + Tr.get("window", lang) + " ---",
                            color = Zinc500,
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                if (i < maxIndex) Divider(color = Zinc800, thickness = 0.5.dp)
            }
        } else {
            if (dayKey != "SATURDAY" && dayKey != "SUNDAY") {
                Text(
                    Tr.get("no_lessons", lang),
                    color = Zinc500,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        val extraSubjectsHistory = remember { mutableMapOf<String, String>() }
        extraBySubject.forEach { (subj, hws) ->
            if (hws.isNotEmpty()) {
                extraSubjectsHistory[subj] = hws.first().icon
            }
        }

        AnimatedVisibility(
            visible = extraSubjectsHistory.keys.any { !extraBySubject[it].isNullOrEmpty() },
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Zinc800, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Tr.get("extra_tasks", lang),
                    color = Zinc500,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                extraSubjectsHistory.forEach { (subj, icon) ->
                    val hws = extraBySubject[subj] ?: emptyList()
                    AnimatedVisibility(
                        visible = hws.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.width(40.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                if (icon.isNotBlank() && icon != "📌") {
                                    Text(icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(subj, color = Color.White, fontSize = 16.sp)
                            }

                            HomeworkListCard(
                                homeworks = hws,
                                currentSchedule = currentSchedule,
                                playingAudioId = playingAudioId,
                                transcribingId = transcribingId,
                                onPlayAudio = onPlayAudio,
                                onTranscribe = onTranscribe,
                                onDeleteHw = onDeleteHw,
                                onViewImage = onViewImage,
                                onTransferCustom = onTransferCustom,
                                lang = lang
                            )
                        }
                    }
                }
            }
        }
    }
}