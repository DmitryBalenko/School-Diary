package com.example.schooldiary.ui.screens.hwscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bymrd1mm.schooldiary.GeminiClient
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.bymrd1mm.schooldiary.SimpleAudioPlayer
import com.bymrd1mm.schooldiary.getNextLessonDay
import com.example.schooldiary.Homework
import com.example.schooldiary.Lesson
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.AsyncImagePreview
import com.example.schooldiary.ui.components.ImageViewer
import com.example.schooldiary.ui.components.TelegramAudioPlayer
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListViewContent(
    hwList: MutableList<Homework>,
    currentSchedule: Map<String, List<Lesson>>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTransfer: (Homework) -> Unit,
    onTransferCustom: (Homework) -> Unit
) {
    val displayItems by remember(hwList, currentSchedule) {
        derivedStateOf {
            hwList.filter { !it.isArchived }.map { hw ->
                val tDateStr = hw.targetDate?.trim() ?: ""
                var isExtra = false
                var cleanDateStr = ""
                if (tDateStr.isNotEmpty()) {
                    try {
                        val cleanStr =
                            if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(
                                " "
                            )
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
            ) { (hw, isExtra, cleanDateStr) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(20.dp),
                    border = if (isExtra) BorderStroke(1.dp, Color.White) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
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
                            if (hw.icon.isNotBlank() && hw.icon != "📌") {
                                Text(hw.icon, fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                            }
                            Column {
                                Text(
                                    hw.subject,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )

                                val dateDisplay = try {
                                    val date = LocalDate.parse(cleanDateStr)
                                    val formatter =
                                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.YYYY")
                                    " (${date.format(formatter)})"
                                } catch (e: Exception) {
                                    ""
                                }

                                Text(
                                    "${Tr.get("recorded_on", lang)} ${
                                        Tr.get(
                                            hw.targetDay,
                                            lang
                                        )
                                    }$dateDisplay",
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
                            val nextDayKey =
                                getNextLessonDay(hw.subject, currentSchedule, isTransfer = true)
                            val nextDayTranslated = Tr.get(nextDayKey, lang)

                            val textWithoutEmoji =
                                Tr.get("overdue_transfer", lang).replace("⚠️ ", "")
                                    .replace("⚠️", "")

                            val buttonText = if (nextDayKey == "UNKNOWN") {
                                Tr.get("overdue_pick_date", lang)
                            } else {
                                "$textWithoutEmoji $nextDayTranslated?"
                            }

                            OutlinedButton(
                                onClick = {
                                    if (nextDayKey == "UNKNOWN") onTransferCustom(hw) else onTransfer(
                                        hw
                                    )
                                },
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
                                    text = buttonText,
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
    currentSchedule: Map<String, List<Lesson>>,
    playingAudioId: Long?,
    transcribingId: Long?,
    onPlayAudio: (Long, String) -> Unit,
    onTranscribe: (Long, String, String) -> Unit,
    onDeleteHw: (Long) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
    onTransferCustom: (Homework) -> Unit,
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

            homeworks.forEach { hw ->
                if (isOverdue(hw) && getNextLessonDay(
                        hw.subject,
                        currentSchedule,
                        isTransfer = true
                    ) == "UNKNOWN"
                ) {
                    OutlinedButton(
                        onClick = { onTransferCustom(hw) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(
                                alpha = 0.7f
                            )
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(Tr.get("overdue_pick_date", lang), textAlign = TextAlign.Center)
                    }
                }
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