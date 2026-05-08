package com.example.schooldiary.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.MySpaceManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.SimpleAudioPlayer
import com.example.schooldiary.BlackBg
import com.example.schooldiary.FileType
import com.example.schooldiary.RedDelete
import com.example.schooldiary.SpaceFile
import com.example.schooldiary.SubjectNote
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.AddSubjectDialog
import com.example.schooldiary.ui.components.ImageViewer
import com.example.schooldiary.ui.components.SimpleHeader
import com.example.schooldiary.ui.components.SubjectSpaceCard
import com.example.schooldiary.ui.components.SwipeToRevealCard
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MySpaceScreen(
    navController: NavController,
    mySpaceManager: MySpaceManager,
    scheduleManager: ScheduleManager,
    lang: String
) {
    val subjects =
        remember { mutableStateListOf<String>().apply { addAll(mySpaceManager.getActiveSubjects()) } }

    val itemsPendingRemoval = remember { mutableStateListOf<String>() }

    var showAddDialog by remember { mutableStateOf(false) }
    var subjectPendingDelete by remember { mutableStateOf<String?>(null) }

    var activeResetAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val scope = rememberCoroutineScope()
    val allLessons = remember { scheduleManager.getSchedule().values.flatten() }
    val haptic = LocalHapticFeedback.current

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        subjects.apply {
            add(to.index, removeAt(from.index))
        }
    }, onDragEnd = { _, _ ->
        mySpaceManager.saveSubjectsOrder(subjects.toList())
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
    ) {
        if (subjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Tr.get("empty_space", lang), color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                state = state.listState,
                contentPadding = PaddingValues(
                    top = 110.dp,
                    bottom = 100.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
            ) {
                items(items = subjects, key = { it }) { subject ->
                    ReorderableItem(state, key = subject) { isDragging ->

                        val isBeingRemoved = itemsPendingRemoval.contains(subject)
                        val visibilityState = remember { MutableTransitionState(true) }.apply {
                            targetState = !isBeingRemoved
                        }

                        if (!visibilityState.targetState && visibilityState.isIdle) {
                            LaunchedEffect(subject) {
                                mySpaceManager.removeSubject(subject)
                                subjects.remove(subject)
                                itemsPendingRemoval.remove(subject)
                            }
                        }

                        val elevation by animateDpAsState(
                            if (isDragging) 16.dp else 0.dp,
                            label = "shadow"
                        )
                        val scale by animateFloatAsState(
                            if (isDragging) 1.05f else 1f,
                            label = "scale"
                        )
                        val zIndex = if (isDragging) 50f else 0f

                        val iconStr = allLessons.find { it.subject == subject }?.icon ?: "📁"
                        val revealState = remember { Animatable(0f) }

                        LaunchedEffect(isDragging) {
                            if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        AnimatedVisibility(
                            visibleState = visibilityState,
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(
                                animationSpec = tween(300)
                            ),
                            enter = expandVertically() + fadeIn()
                        ) {
                            Box(
                                modifier = Modifier
                                    .zIndex(zIndex)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation.toPx()
                                        shape = RoundedCornerShape(24.dp)
                                        clip = false
                                        alpha = 1f
                                    }
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .detectReorderAfterLongPress(state)
                            ) {
                                SwipeToRevealCard(
                                    offsetAnim = revealState,
                                    onDeleteClick = {
                                        subjectPendingDelete = subject
                                        activeResetAction =
                                            { scope.launch { revealState.animateTo(0f) } }
                                    }
                                ) {
                                    SubjectSpaceCard(subject = subject, icon = iconStr, onClick = {
                                        if (!isDragging) navController.navigate(
                                            "subject_space/${
                                                Uri.encode(
                                                    subject
                                                )
                                            }"
                                        )
                                    })
                                }
                            }
                        }
                    }
                }
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
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .zIndex(61f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = Tr.get("my_space", lang),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        if (showAddDialog) {
            AddSubjectDialog(
                scheduleManager = scheduleManager,
                onDismiss = { showAddDialog = false },
                onSubjectSelected = {
                    mySpaceManager.addSubject(it)
                    subjects.clear()
                    subjects.addAll(mySpaceManager.getActiveSubjects())
                    showAddDialog = false
                },
                lang = lang
            )
        }

        AnimatedVisibility(
            visible = subjectPendingDelete != null,
            enter = fadeIn(animationSpec = tween(440)),
            exit = fadeOut(animationSpec = tween(440)),
            modifier = Modifier
                .zIndex(100f)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        subjectPendingDelete = null
                        activeResetAction?.invoke()
                    }
            )
        }

        AnimatedVisibility(
            visible = subjectPendingDelete != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(
                animationSpec = tween(300)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(101f)
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Tr.get("delete_folder", lang),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                subjectPendingDelete = null
                                activeResetAction?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F46))
                        ) {
                            Text(Tr.get("cancel", lang), color = Color.White)
                        }

                        Button(
                            onClick = {
                                val itemToDelete = subjectPendingDelete!!
                                itemsPendingRemoval.add(itemToDelete)
                                subjectPendingDelete = null
                                activeResetAction?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(Tr.get("delete", lang), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectSpaceScreen(
    navController: NavController,
    mySpaceManager: MySpaceManager,
    subjectName: String,
    lang: String
) {
    val context = LocalContext.current
    val noteManager = remember { MySpaceManager.NoteManager(context) }

    var files by remember { mutableStateOf(mySpaceManager.getFilesForSubject(subjectName)) }
    var notes by remember { mutableStateOf(noteManager.getNotes(subjectName)) }

    var viewingImages by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var playingAudioId by remember { mutableStateOf<String?>(null) }
    var fileToDelete by remember { mutableStateOf<SpaceFile?>(null) }
    var noteToDelete by remember { mutableStateOf<SubjectNote?>(null) }

    var isFabExpanded by remember { mutableStateOf(false) }
    var showNoteCreator by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                var addedCount = 0
                uris.forEach { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                    }; if (mySpaceManager.saveFileToSubject(subjectName, uri)) addedCount++
                }
                if (addedCount > 0) {
                    files = mySpaceManager.getFilesForSubject(subjectName); Toast.makeText(
                        context,
                        "Додано: $addedCount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    fun openFile(file: SpaceFile) {
        when (file.type) {
            FileType.IMAGE -> {
                val imageFiles = files.filter { it.type == FileType.IMAGE }.map { it.path }
                val index = imageFiles.indexOf(file.path).takeIf { it >= 0 } ?: 0
                viewingImages = Pair(imageFiles, index)
            }

            FileType.AUDIO -> {
                if (playingAudioId == file.path) {
                    SimpleAudioPlayer.stop(); playingAudioId = null
                } else {
                    playingAudioId = file.path; SimpleAudioPlayer.play(file.path) {
                        playingAudioId = null
                    }
                }
            }

            else -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                File(file.path)
                            ),
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(File(file.path).extension.lowercase())
                                ?: "*/*"
                        ); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }; context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Немає додатку", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareFile(file: SpaceFile) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"; putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(file.path)
                )
            ); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }; context.startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
        }
    }

    DisposableEffect(Unit) { onDispose { SimpleAudioPlayer.stop() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
    ) {
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabExpanded = false }
                    .zIndex(10f)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            SimpleHeader(navController, subjectName)

            if (files.isEmpty() && notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.FolderOpen,
                            null,
                            tint = Zinc500,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Tr.get("no_materials", lang), color = Zinc500)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (notes.isNotEmpty()) {
                        item(key = "notes_header") {
                            Text(
                                text = Tr.get("notes", lang),
                                color = Zinc500,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .animateItemPlacement(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
                                    .padding(bottom = 4.dp)
                            )
                        }
                        items(items = notes, key = { it.id }) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            clipboardManager.setText(AnnotatedString(note.content))
                                            Toast.makeText(context, Tr.get("copied", lang), Toast.LENGTH_SHORT).show()
                                        }
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Zinc800),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.date,
                                            color = Zinc500,
                                            fontSize = 12.sp
                                        )
                                        IconButton(
                                            onClick = { noteToDelete = note },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Zinc500,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = note.content,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    if (files.isNotEmpty()) {
                        item(key = "files_header") {
                            Text(
                                text = Tr.get("files", lang),
                                color = Zinc500,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .animateItemPlacement(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
                                    .padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                        items(items = files, key = { it.path }) { file ->
                            val isPlaying = playingAudioId == file.path
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { openFile(file) },
                                colors = CardDefaults.cardColors(containerColor = Zinc900),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Zinc800, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            when (file.type) {
                                                FileType.IMAGE -> Icons.Outlined.Image
                                                FileType.AUDIO -> if (isPlaying) Icons.Filled.Stop else Icons.Outlined.Audiotrack
                                                FileType.PDF -> Icons.Outlined.PictureAsPdf
                                                FileType.DOC -> Icons.Outlined.InsertDriveFile
                                                else -> Icons.Outlined.InsertDriveFile
                                            }, null, tint = if (isPlaying) RedDelete else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            file.name,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text("${file.sizeStr} • ${file.date}", color = Zinc500, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { shareFile(file) }) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Share",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = { fileToDelete = file }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = Zinc500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .zIndex(11f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { 50 }) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Zinc900.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Tr.get("create_note", lang),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Zinc800, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    isFabExpanded = false
                                    showNoteCreator = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.EditNote, contentDescription = null, tint = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Zinc900.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Tr.get("upload_file", lang),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Zinc800, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    isFabExpanded = false
                                    filePicker.launch(arrayOf("*/*"))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                containerColor = BlackBg,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                val rotation by animateFloatAsState(if (isFabExpanded) 45f else 0f)
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }

        if (showNoteCreator) {
            var noteContent by remember { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            AlertDialog(
                onDismissRequest = { showNoteCreator = false },
                containerColor = Zinc900,
                title = { Text(Tr.get("create_note", lang), color = Color.White) },
                text = {
                    BasicTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Zinc800, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            if (noteContent.isEmpty()) Text(Tr.get("note_text", lang), color = Zinc500)
                            innerTextField()
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (noteContent.isNotBlank()) {
                            val newNote = SubjectNote(
                                id = System.currentTimeMillis(),
                                subject = subjectName,
                                content = noteContent,
                                date = LocalDate.now().toString()
                            )
                            noteManager.saveNote(newNote)
                            notes = noteManager.getNotes(subjectName)
                        }
                        showNoteCreator = false
                    }) {
                        Text(Tr.get("save", lang), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteCreator = false }) {
                        Text(Tr.get("cancel", lang), color = Zinc500)
                    }
                }
            )
        }

        if (viewingImages != null) {
            ImageViewer(viewingImages!!.first, viewingImages!!.second) { viewingImages = null }
        }

        if (fileToDelete != null) {
            AlertDialog(
                onDismissRequest = { fileToDelete = null },
                title = { Text(Tr.get("delete", lang) + "?", color = Color.White) },
                text = { Text(fileToDelete!!.name, color = Zinc500) },
                containerColor = Zinc900,
                confirmButton = {
                    TextButton(onClick = {
                        if (playingAudioId == fileToDelete!!.path) SimpleAudioPlayer.stop()
                        mySpaceManager.deleteFile(fileToDelete!!.path)
                        files = mySpaceManager.getFilesForSubject(subjectName)
                        fileToDelete = null
                    }) { Text(Tr.get("delete", lang), color = RedDelete) }
                },
                dismissButton = {
                    TextButton(onClick = { fileToDelete = null }) {
                        Text(Tr.get("cancel", lang), color = Color.White)
                    }
                }
            )
        }

        if (noteToDelete != null) {
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text(Tr.get("delete", lang) + "?", color = Color.White) },
                text = { Text(Tr.get("create_note", lang), color = Zinc500) },
                containerColor = Zinc900,
                confirmButton = {
                    TextButton(onClick = {
                        noteManager.deleteNote(subjectName, noteToDelete!!.id)
                        notes = noteManager.getNotes(subjectName)
                        noteToDelete = null
                    }) { Text(Tr.get("delete", lang), color = RedDelete) }
                },
                dismissButton = {
                    TextButton(onClick = { noteToDelete = null }) {
                        Text(Tr.get("cancel", lang), color = Color.White)
                    }
                }
            )
        }
    }
}