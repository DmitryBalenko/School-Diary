package com.example.schooldiary.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.AudioRecorder
import com.bymrd1mm.schooldiary.GeminiClient
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.bymrd1mm.schooldiary.SimpleAudioPlayer
import com.bymrd1mm.schooldiary.getNextLessonDate
import com.bymrd1mm.schooldiary.getNextLessonDay
import com.bymrd1mm.schooldiary.saveAudioToInternalStorage
import com.bymrd1mm.schooldiary.saveImageToInternalStorage
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Homework
import com.example.schooldiary.RedDelete
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.daysOrder
import com.example.schooldiary.ui.components.AsyncImagePreview
import com.example.schooldiary.ui.components.RecordingVisualizer
import com.example.schooldiary.ui.components.SimpleHeader
import com.example.schooldiary.ui.components.TelegramAudioPlayer
import com.example.schooldiary.ui.components.formatSeconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteHomeworkScreen(
    navController: NavController,
    hwManager: HomeworkManager,
    scheduleManager: ScheduleManager,
    settingsManager: SettingsManager,
    subject: String,
    icon: String,
    lang: String
) {
    var text by remember { mutableStateOf("") }
    val selectedImages = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var isTranscribing by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val apiKey = settingsManager.apiKey

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current // Для вібрації при довгому натисканні

    // --- СТАН ДЛЯ ВИБОРУ ДНЯ ---
    var showDaySelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Визначаємо стан клавіатури
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Логіка видимості аудіо
    val showAudioPlayer by remember(selectedImages.size, audioFile, isKeyboardOpen) {
        derivedStateOf {
            if (audioFile == null) false
            else if (selectedImages.isNotEmpty() && isKeyboardOpen) false
            else true
        }
    }

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
        }
    }

    // Хелпер для обчислення дати примусово обраного дня
    fun getNextDateForDayKey(targetDayName: String): String {
        val today = LocalDate.now()
        for (i in 1..14) {
            val checkDate = today.plusDays(i.toLong())
            if (checkDate.dayOfWeek.name == targetDayName) {
                return checkDate.toString()
            }
        }
        return today.toString()
    }

    // --- ОНОВЛЕНА ФУНКЦІЯ ЗБЕРЕЖЕННЯ ---
    // Тепер приймає параметр manualDayKey (якщо обрали день вручну)
    fun saveHomeworkToDate(manualDayKey: String? = null) {
        if (isRecording) {
            audioRecorder.stopRecording()
            isRecording = false
        }

        // Якщо день обрано вручну - беремо його, якщо ні - шукаємо по розкладу
        val targetDayKey = manualDayKey ?: getNextLessonDay(subject, scheduleManager.getSchedule())
        val targetDayName = Tr.get(targetDayKey, lang)

        val targetDateReal = if (manualDayKey != null) {
            getNextDateForDayKey(manualDayKey)
        } else {
            getNextLessonDate(subject, scheduleManager.getSchedule())
        }

        val savedAudioPath = audioFile?.let { saveAudioToInternalStorage(context, it) }

        hwManager.saveHomework(
            Homework(
                id = System.currentTimeMillis(),
                subject = subject,
                text = text,
                imagePaths = selectedImages.toList(),
                date = LocalDate.now().toString(),
                icon = icon,
                targetDay = targetDayName,
                audioPath = savedAudioPath,
                targetDate = targetDateReal
            )
        )
        Toast.makeText(context, "${Tr.get("saved", lang)} -> $targetDateReal", Toast.LENGTH_SHORT)
            .show()
        navController.navigate("home") { popUpTo("home") { inclusive = true } }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                recordingSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(100)
            }
        } else {
            recordingSeconds = 0
        }
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempPhotoUri != null) {
                val savedPath = saveImageToInternalStorage(context, tempPhotoUri!!)
                if (savedPath != null) selectedImages.add(savedPath)
            }
        }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            uris.forEach { uri ->
                val savedPath = saveImageToInternalStorage(context, uri)
                if (savedPath != null) selectedImages.add(savedPath)
            }
        }
    val launchCamera = {
        try {
            val photoFile = File(context.cacheDir, "hw_photo_${System.currentTimeMillis()}.jpg")
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, photoFile)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Помилка запуску: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) launchCamera() else Toast.makeText(
                context,
                "Потрібен дозвіл!",
                Toast.LENGTH_LONG
            ).show()
        }
    val audioPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isRecording = true
                audioFile = audioRecorder.startRecording()
            } else {
                Toast.makeText(context, "Потрібен дозвіл на мікрофон!", Toast.LENGTH_LONG).show()
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBg)
                .statusBarsPadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            SimpleHeader(navController, Tr.get("new_task", lang))
            if (isTranscribing) LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Zinc800
            )

            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                subject,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(Tr.get("recording_new", lang), color = Zinc500, fontSize = 12.sp)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .animateContentSize(animationSpec = tween(400))
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { focusManager.clearFocus() })
                            }
                    ) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                            cursorBrush = SolidColor(Color.White),
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            decorationBox = { inner ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (text.isEmpty()) Text(
                                        Tr.get("write_or_dictate", lang),
                                        color = Zinc500,
                                        fontSize = 16.sp
                                    )
                                    inner()
                                }
                            }
                        )

                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectedImages) { path ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImagePreview(path)
                                        IconButton(
                                            onClick = { selectedImages.remove(path) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(Color.Black.copy(0.6f), CircleShape)
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val contentState = when {
                            isRecording -> "RECORDING"
                            showAudioPlayer -> "PLAYER"
                            else -> "HIDDEN"
                        }

                        AnimatedContent(
                            targetState = contentState,
                            transitionSpec = {
                                if (targetState == "HIDDEN") {
                                    EnterTransition.None togetherWith fadeOut(tween(200))
                                } else {
                                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                                }
                            },
                            label = "AudioSection"
                        ) { state ->
                            when (state) {
                                "RECORDING" -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            formatSeconds(recordingSeconds),
                                            color = RedDelete,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        RecordingVisualizer(isRecording = true)
                                    }
                                }

                                "PLAYER" -> {
                                    if (audioFile != null) {
                                        Column {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Zinc800, RoundedCornerShape(12.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TelegramAudioPlayer(
                                                        isPlaying = isPlayingPreview,
                                                        onPlayPause = {
                                                            if (isPlayingPreview) {
                                                                SimpleAudioPlayer.stop()
                                                                isPlayingPreview = false
                                                            } else {
                                                                isPlayingPreview = true
                                                                audioFile?.let {
                                                                    SimpleAudioPlayer.play(
                                                                        it.absolutePath
                                                                    ) { isPlayingPreview = false }
                                                                }
                                                            }
                                                        },
                                                        onTranscribe = {
                                                            if (apiKey.isBlank()) {
                                                                Toast.makeText(
                                                                    context,
                                                                    Tr.get("no_api_key", lang),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                return@TelegramAudioPlayer
                                                            }
                                                            isTranscribing = true
                                                            coroutineScope.launch {
                                                                audioFile?.let {
                                                                    val transcript =
                                                                        GeminiClient.transcribeAudio(
                                                                            it,
                                                                            apiKey
                                                                        )
                                                                    isTranscribing = false
                                                                    if (!transcript.isNullOrBlank()) {
                                                                        val separator =
                                                                            if (text.isNotEmpty()) " " else ""
                                                                        text += separator + transcript
                                                                    } else {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Не розпізнано",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        isTranscribing = isTranscribing,
                                                        lang = lang,
                                                        isSaved = false
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    SimpleAudioPlayer.stop(); audioFile = null
                                                }) {
                                                    Icon(Icons.Default.Close, null, tint = Zinc500)
                                                }
                                            }
                                        }
                                    }
                                }

                                "HIDDEN" -> {}
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlackBg)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) launchCamera() else permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Zinc700)
                    ) { Icon(Icons.Outlined.PhotoCamera, null) }
                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Zinc700)
                    ) { Icon(Icons.Outlined.Image, null) }
                    Button(
                        onClick = {
                            if (isRecording) {
                                isRecording = false
                                audioRecorder.stopRecording()
                            } else {
                                if (audioFile == null) {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        isRecording = true
                                        audioFile = audioRecorder.startRecording()
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    Toast.makeText(context, "Аудіо вже є", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) RedDelete.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = if (isRecording) RedDelete else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isRecording) BorderStroke(
                            1.dp,
                            RedDelete
                        ) else BorderStroke(1.dp, Zinc700),
                        enabled = audioFile == null || isRecording
                    ) {
                        Icon(if (isRecording) Icons.Filled.Stop else Icons.Outlined.Mic, null)
                    }

                    val canSave =
                        text.isNotBlank() || selectedImages.isNotEmpty() || audioFile != null
                    val saveBtnBg by animateColorAsState(
                        targetValue = if (canSave) Color.White else Color.Transparent,
                        animationSpec = tween(300),
                        label = "bg"
                    )
                    val saveBtnIconColor by animateColorAsState(
                        targetValue = if (canSave) Color.Black else Zinc700,
                        animationSpec = tween(300),
                        label = "icon"
                    )
                    val saveBtnBorderColor by animateColorAsState(
                        targetValue = if (canSave) Color.Transparent else Zinc700.copy(
                            alpha = 0.3f
                        ), animationSpec = tween(300), label = "border"
                    )

                    // --- ОНОВЛЕНА КНОПКА ЗБЕРЕЖЕННЯ (ПІДТРИМУЄ LONG CLICK) ---
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(saveBtnBg)
                            .border(1.dp, saveBtnBorderColor, RoundedCornerShape(16.dp))
                            .combinedClickable(
                                enabled = canSave,
                                onClick = { saveHomeworkToDate(null) }, // Звичайний клік (авто-розрахунок)
                                onLongClick = {
                                    // Довгий клік (ручний вибір дня)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    focusManager.clearFocus()
                                    showDaySelector = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Check, null, tint = saveBtnIconColor)
                    }
                }
            }
        }

        // --- ДІАЛОГ ВИБОРУ ДНЯ (ВІДКРИВАЄТЬСЯ ПРИ LONG PRESS) ---
        if (showDaySelector) {
            ModalBottomSheet(
                onDismissRequest = { showDaySelector = false },
                sheetState = sheetState,
                containerColor = Zinc900,
                contentColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = Tr.get("choose_day", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Список днів (Понеділок - П'ятниця)
                    daysOrder.forEach { dayKey ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showDaySelector = false
                                    saveHomeworkToDate(dayKey) // Зберігаємо на обраний день
                                },
                            colors = CardDefaults.cardColors(containerColor = Zinc800)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = Tr.get(dayKey, lang),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Zinc500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


