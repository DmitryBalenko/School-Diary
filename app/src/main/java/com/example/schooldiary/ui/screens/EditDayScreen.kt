package com.example.schooldiary.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.BellManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.example.schooldiary.BellTime
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Lesson
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.EmojiPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import kotlin.collections.map

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditDayScreen(
    navController: NavController,
    scheduleManager: ScheduleManager,
    bellManager: BellManager,
    dayKey: String,
    lang: String
) {
    val bells = remember { bellManager.getBells() }
    val savedSchedule = remember { scheduleManager.getSchedule() }

    // Ініціалізація списку уроків
    val lessons = remember {
        mutableStateListOf<Lesson>().apply {
            if (savedSchedule.containsKey(dayKey)) {
                addAll(savedSchedule[dayKey]!!.map { it.copy() })
                while (size < bells.size) add(Lesson(bells[size].start, bells[size].end, "", "", "📝"))
            } else {
                addAll(bells.map { Lesson(it.start, it.end, "", "", "📝") })
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedLessonIndex by remember { mutableIntStateOf(-1) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val state = rememberReorderableLazyListState(onMove = { from: ItemPosition, to: ItemPosition ->
        lessons.apply { add(to.index, removeAt(from.index)) }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    })

    // --- ДІАЛОГ ВИБОРУ ЕМОДЗІ (без змін) ---
    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                if (selectedLessonIndex != -1) {
                    val targetSubject = lessons[selectedLessonIndex].subject.trim()
                    lessons[selectedLessonIndex] = lessons[selectedLessonIndex].copy(icon = emoji)
                    if (targetSubject.isNotEmpty()) {
                        for (i in lessons.indices) {
                            if (lessons[i].subject.trim().equals(targetSubject, ignoreCase = true)) {
                                lessons[i] = lessons[i].copy(icon = emoji)
                            }
                        }
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                val fullSchedule = scheduleManager.getSchedule().toMutableMap()
                                var scheduleChanged = false
                                for (day in fullSchedule.keys) {
                                    val dayLessons = fullSchedule[day]!!.toMutableList()
                                    var dayChanged = false
                                    for (i in dayLessons.indices) {
                                        if (dayLessons[i].subject.trim().equals(targetSubject, ignoreCase = true)) {
                                            dayLessons[i] = dayLessons[i].copy(icon = emoji)
                                            dayChanged = true
                                            scheduleChanged = true
                                        }
                                    }
                                    if (dayChanged) fullSchedule[day] = dayLessons
                                }
                                if (scheduleChanged) scheduleManager.saveSchedule(fullSchedule)
                            }
                        }
                    }
                }
                showEmojiPicker = false
            },
            lang = lang
        )
    }

    // --- UI ЕКРАНУ ---
    Box(modifier = Modifier
        .fillMaxSize()
        .background(BlackBg)
        .pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }
    ) {
        LazyColumn(
            state = state.listState,
            contentPadding = PaddingValues(top = 140.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .imePadding() // Список стискається над клавіатурою
        ) {
            itemsIndexed(items = lessons, key = { _, item -> item.id }) { index, lesson ->
                ReorderableItem(state, key = lesson.id) { isDragging ->
                    val bell = if (index < bells.size) bells[index] else BellTime(0, "??", "??")
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "shadow")
                    val scale by animateFloatAsState(if (isDragging) 1.02f else 1f, label = "scale")

                    // 1. Створюємо Requester для цієї конкретної картки
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = elevation.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = false
                                alpha = 1f
                            }
                            // 2. Прив'язуємо Requester до всієї картки
                            .bringIntoViewRequester(bringIntoViewRequester),
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .detectReorderAfterLongPress(state)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- ЧАС ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(44.dp)
                            ) {
                                Text(bell.start, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(bell.end, color = Zinc500, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // --- ЦЕНТРАЛЬНА ЧАСТИНА ---
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // ІКОНКА
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Zinc800)
                                            .clickable {
                                                selectedLessonIndex = index
                                                showEmojiPicker = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lesson.icon, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // НАЗВА ПРЕДМЕТУ
                                    BasicTextField(
                                        value = lesson.subject,
                                        onValueChange = { v -> lessons[index] = lessons[index].copy(subject = v) },
                                        textStyle = TextStyle(
                                            color = if (lesson.subject.isEmpty()) Zinc500 else Color.White,
                                            fontSize = 16.sp
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        modifier = Modifier
                                            .widthIn(min = 50.dp)
                                            // 3. Коли отримуємо фокус, просимо показати ВСЮ картку
                                            .onFocusEvent { focusState ->
                                                if (focusState.isFocused) {
                                                    coroutineScope.launch {
                                                        bringIntoViewRequester.bringIntoView()
                                                    }
                                                }
                                            },
                                        decorationBox = { inner ->
                                            if (lesson.subject.isEmpty()) Text(Tr.get("window", lang), color = Zinc500) else inner()
                                        }
                                    )
                                    Spacer(modifier = Modifier.weight(1f).height(32.dp))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // КАБІНЕТ
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicTextField(
                                        value = lesson.room,
                                        onValueChange = { v -> lessons[index] = lessons[index].copy(room = v) },
                                        textStyle = TextStyle(color = Zinc500, fontSize = 12.sp),
                                        cursorBrush = SolidColor(Zinc500),
                                        modifier = Modifier
                                            .widthIn(min = 50.dp)
                                            // 3. Те саме для поля кабінету
                                            .onFocusEvent { focusState ->
                                                if (focusState.isFocused) {
                                                    coroutineScope.launch {
                                                        bringIntoViewRequester.bringIntoView()
                                                    }
                                                }
                                            },
                                        decorationBox = { inner ->
                                            if (lesson.room.isEmpty()) Text(Tr.get("cabinet", lang), color = Zinc700, fontSize = 12.sp) else inner()
                                        }
                                    )
                                    Spacer(modifier = Modifier.weight(1f).height(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // КНОПКА ОЧИСТИТИ
                            IconButton(
                                onClick = { lessons[index] = lessons[index].copy(subject = "", room = "", icon = "📝") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Zinc500, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- ВЕРХНІЙ ХЕДЕР (Градієнт та Кнопки) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Еталонна висота
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg,                    // 0%
                            BlackBg,                    // 25%
                            BlackBg.copy(alpha = 0.9f), // 50%
                            BlackBg.copy(alpha = 0.6f), // 70%
                            BlackBg.copy(alpha = 0.3f), // 85%
                            Color.Transparent           // 100%
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Відступ від системного годинника/челки
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Tr.get(dayKey, lang),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- НИЖНЯ ПАНЕЛЬ (Кнопка Зберегти під клавіатурою) ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, BlackBg.copy(alpha = 0.9f), BlackBg)))
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val orderedLessons = lessons.mapIndexed { i, lesson ->
                                val bell = if (i < bells.size) bells[i] else BellTime(0, "??", "??")
                                lesson.copy(time = bell.start, end = bell.end)
                            }
                            val fullSchedule = scheduleManager.getSchedule().toMutableMap()
                            fullSchedule[dayKey] = orderedLessons
                            scheduleManager.saveSchedule(fullSchedule)
                        }
                        Toast.makeText(context, Tr.get("saved", lang), Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .height(56.dp), // Висота 56.dp
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = Tr.get("save", lang),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp // Додай fontSize сюди
                )
            }
        }
    }
}
