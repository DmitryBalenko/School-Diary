package com.example.schooldiary.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.getSubjectThemeColor
import com.example.schooldiary.BellTime
import com.example.schooldiary.BlueAction
import com.example.schooldiary.RedDelete
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.availableEmojis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), content = content
        )
    }
}

@Composable
fun SimpleHeader(navController: NavController, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp)); Text(
        title,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    }
}

@Composable
fun TextAnimated(
    text: String,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
        },
        label = "textAnim"
    ) { targetText ->
        Text(
            text = targetText,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            modifier = modifier
        )
    }
}

@Composable
fun ExportCheckboxItem(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Zinc500,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun TimeEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onFocus: () -> Unit = {}
) {
    val digits = value.filter { it.isDigit() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Zinc500,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        BasicTextField(
            value = digits,
            onValueChange = { newRaw ->
                val filtered = newRaw.filter { it.isDigit() }.take(4)
                val formatted = if (filtered.length >= 3) {
                    filtered.substring(0, 2) + ":" + filtered.substring(2)
                } else {
                    filtered
                }
                onValueChange(formatted)
            },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            ),
            cursorBrush = SolidColor(BlueAction),
            modifier = Modifier
                .fillMaxWidth()
                .background(Zinc800, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp, horizontal = 0.dp)
                .onFocusChanged { if (it.isFocused) onFocus() },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = TimeVisualTransformation()
        )
    }
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trim = text.text
        val out = StringBuilder()
        for (i in trim.indices) {
            out.append(trim[i]); if (i == 1) out.append(":")
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset; if (offset <= 4) return offset + 1; return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset; if (offset <= 5) return offset - 1; return 4
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

@Composable
fun BellCardItem(
    index: Int,
    bell: BellTime,
    lang: String,
    onBellChange: (BellTime) -> Unit,
    onFocusGained: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${index + 1}.",
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.width(30.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                TimeEditField(
                    value = bell.start,
                    onValueChange = { onBellChange(bell.copy(start = it)) },
                    label = Tr.get("start", lang),
                    onFocus = onFocusGained
                )
            }

            Text(
                "—",
                color = Zinc500,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Light
            )

            Box(modifier = Modifier.weight(1f)) {
                TimeEditField(
                    value = bell.end,
                    onValueChange = { onBellChange(bell.copy(end = it)) },
                    label = Tr.get("end", lang),
                    onFocus = onFocusGained
                )
            }
        }
    }
}

@Composable
fun SwipeToRevealCard(
    offsetAnim: Animatable<Float, AnimationVector1D>,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val anchorWidthPx = with(density) { 85.dp.toPx() }
    val triggerThresholdPx = anchorWidthPx * 1.5f

    val offset = offsetAnim.value
    val revealedWidth = offset.absoluteValue.coerceAtLeast(0f)
    val isPastThreshold = revealedWidth >= triggerThresholdPx
    var isDeleteConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(isPastThreshold) {
        if (isPastThreshold) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Сброс состояния удаления, когда карточка возвращается в исходное положение
    LaunchedEffect(offsetAnim.value) {
        if (offsetAnim.value == 0f) {
            isDeleteConfirmed = false
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPastThreshold || isDeleteConfirmed) Color(0xFFEF4444) else Color(
            0xFF3F3F46
        ),
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(with(density) { revealedWidth.toDp() })
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(color = backgroundColor)
                .clickable { if (isDeleteConfirmed) onDeleteClick() },
            contentAlignment = Alignment.Center
        ) {
            if (revealedWidth > 20f) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.scale(if (isPastThreshold || isDeleteConfirmed) 1.2f else 1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .clip(RoundedCornerShape(24.dp))
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val targetOffset = offsetAnim.value + delta
                            if (targetOffset <= 0) {
                                offsetAnim.snapTo(targetOffset)
                            }
                        }
                    },
                    onDragStopped = {
                        if (offsetAnim.value <= -triggerThresholdPx) {
                            isDeleteConfirmed = true
                            offsetAnim.animateTo(
                                -anchorWidthPx,
                                tween(300, easing = FastOutSlowInEasing)
                            )
                        } else {
                            isDeleteConfirmed = false
                            offsetAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
                        }
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun SubjectSpaceCard(subject: String, icon: String, onClick: () -> Unit) {
    val themeColor = remember(subject, icon) { getSubjectThemeColor(subject, icon) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(themeColor, Color.Black),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY
                        )
                    )
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.03f)
            ) {
                drawCircle(
                    color = Color.White,
                    radius = size.height * 1.2f,
                    center = Offset(size.width * 0.9f, size.height * 0.5f)
                ); drawLine(
                color = Color.White,
                start = Offset(0f, size.height),
                end = Offset(size.width * 0.3f, 0f),
                strokeWidth = 50f
            )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ); Spacer(modifier = Modifier.weight(1f)); Icon(
                Icons.Default.KeyboardArrowRight,
                null,
                tint = Zinc500,
                modifier = Modifier.size(24.dp)
            )
            }
        }
    }
}

