package com.example.schooldiary.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.schooldiary.Zinc900

@Composable
fun SubjectSelectScreen(
    navController: NavController,
    scheduleManager: ScheduleManager,
    lang: String
) {
    val subjects = remember { scheduleManager.getUniqueSubjects(scheduleManager.getSchedule()) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BlackBg)
        .statusBarsPadding()) {
        if (subjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Tr.get("fill_schedule_first", lang), color = Zinc500)
            }
        } else {
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
                        // ПОВЕРНУТО МИНУЛИЙ КОЛІР (Zinc900)
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
            }
        }

        // ВЕРХНІЙ ГРАДІЄНТ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Однакова висота
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg,                    // Повністю чорний
                            BlackBg,                    // Чорний
                            BlackBg.copy(alpha = 0.9f), // Поступово прозоріший
                            BlackBg.copy(alpha = 0.6f),
                            BlackBg.copy(alpha = 0.3f),
                            Color.Transparent           // Вихід у нуль
                        )
                    )
                )
        )

        // ХЕДЕР
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        }
    }
}
