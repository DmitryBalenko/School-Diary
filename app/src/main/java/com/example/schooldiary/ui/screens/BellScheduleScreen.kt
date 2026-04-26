package com.example.schooldiary.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bymrd1mm.schooldiary.BellManager
import com.example.schooldiary.BellTime
import com.example.schooldiary.BlackBg
import com.example.schooldiary.RedDelete
import com.example.schooldiary.Tr
import com.example.schooldiary.Zinc500
import com.example.schooldiary.Zinc800
import com.example.schooldiary.Zinc900
import com.example.schooldiary.ui.components.SimpleHeader
import com.example.schooldiary.ui.components.TimeEditField
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BellScheduleScreen(navController: NavController, bellManager: BellManager, lang: String) {
    val bells = remember {
        mutableStateListOf<BellTime>().apply { addAll(bellManager.getBells()) }
    }

    val initialIds = remember {
        bellManager.getBells().map { it.id }.toSet()
    }

    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(top = 140.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = bells,
                key = { it.id }
            ) { bell ->
                val index = bells.indexOf(bell)
                val bringIntoViewRequester = remember { BringIntoViewRequester() }

                val isNewItem = !initialIds.contains(bell.id)

                val visibleState = remember {
                    MutableTransitionState(initialState = !isNewItem).apply {
                        var targetState = true
                    }
                }

                AnimatedVisibility(
                    visibleState = visibleState,
                    // ВАЖЛИВО: Модифікатор зсуву тепер на верхньому рівні!
                    // Це повертає плавну анімацію підтягування списку при видаленні.
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
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
                                    onValueChange = { v -> bells[index] = bell.copy(start = v) },
                                    label = Tr.get("start", lang),
                                    onFocus = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } }
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                TimeEditField(
                                    value = bell.end,
                                    onValueChange = { v -> bells[index] = bell.copy(end = v) },
                                    label = Tr.get("end", lang),
                                    onFocus = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = {
                            if (bells.isNotEmpty()) {
                                bells.removeAt(bells.lastIndex)
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedDelete.copy(alpha = 0.15f), contentColor = RedDelete),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("- " + Tr.get("delete", lang), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newId = System.currentTimeMillis().toInt()
                            bells.add(BellTime(newId, "00:00", "00:00"))
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc800, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ " + Tr.get("add", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- ВЕРХНІЙ ГРАДІЄНТ (Згідно з твоїм списком) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg,                    // Повністю чорний
                            BlackBg,                    // Чорний
                            BlackBg.copy(alpha = 0.98f),   // Дуже густий
                            BlackBg.copy(alpha = 0.89f),   // Поступовий початок згасання
                            BlackBg.copy(alpha = 0.8f),    // М'який перехід
                            BlackBg.copy(alpha = 0.65f),   // Середина
                            BlackBg.copy(alpha = 0.45f),   // Стає легшим
                            BlackBg.copy(alpha = 0.25f),   // Напівпрозорий
                            BlackBg.copy(alpha = 0.15f),    // Ледь помітний серпанок
                            Color.Transparent
                        )
                    )
                )
        ) {
            Box(modifier = Modifier.statusBarsPadding().padding(top = 0.dp)) {
                SimpleHeader(navController, Tr.get("bells", lang))
            }
        }

        // --- НИЖНІЙ ГРАДІЄНТ (Той самий список, але віддзеркалений) ---
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,          // 0%
                                BlackBg.copy(alpha = 0.15f), // 15%
                                BlackBg.copy(alpha = 0.6f), // 30%
                                BlackBg.copy(alpha = 0.9f), // 50%
                                BlackBg,                    // 75%
                                BlackBg                       // 100% - Під кнопкою
                            )
                        )
                    )
            )

            Button(
                onClick = {
                    bellManager.saveBells(bells.toList())
                    focusManager.clearFocus()
                    Toast.makeText(context, Tr.get("saved", lang), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = Tr.get("save", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