@Composable
fun TelegramAudioPlayer(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onTranscribe: () -> Unit,
    isTranscribing: Boolean,
    lang: String,
    isSaved: Boolean = false
) {
    var durationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis() - (durationSeconds * 1000)
            while (isPlaying) {
                durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(100)
            }
        } else {
            durationSeconds = 0
        }
    }

    val backgroundColor = Color.Black.copy(alpha = 0.5f)
    val borderColor = if (isSaved) Color.White.copy(alpha = 0.1f) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color.White else Color.White.copy(alpha = 0.15f))
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isPlaying) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (isPlaying) {
                    PlaybackVisualizer()
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        formatSeconds(durationSeconds),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        Tr.get("audio", lang),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable { onTranscribe() },
            contentAlignment = Alignment.Center
        ) {
            if (isTranscribing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        tint = Color.White.copy(0.4f),
                        modifier = Modifier.size(10.dp)
                    )
                    Text("A", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageViewer(
    imagePaths: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val buttonBackground = Color(0xFF27272A).copy(alpha = 0.5f)

    val pagerState = rememberPagerState(initialPage = initialIndex) { imagePaths.size }

    val shareImage = {
        try {
            val currentPath = imagePaths.getOrNull(pagerState.currentPage)
            if (currentPath != null) {
                val file = File(currentPath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Поделиться фото"))
                } else {
                    Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при отправке", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imagePath = imagePaths[page]

                val scale = remember { mutableStateOf(1f) }
                val offset = remember { mutableStateOf(Offset.Zero) }

                // Сброс масштаба при перелистывании
                LaunchedEffect(pagerState.currentPage) {
                    if (page != pagerState.currentPage) {
                        scale.value = 1f
                        offset.value = Offset.Zero
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(imagePath))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                do {
                                    val event = awaitPointerEvent()
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()

                                    // Если пальцев > 1 (щипок) ИЛИ картинка уже увеличена: перехватываем жесты
                                    if (event.changes.size > 1 || scale.value > 1.01f) {
                                        scale.value = (scale.value * zoom).coerceIn(1f, 4f)

                                        if (scale.value > 1.01f) {
                                            val maxX = (screenWidth * (scale.value - 1)) / 2
                                            val maxY = (screenHeight * (scale.value - 1)) / 2
                                            val newX =
                                                (offset.value.x + pan.x).coerceIn(-maxX, maxX)
                                            val newY =
                                                (offset.value.y + pan.y).coerceIn(-maxY, maxY)
                                            offset.value = Offset(newX, newY)
                                        } else {
                                            offset.value = Offset.Zero
                                        }

                                        // "Съедаем" жест, чтобы Pager не получил его и не скроллился
                                        event.changes.forEach { it.consume() }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            translationX = offset.value.x
                            translationY = offset.value.y
                        }
                )
            }

            // Верхние элементы управления (Счетчик, Поделиться, Закрыть)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .systemBarsPadding()
                    .padding(top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imagePaths.size > 1) {
                    Box(
                        modifier = Modifier
                            .background(buttonBackground, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${imagePaths.size}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(
                    onClick = shareImage,
                    modifier = Modifier.background(buttonBackground, CircleShape)
                ) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(buttonBackground, CircleShape)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AsyncImagePreview(imagePath: String, onClick: () -> Unit = {}) {
    val context = LocalContext.current; AsyncImage(
        model = ImageRequest.Builder(context).data(File(imagePath)).crossfade(true).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() })
}

enum class ViewMode {
    WEEK, LIST
}

@Composable
fun RecordingVisualizer(isRecording: Boolean) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isRecording) {
            Box(
                Modifier
                    .height(2.dp)
                    .width(100.dp)
                    .background(Zinc500, CircleShape)
            )
        } else {
            val infiniteTransition =
                rememberInfiniteTransition(label = "wave"); repeat(10) { index ->
                val height by infiniteTransition.animateFloat(
                    initialValue = 4f,
                    targetValue = if (index % 2 == 0) 30f else 15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            300 + (index * 50),
                            easing = FastOutSlowInEasing
                        ), repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar$index"
                ); Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height(height.dp)
                    .background(RedDelete, CircleShape)
            )
            }
        }
    }
}

@Composable
fun PlaybackVisualizer() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val infiniteTransition = rememberInfiniteTransition(label = "play"); repeat(5) { index ->
        val height by infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = 16f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    300 + (index * 70),
                    easing = FastOutSlowInEasing
                ), repeatMode = RepeatMode.Reverse
            ),
            label = "playBar$index"
        ); Box(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .width(3.dp)
            .height(height.dp)
            .background(Color.White, CircleShape)
    )
    }
    }
}

fun formatSeconds(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60; return String.format("%02d:%02d", m, s)
}

@Composable
fun EmojiPickerDialog(onDismiss: () -> Unit, onEmojiSelected: (String) -> Unit, lang: String) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    Tr.get("choose_icon", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(availableEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Zinc800)
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) { Text(emoji, fontSize = 32.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    scheduleManager: ScheduleManager,
    onDismiss: () -> Unit,
    onSubjectSelected: (String) -> Unit,
    lang: String
) {
    val uniqueSubjects =
        remember { scheduleManager.getUniqueSubjects(scheduleManager.getSchedule()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    Tr.get("choose_subject", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ); Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uniqueSubjects) { lesson ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSubjectSelected(lesson.subject) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                lesson.icon,
                                fontSize = 20.sp
                            ); Spacer(modifier = Modifier.width(12.dp)); Text(
                            lesson.subject,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        }; Divider(color = Zinc800)
                    }
                }
            }
        }
    }
}