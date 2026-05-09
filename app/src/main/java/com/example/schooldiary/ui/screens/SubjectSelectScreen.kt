package com.example.schooldiary.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.ScheduleManager
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import java.text.Collator
import java.util.Locale

enum class SubjectViewMode { GRID, LIST }

@Composable
fun RoundedOutlinedGridIcon(tint: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        val boxSize = 7.dp.toPx()
        val cornerRadius = CornerRadius(2.5f.dp.toPx(), 2.5f.dp.toPx())

        val offset1 = 3.5f.dp.toPx()
        val offset2 = 13.5f.dp.toPx()

        val style = Stroke(width = strokeWidth)

        drawRoundRect(
            color = tint,
            topLeft = Offset(offset1, offset1),
            size = Size(boxSize, boxSize),
            cornerRadius = cornerRadius,
            style = style
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(offset2, offset1),
            size = Size(boxSize, boxSize),
            cornerRadius = cornerRadius,
            style = style
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(offset1, offset2),
            size = Size(boxSize, boxSize),
            cornerRadius = cornerRadius,
            style = style
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(offset2, offset2),
            size = Size(boxSize, boxSize),
            cornerRadius = cornerRadius,
            style = style
        )
    }
}

@Composable
fun SubjectSelectScreen(
    navController: NavController,
    scheduleManager: ScheduleManager,
    lang: String
) {
    val context = LocalContext.current

    val collator = remember(lang) {
        val locale = when (lang) {
            "ua" -> Locale("uk", "UA")
            "ru" -> Locale("ru", "RU")
            else -> Locale.ENGLISH
        }
        Collator.getInstance(locale)
    }

    val subjects = remember {
        scheduleManager.getUniqueSubjects(scheduleManager.getSchedule())
            .sortedWith(compareBy(collator) { it.subject })
    }

    val prefs = remember { context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE) }
    var viewMode by remember {
        mutableStateOf(
            try {
                SubjectViewMode.valueOf(
                    prefs.getString(
                        "subject_view_mode",
                        SubjectViewMode.GRID.name
                    ) ?: SubjectViewMode.GRID.name
                )
            } catch (e: Exception) {
                SubjectViewMode.GRID
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
    ) {
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { 50 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(200)) { -50 })
            },
            label = "ContentModeToggle",
            modifier = Modifier.fillMaxSize()
        ) { mode ->
            if (mode == SubjectViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        top = 100.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 40.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(subjects) { lesson ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black)
                                .clickable {
                                    navController.navigate(
                                        "write_hw/${Uri.encode(lesson.subject)}/${
                                            Uri.encode(
                                                lesson.icon
                                            )
                                        }"
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Zinc900),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(lesson.icon, fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    lesson.subject,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    // Картка "Інше завдання"
                    item {
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black)
                                .clickable { navController.navigate("write_hw/CUSTOM_SUBJECT/EMPTY_ICON") },
                            colors = CardDefaults.cardColors(containerColor = BlackBg),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Zinc700)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    Tr.get("custom_subject", lang),
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 100.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(subjects) { lesson ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                                .clickable {
                                    navController.navigate(
                                        "write_hw/${Uri.encode(lesson.subject)}/${
                                            Uri.encode(
                                                lesson.icon
                                            )
                                        }"
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Zinc900),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lesson.icon, fontSize = 32.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = lesson.subject,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Zinc500
                                )
                            }
                        }
                    }
                    // Елемент "Інше завдання"
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                                .clickable { navController.navigate("write_hw/CUSTOM_SUBJECT/EMPTY_ICON") },
                            colors = CardDefaults.cardColors(containerColor = BlackBg),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Zinc700)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = Tr.get("custom_subject", lang),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Zinc500
                                )
                            }
                        }
                    }
                }
            }
        }

        // ВЕРХНІЙ ГРАДІЄНТ ТА ХЕДЕР
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg,
                            BlackBg,
                            BlackBg.copy(alpha = 0.9f),
                            BlackBg.copy(alpha = 0.6f),
                            BlackBg.copy(alpha = 0.3f),
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Tr.get("choose_subject", lang),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    val newMode =
                        if (viewMode == SubjectViewMode.GRID) SubjectViewMode.LIST else SubjectViewMode.GRID
                    viewMode = newMode
                    prefs.edit().putString("subject_view_mode", newMode.name).apply()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Zinc800, CircleShape)
                    .clip(CircleShape)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(
                            tween(300),
                            0.8f
                        )).togetherWith(fadeOut(tween(300)) + scaleOut(tween(300), 0.8f))
                    },
                    label = "ViewModeToggle"
                ) { mode ->
                    if (mode == SubjectViewMode.GRID) Icon(
                        Icons.Outlined.FormatListBulleted,
                        contentDescription = null,
                        tint = Color.White
                    )
                    else RoundedOutlinedGridIcon(tint = Color.White)
                }
            }
        }
    }
}