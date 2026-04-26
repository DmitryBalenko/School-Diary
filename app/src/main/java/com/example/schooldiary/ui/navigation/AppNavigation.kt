package com.example.schooldiary.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bymrd1mm.schooldiary.BellManager
import com.bymrd1mm.schooldiary.HomeworkManager
import com.bymrd1mm.schooldiary.MySpaceManager
import com.bymrd1mm.schooldiary.ScheduleManager
import com.bymrd1mm.schooldiary.SettingsManager
import com.example.schooldiary.ui.screens.BellScheduleScreen
import com.example.schooldiary.ui.screens.CalendarArchiveScreen
import com.example.schooldiary.ui.screens.EditDayScreen
import com.example.schooldiary.ui.screens.HomeScreen
import com.example.schooldiary.ui.screens.MySpaceScreen
import com.example.schooldiary.ui.screens.SettingsMainScreen
import com.example.schooldiary.ui.screens.SubjectSelectScreen
import com.example.schooldiary.ui.screens.SubjectSpaceScreen
import com.example.schooldiary.ui.screens.UnifiedHomeworkScreen
import com.example.schooldiary.ui.screens.WriteHomeworkScreen

@Composable
fun AppContent() {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }
    AnimatedVisibility(visible = showContent, enter = fadeIn(animationSpec = tween(600))) { AppNavigation() }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val hwManager = remember { HomeworkManager(context) }
    val scheduleManager = remember { ScheduleManager(context) }
    val bellManager = remember { BellManager(context) }
    val mySpaceManager = remember { MySpaceManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    var lang by remember { mutableStateOf(settingsManager.currentLanguage) }

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(tween(300)) }
    ) {
        composable("home") { HomeScreen(navController, hwManager, lang) }
        composable("settings_main") { SettingsMainScreen(navController, settingsManager, lang) { newLang -> lang = newLang } }
        composable("settings_bells") { BellScheduleScreen(navController, bellManager, lang) }
        composable("edit_day/{dayKey}") { entry -> EditDayScreen(navController, scheduleManager, bellManager, entry.arguments?.getString("dayKey") ?: "MONDAY", lang) }

        // --- НОВИЙ ГОЛОВНИЙ ЕКРАН ДЛЯ ДЗ ---
        composable("unified_hw") {
            UnifiedHomeworkScreen(navController, hwManager, scheduleManager, bellManager, settingsManager, lang)
        }

        composable("subject_select") { SubjectSelectScreen(navController, scheduleManager, lang) }
        composable("write_hw/{subject}/{icon}") { entry -> WriteHomeworkScreen(navController, hwManager, scheduleManager, settingsManager, Uri.decode(entry.arguments?.getString("subject") ?: ""), Uri.decode(entry.arguments?.getString("icon") ?: ""), lang) }
        composable("my_space") { MySpaceScreen(navController, mySpaceManager, scheduleManager, lang) }
        composable("subject_space/{subjectName}") { entry -> SubjectSpaceScreen(navController, mySpaceManager, Uri.decode(entry.arguments?.getString("subjectName") ?: ""), lang) }
        composable("calendar") { CalendarArchiveScreen(navController, hwManager, lang) }
    }
}






















