package com.example.schooldiary.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bymrd1mm.schooldiary.HomeworkManager
import com.example.schooldiary.BlackBg
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc700
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.AnimatedCard

@Composable
fun HomeScreen(navController: NavController, hwManager: HomeworkManager, lang: String) {
    var hwCount by remember { mutableIntStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { hwCount = hwManager.getHomeworkList().filter { !it.isArchived }.size }

    Column(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding().padding(20.dp)) {

        // --- ВЕРХНЯ ПАНЕЛЬ ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(Tr.get("app_name", lang), color = Zinc500, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.background(Zinc900, CircleShape)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("settings_main") }, modifier = Modifier.background(Zinc900, CircleShape)) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(Tr.get("hello", lang), color = Zinc500, fontSize = 13.sp, letterSpacing = 2.sp)
        Text(Tr.get("what_to_do", lang), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
        Spacer(modifier = Modifier.height(30.dp))

        // --- КАРТКИ МЕНЮ ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedCard(onClick = { navController.navigate("subject_select") }, modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) { Text("✏️", fontSize = 32.sp); Spacer(modifier = Modifier.height(12.dp)); Text(Tr.get("write_hw", lang), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(
                    Tr.get("add_new_task", lang), color = Zinc500, fontSize = 13.sp) }
            }

            AnimatedCard(onClick = { navController.navigate("unified_hw") }, modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
                    Text("👀", fontSize = 32.sp)
                    Column {
                        Text(Tr.get("view_hw", lang), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, style = TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(Tr.get("active_tasks", lang) + " ", color = Zinc500, fontSize = 13.sp, style = TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)))
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(24.dp)
                                    .defaultMinSize(minWidth = 24.dp)
                                    .background(Zinc700, shape = RoundedCornerShape(percent = 50))
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(text = "$hwCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)))
                            }
                        }
                    }
                }
            }

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "scale")
            Card(modifier = Modifier.fillMaxWidth().height(140.dp).scale(scale).clickable(interactionSource = interactionSource, indication = null) { navController.navigate("my_space") }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Zinc900)) {
                Box(modifier = Modifier.fillMaxSize()) { Canvas(modifier = Modifier.fillMaxSize()) { drawRect(brush = Brush.radialGradient(colors = listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)), center = Offset(size.width * 0.8f, size.height * 0.2f), radius = size.width * 0.8f)); val starOffsets = listOf(Offset(size.width * 0.1f, size.height * 0.2f), Offset(size.width * 0.4f, size.height * 0.15f), Offset(size.width * 0.8f, size.height * 0.3f), Offset(size.width * 0.6f, size.height * 0.6f)); starOffsets.forEach { drawCircle(Color.White.copy(alpha = 0.5f), radius = 2.dp.toPx(), center = it) } }; Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.CenterStart) { Text(text = Tr.get("my_space", lang), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }; Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.ArrowForward, null, tint = Zinc500) } }
            }
        }
    }

    // --- ДІАЛОГ ПРО ДОДАТОК ---
    if (showInfoDialog) {
        val appVersion = remember {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            } catch (e: Exception) {
                "1.0"
            }
        }
        val appIcon = remember { context.packageManager.getApplicationIcon(context.packageName) }

        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Іконка додатку (системна)
                    AsyncImage(
                        model = appIcon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Назва School Diary та версія
                    Text(
                        text = "School Diary",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "v$appVersion",
                        fontSize = 13.sp,
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- КНОПКА TELEGRAM (ТЕМНА, БЕЗ ІКОНКИ) ---
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/SchoolDiaryProject"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333), // Темно-сірий колір
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        // Тільки текст, без літачка
                        Text(
                            text = "Telegram Channel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
