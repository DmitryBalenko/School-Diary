package com.example.schooldiary.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.SettingsManager
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.daysOrder
import com.example.schooldiary.ui.components.ExportCheckboxItem
import com.example.schooldiary.ui.components.TextAnimated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    currentLang: String,
    onLangChange: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val density = LocalDensity.current

    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    var apiKey by remember { mutableStateOf(settingsManager.apiKey) }
    val isUnsaved by remember { derivedStateOf { apiKey != settingsManager.apiKey } }

    var showExportMenu by remember { mutableStateOf(false) }
    var exportSchedule by remember { mutableStateOf(true) }
    var exportSettings by remember { mutableStateOf(false) }
    var exportSpace by remember { mutableStateOf(false) }
    var exportApi by remember { mutableStateOf(false) }
    var exportArchived by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- НАЛАШТУВАННЯ ПЛАВНОЇ ТА М'ЯКОЇ ПРУЖИНИ ---
    // Менший stiffness робить анімацію повільнішою і плавнішою
    val SMOOTH_STIFFNESS = 200f // Spring.StiffnessLow
    val SMOOTH_DAMPING = 0.9f // Майже без відскоку, просто м'яке гальмування

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            if (uri != null) {
                val fileUri = settingsManager.exportData(
                    exportSchedule,
                    exportSettings,
                    exportSpace,
                    exportApi,
                    exportArchived
                )
                if (fileUri != null) {
                    try {
                        context.contentResolver.openInputStream(fileUri)?.use { input ->
                            context.contentResolver.openOutputStream(uri)
                                ?.use { output -> input.copyTo(output) }
                        }
                        Toast.makeText(
                            context,
                            Tr.get("export_success", currentLang),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            Tr.get("error_save", currentLang),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                if (settingsManager.importData(uri)) {
                    Toast.makeText(
                        context,
                        Tr.get("import_success", currentLang),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, Tr.get("error_read", currentLang), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .statusBarsPadding()
            .imePadding()
            // Закриття клавіатури при натисканні на фон
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            // --- HEADER ТА ВЕРХНІ КАРТКИ ---
            AnimatedVisibility(
                visible = !isKeyboardOpen,
                enter = expandVertically(
                    animationSpec = spring(
                        stiffness = SMOOTH_STIFFNESS,
                        dampingRatio = SMOOTH_DAMPING
                    )
                ) +
                        fadeIn(animationSpec = tween(400)),
                exit = shrinkVertically(
                    animationSpec = spring(
                        stiffness = SMOOTH_STIFFNESS,
                        dampingRatio = SMOOTH_DAMPING
                    )
                ) +
                        fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    // 1. Заголовок і кнопки керування
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextAnimated(
                            text = Tr.get("settings", currentLang),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                val intent =
                                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                                context.startActivity(Intent.makeRestartActivityTask(intent?.component))
                                Runtime.getRuntime().exit(0)
                            },
                            modifier = Modifier
                                .background(Zinc800, CircleShape)
                                .size(40.dp)
                        ) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
                        Spacer(modifier = Modifier.width(12.dp))
                        val flag = when (currentLang) {
                            "ua" -> "🇺🇦"; "ru" -> "🇷🇺"; else -> "🇬🇧"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Zinc800)
                                .clickable {
                                    val nextLang = when (currentLang) {
                                        "ua" -> "ru"; "ru" -> "en"; else -> "ua"
                                    }
                                    settingsManager.currentLanguage = nextLang
                                    onLangChange(nextLang)
                                }
                                .padding(10.dp)
                        ) { TextAnimated(text = flag, fontSize = 20.sp) }
                    }

                    // 2. Картки розкладу та дзвінків
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("settings_bells") },
                            colors = CardDefaults.cardColors(containerColor = Zinc800),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "⏰",
                                    fontSize = 24.sp
                                ); Spacer(modifier = Modifier.width(16.dp)); Column {
                                TextAnimated(
                                    text = Tr.get(
                                        "bells",
                                        currentLang
                                    ), fontSize = 18.sp, fontWeight = FontWeight.Bold
                                ); TextAnimated(
                                text = Tr.get("lessons_and_bells", currentLang),
                                color = Zinc500,
                                fontSize = 12.sp
                            )
                            }
                            }
                        }
                        daysOrder.forEach { dayKey ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("edit_day/$dayKey") },
                                colors = CardDefaults.cardColors(containerColor = Zinc900),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextAnimated(
                                        text = Tr.get(dayKey, currentLang),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f)); Icon(
                                    Icons.Filled.Edit,
                                    null,
                                    tint = Zinc500
                                )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // --- БЛОК API ---
            val apiSpacerHeight by animateDpAsState(
                targetValue = if (isKeyboardOpen) 40.dp else 0.dp,
                animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING),
                label = "apiSpacerAnim"
            )
            Spacer(modifier = Modifier.height(apiSpacerHeight))

            Text(
                Tr.get("api_setup", currentLang),
                color = Zinc500,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            stiffness = SMOOTH_STIFFNESS,
                            dampingRatio = SMOOTH_DAMPING
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Text(Tr.get("api_key_desc", currentLang), color = Zinc500, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        val commonTextStyle = TextStyle(
                            color = if (!isUnsaved && apiKey.isNotEmpty()) Zinc500 else Color.White,
                            fontSize = 13.sp,
                            lineHeight = 16.sp
                        )

                        BasicTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            textStyle = commonTextStyle,
                            cursorBrush = SolidColor(Color.White),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Zinc800, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (apiKey.isEmpty()) Text(
                                        Tr.get("api_key_hint", currentLang),
                                        style = commonTextStyle.copy(color = Zinc700)
                                    )
                                    innerTextField()
                                }
                            }
                        )

                        AnimatedVisibility(
                            visible = isUnsaved,
                            enter = fadeIn(tween(300)) + expandHorizontally(),
                            exit = fadeOut(tween(300)) + shrinkHorizontally()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .clickable {
                                        settingsManager.apiKey = apiKey
                                        focusManager.clearFocus()
                                        Toast.makeText(
                                            context,
                                            Tr.get("key_saved", currentLang),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Check, null, tint = Color.Black) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://aistudio.google.com/app/apikey")
                                    )
                                )
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Tr.get("get_key", currentLang),
                            color = Zinc500,
                            fontSize = 11.sp
                        ); Spacer(modifier = Modifier.width(4.dp)); Icon(
                        Icons.Default.ArrowOutward,
                        null,
                        tint = Zinc500,
                        modifier = Modifier.size(10.dp)
                    )
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }

        // --- НИЖНІ КНОПКИ ---
        AnimatedVisibility(
            visible = !isKeyboardOpen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f),
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = SMOOTH_STIFFNESS,
                    dampingRatio = SMOOTH_DAMPING
                )
            ) { it } + fadeIn(tween(400)),
            exit = slideOutVertically(
                animationSpec = spring(
                    stiffness = SMOOTH_STIFFNESS,
                    dampingRatio = SMOOTH_DAMPING
                )
            ) { it } + fadeOut(tween(300))
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BlackBg.copy(alpha = 0.8f),
                                    BlackBg
                                ), startY = 0f, endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { showExportMenu = true },
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Upload,
                                null,
                                tint = Color.White
                            ); Spacer(modifier = Modifier.width(8.dp)); TextAnimated(
                            text = Tr.get(
                                "export",
                                currentLang
                            ), fontWeight = FontWeight.Bold
                        )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable {
                                importLauncher.launch(
                                    arrayOf(
                                        "application/zip",
                                        "application/json"
                                    )
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Download,
                                null,
                                tint = Color.White
                            ); Spacer(modifier = Modifier.width(8.dp)); TextAnimated(
                            text = Tr.get(
                                "import",
                                currentLang
                            ), fontWeight = FontWeight.Bold
                        )
                        }
                    }
                }
            }
        }

        // ModalBottomSheet
        if (showExportMenu) {
            ModalBottomSheet(
                onDismissRequest = { showExportMenu = false },
                sheetState = sheetState,
                containerColor = Zinc900,
                contentColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = Tr.get("export_options", currentLang),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = Tr.get("select_all", currentLang),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    exportSchedule = true
                                    exportSettings = true
                                    exportSpace = true
                                    exportApi = true
                                    exportArchived = true
                                }
                                .padding(8.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExportCheckboxItem(
                            label = Tr.get("item_schedule", currentLang),
                            checked = exportSchedule,
                            onChecked = { exportSchedule = it })
                        ExportCheckboxItem(
                            label = Tr.get("item_archived", currentLang),
                            checked = exportArchived,
                            onChecked = { exportArchived = it })
                        ExportCheckboxItem(
                            label = Tr.get("item_settings", currentLang),
                            checked = exportSettings,
                            onChecked = { exportSettings = it })
                        ExportCheckboxItem(
                            label = Tr.get("item_myspace", currentLang),
                            checked = exportSpace,
                            onChecked = { exportSpace = it })
                        ExportCheckboxItem(
                            label = Tr.get("item_api", currentLang),
                            checked = exportApi,
                            onChecked = { exportApi = it })
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(
                        visible = exportSchedule || exportSettings || exportSpace || exportApi,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Button(
                            onClick = {
                                showExportMenu = false
                                exportLauncher.launch("school_diary_backup.zip")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Text(
                                Tr.get("export_btn", currentLang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

