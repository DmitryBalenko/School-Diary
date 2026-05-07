package com.example.schooldiary.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.BellManager
import com.bymrd1mm.schooldiary.GeminiClient
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.bymrd1mm.schooldiary.SimpleAudioPlayer
import com.example.schooldiary.BellTime
import com.example.schooldiary.BlackBg
import com.example.schooldiary.CardDark
import com.example.schooldiary.Homework
import com.example.schooldiary.Lesson
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.getNextLessonDate
import com.example.schooldiary.getNextLessonDay
import com.example.schooldiary.ui.components.AsyncImagePreview
import com.example.schooldiary.ui.components.ImageViewer
import com.example.schooldiary.ui.components.TelegramAudioPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private fun isOverdue(hw: Homework): Boolean {
    if (hw.isArchived) return false
    val tDateStr = hw.targetDate?.trim() ?: return false
    return try {
        val cleanStr = if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(" ")
        val tDate = LocalDate.parse(cleanStr)
        val today = LocalDate.now()

        if (tDate.isBefore(today)) return true
        if (tDate.isEqual(today) && LocalTime.now().hour >= 16) return true

        false
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifiedHomeworkScreen(
    navController: NavController,
    hwManager: HomeworkManager,
    scheduleManager: ScheduleManager,
    bellManager: BellManager,
    settingsManager: SettingsManager,
    lang: String
) {
    var viewMode by remember { mutableStateOf(ViewMode.WEEK) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val offsetX = remember { Animatable(0f) }
    val screenWidthPx = context.resources.displayMetrics.widthPixels.toFloat()
    val triggerThreshold = -screenWidthPx * 0.22f

    val hwList = remember { mutableStateListOf<Homework>() }
    val currentSchedule = remember { scheduleManager.getSchedule() }
    val bells = remember { bellManager.getBells() }

    val isPastThreshold by remember { derivedStateOf { offsetX.value <= triggerThreshold } }
    var hasVibrated by remember { mutableStateOf(false) }

    LaunchedEffect(isPastThreshold) {
        if (isPastThreshold && !hasVibrated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        12,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
            hasVibrated = true
        } else if (!isPastThreshold) {
            hasVibrated = false
        }
    }

    LaunchedEffect(Unit) {
        hwList.clear()
        hwList.addAll(hwManager.getHomeworkListAsync())
    }

    val onTransferHw = { hw: Homework ->
        scope.launch {
            val newDate = getNextLessonDate(hw.subject, currentSchedule, isTransfer = true)
            val newDay = getNextLessonDay(hw.subject, currentSchedule, isTransfer = true)
            val updatedHw = hw.copy(targetDate = newDate, targetDay = newDay)

            hwManager.updateHomework(updatedHw)
            val idx = hwList.indexOfFirst { it.id == hw.id }
            if (idx != -1) {
                hwList[idx] = updatedHw
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
    ) {
        if (offsetX.value < 0) {
            val pullDistance = offsetX.value.absoluteValue
            val arrowRotation by animateFloatAsState(if (isPastThreshold) 180f else 0f)
            val circleScale by animateFloatAsState(if (isPastThreshold) 1.15f else 1.0f)

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(80.dp)
                    .width(with(density) { pullDistance.toDp() })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Zinc800,
                            shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .size(56.dp)
                        .scale(circleScale)
                        .shadow(6.dp, CircleShape, spotColor = Color.Black.copy(0.5f))
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = arrowRotation })
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(BlackBg)
                .draggable(
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX.value + delta).coerceAtMost(0f)
                        scope.launch { offsetX.snapTo(newOffset) }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        scope.launch {
                            if (offsetX.value <= triggerThreshold) {
                                offsetX.animateTo(-screenWidthPx, tween(300))
                                navController.navigate("calendar")
                                delay(100)
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(
                                    0f,
                                    spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                                )
                            }
                        }
                    }
                )
        ) {
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350)) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
                    ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(200)) + scaleOut(
                                targetScale = 1.1f,
                                animationSpec = tween(200)
                            )
                        )
                },
                label = "contentMorph",
                modifier = Modifier.fillMaxSize()
            ) { mode ->
                if (mode == ViewMode.WEEK)
                    WeekViewContent(
                        hwList = hwList,
                        currentSchedule = currentSchedule,
                        bells = bells,
                        settingsManager = settingsManager,
                        lang = lang,
                        topPadding = 100.dp,
                        bottomPadding = 40.dp,
                        onTransfer = { onTransferHw(it) }
                    )
                else
                    ListViewContent(
                        hwList = hwList,
                        currentSchedule = currentSchedule,
                        settingsManager = settingsManager,
                        lang = lang,
                        topPadding = 100.dp,
                        bottomPadding = 40.dp,
                        onTransfer = { onTransferHw(it) }
                    )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg, BlackBg, BlackBg.copy(alpha = 0.98f),
                            BlackBg.copy(alpha = 0.89f), BlackBg.copy(alpha = 0.8f),
                            BlackBg.copy(alpha = 0.65f), BlackBg.copy(alpha = 0.45f),
                            BlackBg.copy(alpha = 0.25f), BlackBg.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 1.1f))
                },
                label = "titleMorph",
                modifier = Modifier.weight(1f)
            ) { mode ->
                Text(
                    text = if (mode == ViewMode.WEEK) Tr.get(
                        "week_schedule",
                        lang
                    ) else Tr.get("task_list", lang),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.WEEK) ViewMode.LIST else ViewMode.WEEK
                },
                modifier = Modifier
                    .background(Zinc800, CircleShape)
                    .size(40.dp)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200)) + rotationIn())
                            .togetherWith(fadeOut(animationSpec = tween(200)) + rotationOut())
                    },
                    label = "iconMorph"
                ) { mode ->
                    Icon(
                        imageVector = if (mode == ViewMode.WEEK) Icons.Outlined.FormatListBulleted else Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

enum class ViewMode { WEEK, LIST }

@Composable
fun WeekViewContent(
    hwList: MutableList<Homework>,
    currentSchedule: Map<String, List<Lesson>>,
    bells: List<BellTime>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTransfer: (Homework) -> Unit
) {
    // Всі дні тижня жорстко зафіксовані в пам'яті
    val daysKeys = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    // Динамічний стан для суботи та неділі
    val hasSat by remember(hwList) {
        derivedStateOf { hwList.any { !it.isArchived && (it.targetDay == "SATURDAY" || Tr.data.values.any { map -> map["SATURDAY"] == it.targetDay }) } }
    }
    val hasSun by remember(hwList) {
        derivedStateOf { hwList.any { !it.isArchived && (it.targetDay == "SUNDAY" || Tr.data.values.any { map -> map["SUNDAY"] == it.targetDay }) } }
    }

    val (targetIndex, todayIndex, isAfter16, startOfWeek) = remember(hasSat, hasSun) {
        val now = java.time.LocalDateTime.now()
        val currentDayStr = now.dayOfWeek.name
        val currentDayOfWeek = now.toLocalDate().dayOfWeek.value // 1..7
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
                if (currentHour < 16 && hasSat) 5
                else if (hasSun) 6
                else 0
            }
            "SUNDAY" -> {
                if (currentHour < 16 && hasSun) 6
                else 0
            }
            else -> 0
        }

        val shiftToNextWeek = when (currentDayStr) {
            "FRIDAY" -> currentHour >= 16 && !hasSat && !hasSun
            "SATURDAY" -> currentHour >= 16 && !hasSun || (!hasSat && !hasSun)
            "SUNDAY" -> currentHour >= 16 || (!hasSun)
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
        // Додано key, щоб анімації зникнення працювали коректно
        itemsIndexed(items = daysKeys, key = { _, day -> day }) { index, dayKey ->
            val isWeekend = dayKey == "SATURDAY" || dayKey == "SUNDAY"
            val isVisible = !isWeekend || (dayKey == "SATURDAY" && hasSat) || (dayKey == "SUNDAY" && hasSun)

            // AnimatedVisibility огортає день. Якщо завдання зникає, зникає і сам день плавно!
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(400)) + shrinkVertically(tween(400))
            ) {
                // Відступ перенесено сюди, щоб не було "фантомних" прогалин після зникнення
                Box(modifier = Modifier.padding(bottom = 24.dp)) {
                    val dayOffset = index.toLong()
                    val sectionDate = startOfWeek.plusDays(dayOffset)

                    DaySection(
                        dayName = Tr.get(dayKey, lang),
                        lessons = currentSchedule[dayKey] ?: emptyList(),
                        hwMap = hwMap,
                        dayKey = dayKey,
                        sectionDate = sectionDate,
                        currentSchedule = currentSchedule,
                        bells = bells,
                        isToday = (index == todayIndex && !isAfter16),
                        isTomorrow = (index == targetIndex && index != todayIndex),
                        isFocused = (index == targetIndex),
                        playingAudioId = playingAudioId,
                        transcribingId = transcribingId,
                        onPlayAudio = { id, path ->
                            if (playingAudioId == id) {
                                SimpleAudioPlayer.stop(); playingAudioId = null
                            } else {
                                playingAudioId = id; SimpleAudioPlayer.play(path) { playingAudioId = null }
                            }
                        },
                        onTranscribe = { id, path, currentText ->
                            if (settingsManager.apiKey.isBlank()) {
                                Toast.makeText(context, Tr.get("no_api_key", lang), Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                transcribingId = id
                                coroutineScope.launch {
                                    val transcript =
                                        GeminiClient.transcribeAudio(File(path), settingsManager.apiKey)
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
                                delay(300) // невелика затримка для тактильного ефекту кліку
                                hwManager.archiveHomework(id)
                                val idx = hwList.indexOfFirst { it.id == id }
                                if (idx != -1) hwList[idx] = hwList[idx].copy(isArchived = true)
                            }
                        },
                        onViewImage = { paths, idx -> viewingImages = Pair(paths, idx) },
                        onTransfer = onTransfer,
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
    isToday: Boolean,
    isTomorrow: Boolean,
    isFocused: Boolean,
    playingAudioId: Long?,
    transcribingId: Long?,
    onPlayAudio: (Long, String) -> Unit,
    onTranscribe: (Long, String, String) -> Unit,
    onDeleteHw: (Long) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
    onTransfer: (Homework) -> Unit,
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
            .animateContentSize() // Анімація зміни розмірів блоку
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

                if (isToday) {
                    Text(
                        text = " ${Tr.get("today", lang)}",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else if (isTomorrow) {
                    Text(
                        text = " ${Tr.get("tomorrow", lang)}",
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
                        isOverdue(hw) && getNextLessonDay(hw.subject, currentSchedule, isTransfer = true) == dayKey
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
                        Text(lesson.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
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
                            val countText = if (overdueHomeworks.size > 1) " (${overdueHomeworks.size})" else ""
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
                            playingAudioId = playingAudioId,
                            transcribingId = transcribingId,
                            onPlayAudio = onPlayAudio,
                            onTranscribe = onTranscribe,
                            onDeleteHw = onDeleteHw,
                            onViewImage = onViewImage,
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

        // --- ДОДАТКОВІ ЗАВДАННЯ (ПОЗА РОЗКЛАДОМ) ---
        val extraHomeworks = hwMap.values.flatten().filter { hw ->
            if (hw.isArchived) return@filter false
            val tDateStr = hw.targetDate?.trim() ?: ""
            if (tDateStr.isNotEmpty()) {
                try {
                    val cleanStr = if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(" ")
                    cleanStr == sectionDate.toString() && hw.subject.trim() !in lessonSubjects
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }

        val extraBySubject = extraHomeworks.groupBy { it.subject.trim() }

        // Зберігаємо історію, щоб завдання відмальовувало анімацію зникнення
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
                                Text(icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(subj, color = Color.White, fontSize = 16.sp)
                            }

                            HomeworkListCard(
                                homeworks = hws,
                                playingAudioId = playingAudioId,
                                transcribingId = transcribingId,
                                onPlayAudio = onPlayAudio,
                                onTranscribe = onTranscribe,
                                onDeleteHw = onDeleteHw,
                                onViewImage = onViewImage,
                                lang = lang
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListViewContent(
    hwList: MutableList<Homework>,
    currentSchedule: Map<String, List<Lesson>>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTransfer: (Homework) -> Unit
) {
    val displayItems by remember(hwList, currentSchedule) {
        derivedStateOf {
            hwList.filter { !it.isArchived }.map { hw ->
                val tDateStr = hw.targetDate?.trim() ?: ""
                var isExtra = false
                var cleanDateStr = ""
                if (tDateStr.isNotEmpty()) {
                    try {
                        val cleanStr = if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(" ")
                        cleanDateStr = cleanStr
                        val date = LocalDate.parse(cleanStr)
                        val dayKey = date.dayOfWeek.name
                        val dayLessons = currentSchedule[dayKey] ?: emptyList()
                        isExtra = hw.subject.trim() !in dayLessons.map { it.subject.trim() }
                    } catch (e: Exception) {
                        isExtra = false
                    }
                }
                Triple(hw, isExtra, cleanDateStr)
            }.sortedWith(Comparator { t1, t2 ->
                if (t1.second && !t2.second) return@Comparator -1
                if (!t1.second && t2.second) return@Comparator 1

                if (t1.second && t2.second) {
                    return@Comparator t1.third.compareTo(t2.third)
                }

                0
            })
        }
    }

    val context = LocalContext.current
    val hwManager = remember { HomeworkManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    var viewingImages by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var playingAudioId by remember { mutableStateOf<Long?>(null) }
    var transcribingId by remember { mutableStateOf<Long?>(null) }

    if (displayItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
            Text(Tr.get("no_tasks", lang), color = Zinc500)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                top = topPadding,
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = displayItems,
                key = { it.first.id }
            ) { (hw, isExtra, _) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(20.dp),
                    border = if (isExtra) BorderStroke(1.dp, Color.White) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isExtra) {
                            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = Tr.get("extra_badge", lang),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hw.icon, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    hw.subject,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "${Tr.get("recorded_on", lang)} ${hw.targetDay}",
                                    color = Zinc500,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (hw.text.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(hw.text, color = Color.White)
                        }

                        hw.audioPath?.let { path ->
                            Spacer(Modifier.height(8.dp))
                            TelegramAudioPlayer(
                                isPlaying = playingAudioId == hw.id,
                                onPlayPause = {
                                    if (playingAudioId == hw.id) {
                                        SimpleAudioPlayer.stop(); playingAudioId = null
                                    } else {
                                        playingAudioId = hw.id; SimpleAudioPlayer.play(path) {
                                            playingAudioId = null
                                        }
                                    }
                                },
                                onTranscribe = {
                                    if (settingsManager.apiKey.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            Tr.get("no_api_key", lang),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        transcribingId = hw.id
                                        coroutineScope.launch {
                                            val transcript = GeminiClient.transcribeAudio(
                                                File(path),
                                                settingsManager.apiKey
                                            )
                                            transcribingId = null
                                            if (!transcript.isNullOrBlank()) {
                                                val separator =
                                                    if (hw.text.isNotEmpty()) "\n" else ""
                                                val indexHw = hwList.indexOfFirst { it.id == hw.id }
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
                                isTranscribing = transcribingId == hw.id,
                                lang = lang,
                                isSaved = true
                            )
                        }

                        if (hw.imagePaths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(hw.imagePaths) { imgIdx, path ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImagePreview(path) {
                                            viewingImages = Pair(hw.imagePaths, imgIdx)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        AnimatedVisibility(
                            visible = isOverdue(hw),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val nextDayKey = getNextLessonDay(hw.subject, currentSchedule, isTransfer = true)
                            val nextDayTranslated = Tr.get(nextDayKey, lang)

                            val textWithoutEmoji =
                                Tr.get("overdue_transfer", lang).replace("⚠️ ", "")
                                    .replace("⚠️", "")

                            OutlinedButton(
                                onClick = { onTransfer(hw) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White.copy(
                                        alpha = 0.7f
                                    )
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "$textWithoutEmoji $nextDayTranslated?",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (playingAudioId == hw.id) SimpleAudioPlayer.stop()
                                    hwManager.archiveHomework(hw.id)
                                    val idx = hwList.indexOfFirst { it.id == hw.id }
                                    if (idx != -1) {
                                        hwList[idx] = hwList[idx].copy(isArchived = true)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Tr.get("done", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (viewingImages != null) {
        ImageViewer(viewingImages!!.first, viewingImages!!.second) { viewingImages = null }
    }
}

@Composable
fun HomeworkListCard(
    homeworks: List<Homework>,
    playingAudioId: Long?,
    transcribingId: Long?,
    onPlayAudio: (Long, String) -> Unit,
    onTranscribe: (Long, String, String) -> Unit,
    onDeleteHw: (Long) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
    lang: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Zinc800),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .padding(12.dp)
        ) {

            val allText =
                homeworks.mapNotNull { it.text.takeIf { t -> t.isNotBlank() } }
                    .joinToString("\n\n")
            if (allText.isNotBlank()) {
                Text(allText, color = Color.White.copy(0.9f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            homeworks.forEach { hw ->
                val currentAudioPath = hw.audioPath
                if (currentAudioPath != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TelegramAudioPlayer(
                        isPlaying = playingAudioId == hw.id,
                        onPlayPause = { onPlayAudio(hw.id, currentAudioPath) },
                        onTranscribe = {
                            onTranscribe(
                                hw.id,
                                currentAudioPath,
                                hw.text
                            )
                        },
                        isTranscribing = transcribingId == hw.id,
                        lang = lang,
                        isSaved = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val allImages = homeworks.flatMap { it.imagePaths }
            if (allImages.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(allImages) { imgIdx, path ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImagePreview(path) {
                                onViewImage(
                                    allImages,
                                    imgIdx
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = Tr.get("done", lang),
                    color = Zinc500,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            homeworks.forEach {
                                onDeleteHw(
                                    it.id
                                )
                            }
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}

fun rotationIn() = fadeIn(tween(200)) + scaleIn(initialScale = 0.5f)
fun rotationOut() = fadeOut(tween(200)) + scaleOut(targetScale = 0.5f)

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)