package com.example.schooldiary.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.*
import com.example.schooldiary.*
import com.example.schooldiary.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

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
    val haptic = LocalHapticFeedback.current

    var showDaySelector by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Стан для кастомного завдання
    val isCustomSubject = subject == "CUSTOM_SUBJECT"
    var customSubjectName by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val customInteractionSource = remember { MutableInteractionSource() }

    val showAudioPlayer by remember(selectedImages.size, audioFile, isKeyboardOpen) {
        derivedStateOf {
            if (audioFile == null) false
            else if (selectedImages.isNotEmpty() && isKeyboardOpen) false
            else true
        }
    }

    LaunchedEffect(isKeyboardOpen) { if (!isKeyboardOpen) focusManager.clearFocus() }

    fun getNextDateForDayKey(targetDayName: String): String {
        val today = LocalDate.now()
        for (i in 1..14) {
            val checkDate = today.plusDays(i.toLong())
            if (checkDate.dayOfWeek.name == targetDayName) return checkDate.toString()
        }
        return today.toString()
    }

    fun saveHomeworkToDate(manualDayKey: String? = null, exactDate: LocalDate? = null) {
        if (isRecording) { audioRecorder.stopRecording(); isRecording = false }

        val finalSubject = if (isCustomSubject) customSubjectName.trim() else subject
        val finalIcon = if (isCustomSubject) customIcon else icon

        val targetDayName: String
        val targetDateReal: String

        if (exactDate != null) {
            targetDateReal = exactDate.toString()
            targetDayName = Tr.get(exactDate.dayOfWeek.name, lang)
        } else {
            val targetDayKey = manualDayKey ?: getNextLessonDay(finalSubject, scheduleManager.getSchedule())
            targetDayName = Tr.get(targetDayKey, lang)
            targetDateReal = if (manualDayKey != null) getNextDateForDayKey(manualDayKey) else getNextLessonDate(finalSubject, scheduleManager.getSchedule())
        }

        val savedAudioPath = audioFile?.let { saveAudioToInternalStorage(context, it) }
        hwManager.saveHomework(Homework(id = System.currentTimeMillis(), subject = finalSubject, text = text, imagePaths = selectedImages.toList(), date = LocalDate.now().toString(), icon = finalIcon, targetDay = targetDayName, audioPath = savedAudioPath, targetDate = targetDateReal))
        Toast.makeText(context, "${Tr.get("saved", lang)} -> $targetDateReal", Toast.LENGTH_SHORT).show()
        navController.navigate("home") { popUpTo("home") { inclusive = true } }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) { recordingSeconds = (System.currentTimeMillis() - startTime) / 1000; delay(100) }
        } else { recordingSeconds = 0 }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success && tempPhotoUri != null) saveImageToInternalStorage(context, tempPhotoUri!!)?.let { selectedImages.add(it) } }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris -> uris.forEach { uri -> saveImageToInternalStorage(context, uri)?.let { selectedImages.add(it) } } }

    val launchCamera = {
        try {
            val photoFile = File(context.cacheDir, "hw_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) launchCamera() else Toast.makeText(context, "Потрібен дозвіл!", Toast.LENGTH_LONG).show() }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) { isRecording = true; audioFile = audioRecorder.startRecording() } else Toast.makeText(context, "Потрібен дозвіл!", Toast.LENGTH_LONG).show() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding().pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
            SimpleHeader(navController, Tr.get("new_task", lang))
            if (isTranscribing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = Zinc800)

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isCustomSubject) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Zinc800).clickable { showEmojiPicker = true }, contentAlignment = Alignment.Center) {
                                if (customIcon.isBlank()) Icon(Icons.Default.Add, null, tint = Zinc500) else Text(customIcon, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = customInteractionSource,
                                        indication = null
                                    ) {
                                        focusRequester.requestFocus()
                                    }
                            ) {
                                BasicTextField(
                                    value = customSubjectName,
                                    onValueChange = { customSubjectName = it },
                                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                    cursorBrush = SolidColor(Color.White),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    decorationBox = { inner ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (customSubjectName.isEmpty()) Text(Tr.get("custom_subject_hint", lang), color = Zinc500, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            inner()
                                        }
                                    }
                                )
                                Text(Tr.get("recording_new", lang), color = Zinc500, fontSize = 12.sp)
                            }
                        } else {
                            Text(icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(Tr.get("recording_new", lang), color = Zinc500, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp).animateContentSize(tween(400)).pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                            cursorBrush = SolidColor(Color.White),
                            interactionSource = interactionSource,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            decorationBox = { inner ->
                                Box {
                                    if (text.isEmpty()) Text(Tr.get("write_or_dictate", lang), color = Zinc500, fontSize = 16.sp)
                                    inner()
                                }
                            }
                        )

                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectedImages) { path ->
                                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))) {
                                        AsyncImagePreview(path)
                                        IconButton(onClick = { selectedImages.remove(path) }, modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.6f), CircleShape).size(20.dp)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                    }
                                }
                            }
                        }

                        val contentState = when {
                            isRecording -> "RECORDING"
                            showAudioPlayer -> "PLAYER"
                            else -> "HIDDEN"
                        }

                        AnimatedContent(targetState = contentState, transitionSpec = { if (targetState == "HIDDEN") EnterTransition.None togetherWith fadeOut(tween(200)) else fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }, label = "Audio") { state ->
                            when (state) {
                                "RECORDING" -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(formatSeconds(recordingSeconds), color = RedDelete, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RecordingVisualizer(isRecording = true)
                                }
                                "PLAYER" -> if (audioFile != null) Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Zinc800, RoundedCornerShape(12.dp)).padding(8.dp)) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            TelegramAudioPlayer(isPlaying = isPlayingPreview, onPlayPause = { if (isPlayingPreview) { SimpleAudioPlayer.stop(); isPlayingPreview = false } else { isPlayingPreview = true; audioFile?.let { SimpleAudioPlayer.play(it.absolutePath) { isPlayingPreview = false } } } }, onTranscribe = { if (apiKey.isBlank()) { Toast.makeText(context, Tr.get("no_api_key", lang), Toast.LENGTH_LONG).show(); return@TelegramAudioPlayer }; isTranscribing = true; coroutineScope.launch { audioFile?.let { val transcript = GeminiClient.transcribeAudio(it, apiKey); isTranscribing = false; if (!transcript.isNullOrBlank()) { text += (if (text.isNotEmpty()) " " else "") + transcript } else { Toast.makeText(context, "Не розпізнано", Toast.LENGTH_SHORT).show() } } } }, isTranscribing = isTranscribing, lang = lang, isSaved = false)
                                        }
                                        IconButton(onClick = { SimpleAudioPlayer.stop(); audioFile = null }) { Icon(Icons.Default.Close, null, tint = Zinc500) }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ПАНЕЛЬ КНОПОК
            Column(modifier = Modifier.fillMaxWidth().background(BlackBg).navigationBarsPadding().imePadding()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCamera() else permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc700)) { Icon(Icons.Outlined.PhotoCamera, null) }
                    Button(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc700)) { Icon(Icons.Outlined.Image, null) }
                    Button(onClick = { if (isRecording) { isRecording = false; audioRecorder.stopRecording() } else { if (audioFile == null) { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { isRecording = true; audioFile = audioRecorder.startRecording() } else audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) } } }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) RedDelete.copy(0.2f) else Color.Transparent, contentColor = if (isRecording) RedDelete else Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, if (isRecording) RedDelete else Zinc700), enabled = audioFile == null || isRecording) { Icon(if (isRecording) Icons.Filled.Stop else Icons.Outlined.Mic, null) }

                    val canSave = (!isCustomSubject || customSubjectName.isNotBlank()) && (text.isNotBlank() || selectedImages.isNotEmpty() || audioFile != null)
                    val saveBtnBg by animateColorAsState(if (canSave) Color.White else Color.Transparent, tween(300), label = "bg")
                    val saveBtnIconColor by animateColorAsState(if (canSave) Color.Black else Zinc700, tween(300), label = "icon")

                    Box(
                        modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(16.dp)).background(saveBtnBg).border(1.dp, if (canSave) Color.Transparent else Zinc700.copy(0.3f), RoundedCornerShape(16.dp)).combinedClickable(
                            enabled = canSave,
                            onClick = {
                                if (isCustomSubject) {
                                    focusManager.clearFocus()
                                    showDaySelector = true
                                } else {
                                    saveHomeworkToDate()
                                }
                            },
                            onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); focusManager.clearFocus(); showDaySelector = true }
                        ),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Check, null, tint = saveBtnIconColor) }
                }
            }
        }

        if (showDaySelector) {
            ModalBottomSheet(onDismissRequest = { showDaySelector = false }, sheetState = sheetState, containerColor = Zinc900, contentColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                    Text(Tr.get("choose_day", lang), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                    daysOrder.forEach { dayKey ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp)).clickable { showDaySelector = false; saveHomeworkToDate(manualDayKey = dayKey) }, colors = CardDefaults.cardColors(containerColor = Zinc800)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(Tr.get(dayKey, lang), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Icon(Icons.Default.ArrowForwardIos, null, tint = Zinc500, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp)).clickable { showDaySelector = false; showDatePicker = true }, colors = CardDefaults.cardColors(containerColor = Zinc800)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(Tr.get("pick_date", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { saveHomeworkToDate(exactDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; showDatePicker = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text(Tr.get("save", lang)) } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }, colors = ButtonDefaults.textButtonColors(contentColor = Zinc500)) { Text(Tr.get("cancel", lang)) } },
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

        if (showEmojiPicker) EmojiPickerDialog(onDismiss = { showEmojiPicker = false }, onEmojiSelected = { customIcon = it; showEmojiPicker = false }, lang = lang)
    }
}