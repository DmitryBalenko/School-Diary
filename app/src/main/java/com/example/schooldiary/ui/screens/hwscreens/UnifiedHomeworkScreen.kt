package com.example.schooldiary.ui.screens.hwscreens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.BellManager
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.bymrd1mm.schooldiary.getNextLessonDate
import com.bymrd1mm.schooldiary.getNextLessonDay
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Homework
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
    val focusManager = LocalFocusManager.current

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

    var hwForCustomTransfer by remember { mutableStateOf<Homework?>(null) }
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
                        onTransfer = { onTransferHw(it) },
                        onTransferCustom = { hwForCustomTransfer = it }
                    )
                else
                    ListViewContent(
                        hwList = hwList,
                        currentSchedule = currentSchedule,
                        settingsManager = settingsManager,
                        lang = lang,
                        topPadding = 100.dp,
                        bottomPadding = 40.dp,
                        onTransfer = { onTransferHw(it) },
                        onTransferCustom = { hwForCustomTransfer = it }
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

        if (hwForCustomTransfer != null) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { hwForCustomTransfer = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                val updatedHw = hwForCustomTransfer!!.copy(
                                    targetDate = selectedDate.toString(),
                                    targetDay = Tr.get(selectedDate.dayOfWeek.name, lang)
                                )

                                // 1. ОДРАЗУ закриваємо діалог
                                hwForCustomTransfer = null

                                scope.launch {
                                    // 2. Чекаємо, поки діалог зникне і Compose поверне фокус на картку
                                    delay(200)

                                    // 3. Збиваємо фокус
                                    focusManager.clearFocus()

                                    // 4. ТІЛЬКИ ТЕПЕР оновлюємо дані.
                                    // Картка полетить вниз, але екран залишиться на місці!
                                    hwManager.updateHomework(updatedHw)
                                    val idx = hwList.indexOfFirst { it.id == updatedHw.id }
                                    if (idx != -1) hwList[idx] = updatedHw
                                }
                            } ?: run {
                                hwForCustomTransfer = null
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(Tr.get("save", lang))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { hwForCustomTransfer = null },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Zinc500)
                    ) {
                        Text(Tr.get("cancel", lang))
                    }
                },
                tonalElevation = 0.dp
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Zinc900,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Zinc500,
                        dayContentColor = Color.White,
                        selectedDayContainerColor = Color.White,
                        selectedDayContentColor = Color.Black,
                        todayContentColor = Color.White,
                        todayDateBorderColor = Zinc500,
                        selectedYearContainerColor = Color.White,
                        selectedYearContentColor = Color.Black
                    )
                )
            }
        }
    }
}