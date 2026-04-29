package com.example.schooldiary.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.HomeworkManager
import com.example.schooldiary.BlackBg
import com.example.schooldiary.BlueAction
import com.example.schooldiary.Homework
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.SimpleHeader
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarArchiveScreen(navController: NavController, hwManager: HomeworkManager, lang: String) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var isNext by remember { mutableStateOf(true) } // Напрямок гортання

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var archivedTasks by remember { mutableStateOf<List<Homework>>(emptyList()) }
    var showTasksSheet by remember { mutableStateOf(false) }

    // Инструменты для обратной связи и буфера обмена
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Завантаження архівних завдань
    LaunchedEffect(Unit) {
        archivedTasks = hwManager.getHomeworkListAsync().filter { it.isArchived }
    }

    fun getDisplayDate(hw: Homework): LocalDate {
        return if (!hw.targetDate.isNullOrEmpty()) {
            try {
                LocalDate.parse(hw.targetDate)
            } catch (e: Exception) {
                LocalDate.parse(hw.date)
            }
        } else {
            LocalDate.parse(hw.date)
        }
    }

    val tasksForSelectedDate = remember(selectedDate, archivedTasks) {
        archivedTasks.filter { getDisplayDate(it) == selectedDate }
    }

    val eventDates = remember(archivedTasks) {
        archivedTasks.map { getDisplayDate(it) }.toSet()
    }

    val weekDays = remember(lang) {
        if (lang == "en") listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
        else listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "НД")
    }

    // --- Bottom Sheet (Список завдань за обраний день) ---
    if (showTasksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTasksSheet = false },
            containerColor = Zinc900,
            contentColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (tasksForSelectedDate.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Tr.get("events_none", lang), color = Zinc500, fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = tasksForSelectedDate, key = { it.id }) { hw ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { /* ничего не делаем */ },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val textToCopy = "${hw.icon} ${hw.subject}: ${hw.text}"
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            Toast.makeText(
                                                context,
                                                Tr.get("copied", lang),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ),
                                colors = CardDefaults.cardColors(containerColor = BlackBg),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Zinc800)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(hw.icon, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = hw.subject,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        if (hw.text.isNotEmpty()) {
                                            Text(
                                                text = hw.text,
                                                color = Zinc500,
                                                fontSize = 13.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ГОЛОВНИЙ ЕКРАН (БЕЗ ТІНЕЙ) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ПРОСТИЙ ХЕДЕР ЗВЕРХУ
            SimpleHeader(navController, Tr.get("history_calendar", lang))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                // --- РЯДОК З МІСЯЦЕМ ТА СТРІЛКАМИ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ШВИДКА АНІМАЦІЯ НАЗВИ МІСЯЦЯ (Вгору/Вниз)
                    AnimatedContent(
                        targetState = yearMonth,
                        transitionSpec = {
                            val animSpec = tween<IntOffset>(150, easing = FastOutSlowInEasing)
                            val fadeSpec = tween<Float>(150)
                            if (isNext) {
                                (slideInVertically(animSpec) { height -> height } + fadeIn(fadeSpec)).togetherWith(
                                    slideOutVertically(animSpec) { height -> -height } + fadeOut(
                                        fadeSpec
                                    )
                                )
                            } else {
                                (slideInVertically(animSpec) { height -> -height } + fadeIn(fadeSpec)).togetherWith(
                                    slideOutVertically(animSpec) { height -> height } + fadeOut(
                                        fadeSpec
                                    )
                                )
                            }
                        },
                        label = "MonthAnim"
                    ) { targetMonth ->
                        Text(
                            text = "${
                                targetMonth.month.getDisplayName(
                                    java.time.format.TextStyle.FULL,
                                    if (lang == "ua") Locale("uk") else Locale.ENGLISH
                                ).replaceFirstChar { it.uppercase() }
                            } ${targetMonth.year}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Zinc900, CircleShape)
                                .clickable {
                                    isNext = false // Гортаємо назад
                                    yearMonth = yearMonth.minusMonths(1)
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Zinc900, CircleShape)
                                .clickable {
                                    isNext = true // Гортаємо вперед
                                    yearMonth = yearMonth.plusMonths(1)
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                        }
                    }
                }

                // --- ДНІ ТИЖНЯ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Zinc500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // --- ШВИДКА АНІМАЦІЯ СІТКИ КАЛЕНДАРЯ (Вліво/Вправо) ---
                AnimatedContent(
                    targetState = yearMonth,
                    transitionSpec = {
                        val animSpec = tween<IntOffset>(150, easing = FastOutSlowInEasing)
                        val fadeSpec = tween<Float>(150)

                        if (isNext) {
                            (slideInHorizontally(animSpec) { width -> width } + fadeIn(fadeSpec)).togetherWith(
                                slideOutHorizontally(animSpec) { width -> -width } + fadeOut(
                                    fadeSpec
                                )
                            )
                        } else {
                            (slideInHorizontally(animSpec) { width -> -width } + fadeIn(fadeSpec)).togetherWith(
                                slideOutHorizontally(animSpec) { width -> width } + fadeOut(fadeSpec)
                            )
                        }
                    },
                    label = "GridAnim"
                ) { targetMonth ->

                    // Розрахунок днів саме для місяця, який зараз малюється в анімації
                    val daysInMonth = remember(targetMonth) {
                        val days = mutableListOf<LocalDate?>()
                        val firstOfMonth = targetMonth.atDay(1)
                        val daysInMonthVal = targetMonth.lengthOfMonth()
                        val firstDayOfWeek = firstOfMonth.dayOfWeek.value
                        repeat(firstDayOfWeek - 1) { days.add(null) }
                        for (i in 1..daysInMonthVal) {
                            days.add(targetMonth.atDay(i))
                        }
                        days
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(daysInMonth) { date ->
                            if (date == null) {
                                Spacer(modifier = Modifier.aspectRatio(1f))
                            } else {
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()
                                val hasEvent = eventDates.contains(date)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Zinc900)
                                        .border(
                                            width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                                            color = if (isSelected) Color.White else if (isToday) Zinc500 else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedDate = date
                                            showTasksSheet = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            color = if (isToday && !isSelected) BlueAction else Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        )

                                        if (hasEvent) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) Color.White else BlueAction,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // Кінець AnimatedContent сітки

                Spacer(modifier = Modifier.height(24.dp))

                // --- ПЛАШКА З АРХІВОМ ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📂", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(Tr.get("archived", lang), color = Zinc500, fontSize = 12.sp)
                            Text(
                                "${tasksForSelectedDate.size} завдань за ${selectedDate.dayOfMonth}.${selectedDate.monthValue}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}