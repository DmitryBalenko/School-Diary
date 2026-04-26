@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.bymrd1mm.schooldiary

// Android & System
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import android.graphics.Matrix
import android.media.AudioAttributes
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.MimeTypeMap
import android.widget.Toast

// Activity & Lifecycle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

// Compose Animation
import androidx.compose.animation.*
import androidx.compose.animation.core.*

// Compose Foundation & Layout
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions

// Compose UI & Runtime
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
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
import androidx.compose.ui.zIndex

// Compose Material 3 & Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*

// Reorderable Library (БЕЗ ДУБЛИКАТОВ)
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

// AndroidX & Utils
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.exifinterface.media.ExifInterface

// Navigation
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Third Party (Coil, Gson)
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Coroutines & Java/Kotlin Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Collator
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.Scanner
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// --- СИСТЕМА ПЕРЕКЛАДІВ ---
object Tr {
    val data = mapOf(
        "ua" to mapOf(
            "app_name" to "Мій Щоденник",
            "hello" to "ПРИВІТ, УЧНЮ!",
            "what_to_do" to "Що будемо робити?",
            "write_hw" to "Записати ДЗ",
            "add_new_task" to "Додати нове завдання",
            "view_hw" to "Подивитися ДЗ",
            "active_tasks" to "Активних:",
            "my_space" to "My Space",
            "settings" to "Налаштування",
            "export" to "Експорт",
            "import" to "Імпорт",
            "export_success" to "Успішно експортовано!",
            "import_success" to "Дані відновлено! Перезапустіть додаток.",
            "error_save" to "Помилка збереження",
            "error_read" to "Помилка читання файлу",
            "empty_space" to "Тут поки що пусто",
            "delete_folder" to "Видалити папку?",
            "delete_folder_desc" to "Всі файли в папці будуть втрачені.",
            "cancel" to "Скасувати",
            "delete" to "Видалити",
            "add" to "Додати",
            "bells" to "Дзвінки",
            "save" to "Зберегти",
            "saved" to "Збережено!",
            "window" to "Вікно",
            "today" to "СЬОГОДНІ",
            "cabinet" to "Кабінет...",
            "choose_subject" to "Обери предмет",
            "fill_schedule_first" to "Спочатку заповни розклад ⚙️",
            "new_task" to "Нове завдання",
            "recording_new" to "Запис нового завдання",
            "write_or_dictate" to "Напиши або продиктуй...",
            "list_view" to "Списком",
            "all_active" to "Всі активні завдання",
            "week_view" to "На тиждень",
            "schedule_hw" to "Розклад + ДЗ",
            "task_list" to "Список завдань",
            "no_tasks" to "Завдань немає 🎉",
            "done" to "Виконано",
            "week_schedule" to "Завдання на тиждень",
            "tomorrow" to "ЗАВТРА",
            "copied" to "Скопійовано",
            "audio" to "Аудіо",
            "no_lessons" to "Уроків немає",
            "no_materials" to "Немає матеріалів",
            "choose_icon" to "Обери іконку",
            "choose_day" to "Оберіть день",
            "when_task" to "На коли це завдання?",
            "start" to "Початок",
            "end" to "Кінець",
            "api_setup" to "Налаштування Google Gemini",
            "api_key_hint" to "Введи Gemini API Key",
            "api_key_desc" to "Використовує модель Gemini 2.5 Flash",
            "get_key" to "Отримати ключ (aistudio.google.com)",
            "key_saved" to "Ключ збережено",
            "no_api_key" to "Спочатку введіть API ключ Gemini в налаштуваннях!",
            "export_options" to "Що експортувати?",
            "item_schedule" to "Розклад та ДЗ",
            "item_settings" to "Налаштування (Дзвінки)",
            "item_myspace" to "Папки My Space",
            "item_api" to "API Ключ Gemini",
            "item_archived" to "Архівовані завдання",
            "select_all" to "Вибрати все",
            "export_btn" to "Експортуровати обране",
            "history_calendar" to "Історія та Архів",
            "view_history" to "Календар виконаних",
            "archived" to "Архівовано",
            "events_none" to "Подій не заплановано",
            "recorded_on" to "На:",
            "lessons_and_bells" to "Уроки та дзвінки",
            "press_plus" to "Натисни + щоб додати",
            "MONDAY" to "Понеділок",
            "TUESDAY" to "Вівторок",
            "WEDNESDAY" to "Середа",
            "THURSDAY" to "Четвер",
            "FRIDAY" to "П'ятниця"
        ),
        "ru" to mapOf(
            "app_name" to "Мой Дневник",
            "hello" to "ПРИВЕТ, УЧЕНИК!",
            "what_to_do" to "Что будем делать?",
            "write_hw" to "Записать ДЗ",
            "add_new_task" to "Добавить новое задание",
            "view_hw" to "Посмотреть ДЗ",
            "active_tasks" to "Активных:",
            "my_space" to "My Space",
            "settings" to "Настройки",
            "export" to "Экспорт",
            "import" to "Импорт",
            "today" to "СЕГОДНЯ",
            "export_success" to "Успешно экспортировано!",
            "import_success" to "Данные восстановлены! Перезапустите приложение.",
            "error_save" to "Ошибка сохранения",
            "error_read" to "Ошибка чтения файла",
            "empty_space" to "Здесь пока пусто",
            "delete_folder" to "Удалить папку?",
            "delete_folder_desc" to "Все файлы в папке будут утеряны.",
            "cancel" to "Отмена",
            "delete" to "Удалить",
            "add" to "Добавить",
            "bells" to "Звонки",
            "save" to "Сохранить",
            "saved" to "Сохранено!",
            "window" to "Окно",
            "cabinet" to "Кабинет...",
            "choose_subject" to "Выбери предмет",
            "fill_schedule_first" to "Сначала заполни расписание ⚙️",
            "new_task" to "Новое задание",
            "recording_new" to "Запись нового задания",
            "write_or_dictate" to "Напиши или продиктуй...",
            "list_view" to "Списком",
            "all_active" to "Все активные задания",
            "week_view" to "На неделю",
            "schedule_hw" to "Расписание + ДЗ",
            "task_list" to "Список заданий",
            "no_tasks" to "Заданий нет 🎉",
            "done" to "Сделано",
            "week_schedule" to "Задания на неделю",
            "tomorrow" to "ЗАВТРА",
            "copied" to "Скопировано",
            "audio" to "Аудио",
            "no_lessons" to "Уроков нет",
            "no_materials" to "Нет материалов",
            "choose_icon" to "Выбери иконку",
            "choose_day" to "Выберите день",
            "when_task" to "На когда это задание?",
            "start" to "Начало",
            "end" to "Конец",
            "api_setup" to "Настройки Google Gemini",
            "api_key_hint" to "Введите Gemini API Key",
            "api_key_desc" to "Использует модель Gemini 2.5 Flash",
            "get_key" to "Получить ключ (aistudio.google.com)",
            "key_saved" to "Ключ сохранен",
            "no_api_key" to "Сначала введите API ключ Gemini в настройках!",
            "export_options" to "Что экспортировать?",
            "item_schedule" to "Расписание и ДЗ",
            "item_settings" to "Настройки (Звонки)",
            "item_myspace" to "Папки My Space",
            "item_api" to "API Ключ Gemini",
            "item_archived" to "Архивированные задания",
            "select_all" to "Выбрать все",
            "export_btn" to "Экспортировать выбранное",
            "history_calendar" to "История и Архив",
            "view_history" to "Календарь выполненных",
            "archived" to "Архивировано",
            "events_none" to "Событий не запланировано",
            "recorded_on" to "На:",
            "lessons_and_bells" to "Уроки и звонки",
            "press_plus" to "Нажми + чтобы добавить",
            "MONDAY" to "Понедельник",
            "TUESDAY" to "Вторник",
            "WEDNESDAY" to "Среда",
            "THURSDAY" to "Четверг",
            "FRIDAY" to "Пятница"
        ),
        "en" to mapOf(
            "app_name" to "My Diary",
            "hello" to "HELLO, STUDENT!",
            "what_to_do" to "What shall we do?",
            "write_hw" to "Write HW",
            "add_new_task" to "Add a new task",
            "view_hw" to "View HW",
            "active_tasks" to "Active:",
            "my_space" to "My Space",
            "settings" to "Settings",
            "today" to "TODAY",
            "export" to "Export",
            "import" to "Import",
            "export_success" to "Successfully exported!",
            "import_success" to "Data restored! Restart the app.",
            "error_save" to "Save error",
            "error_read" to "File read error",
            "empty_space" to "It's empty here",
            "delete_folder" to "Delete folder?",
            "delete_folder_desc" to "All files in the folder will be lost.",
            "cancel" to "Cancel",
            "delete" to "Delete",
            "add" to "Add",
            "bells" to "Bells",
            "save" to "Save",
            "saved" to "Saved!",
            "window" to "Free period",
            "cabinet" to "Room...",
            "choose_subject" to "Choose subject",
            "fill_schedule_first" to "Fill schedule first ⚙️",
            "new_task" to "New Task",
            "recording_new" to "Recording new task",
            "write_or_dictate" to "Write or dictate...",
            "list_view" to "List view",
            "all_active" to "All active tasks",
            "week_view" to "Week view",
            "schedule_hw" to "Schedule + HW",
            "task_list" to "Task List",
            "no_tasks" to "No tasks 🎉",
            "done" to "Done",
            "week_schedule" to "Week tasks",
            "tomorrow" to "TOMORROW",
            "copied" to "Copied",
            "audio" to "Audio",
            "no_lessons" to "No lessons",
            "no_materials" to "No materials",
            "choose_icon" to "Choose icon",
            "choose_day" to "Choose day",
            "when_task" to "When is this task for?",
            "start" to "Start",
            "end" to "End",
            "api_setup" to "Google Gemini Setup",
            "api_key_hint" to "Enter Gemini API Key",
            "api_key_desc" to "Uses Gemini 2.5 Flash model",
            "get_key" to "Get key (aistudio.google.com)",
            "key_saved" to "Key saved",
            "no_api_key" to "Enter Gemini API Key in settings first!",
            "export_options" to "What to export?",
            "item_schedule" to "Schedule & Homework",
            "item_settings" to "Settings (Bells)",
            "item_myspace" to "My Space Folders",
            "item_api" to "Gemini API Key",
            "item_archived" to "Archived Tasks",
            "select_all" to "Select All",
            "export_btn" to "Export Selected",
            "history_calendar" to "History & Archive",
            "view_history" to "Completed Calendar",
            "archived" to "Archived",
            "events_none" to "No events scheduled",
            "recorded_on" to "Due:",
            "lessons_and_bells" to "Lessons and bells",
            "press_plus" to "Press + to add",
            "MONDAY" to "Monday",
            "TUESDAY" to "Tuesday",
            "WEDNESDAY" to "Wednesday",
            "THURSDAY" to "Thursday",
            "FRIDAY" to "Friday"
        )
    )

    fun get(key: String, lang: String): String {
        return data[lang]?.get(key) ?: data["ua"]?.get(key) ?: key
    }
}

// --- КОЛЬОРИ ---
val BlackBg = Color(0xFF000000)
val Zinc900 = Color(0xFF1C1C1E)
val CardDark = Color(0xFF0A0A0A) // Дуже темний сірий, майже чорний
val Zinc800 = Color(0xFF2C2C2E)
val Zinc700 = Color(0xFF3A3A3C)
val Zinc500 = Color(0xFF8E8E93)
val Zinc400 = Color(0xFFA1A1AA)
val RedDelete = Color(0xFFFF453A)
val GreenPlay = Color(0xFF30D158)
val BlueAction = Color(0xFF0A84FF)
val BlueCalendar = Color(0xFF4a90e2)

// --- НАБІР ЕМОДЗІ ---
val availableEmojis = listOf(
    "📚", "📐", "🧪", "🇬🇧", "🎨", "🤸", "⚛️", "🗣️",
    "✍️", "🌎", "🌱", "🛡️", "💰", "🗺️", "💻", "📖",
    "📏", "🎼", "⚽", "🏊", "💃", "🧠", "🔨", "🍳",
    "🩺", "⚖️", "🎭", "🎬", "🎤", "🦠", "🧬", "🔭"
)

// --- МОДЕЛІ ---
data class Lesson(
    var time: String,
    var end: String,
    var subject: String,
    var room: String,
    var icon: String,
    val id: String = UUID.randomUUID().toString()
)

data class Homework(
    val id: Long,
    val subject: String,
    val text: String,
    var imagePaths: List<String>,
    val date: String, // Creation date
    val icon: String,
    val targetDay: String, // e.g. "MONDAY"
    var audioPath: String? = null,
    val targetDate: String? = null, // e.g. "2026-02-05" - The specific date assigned
    var isArchived: Boolean = false
)

data class SpaceFile(val name: String, val path: String, val type: FileType, val sizeStr: String)
enum class FileType { IMAGE, AUDIO, PDF, DOC, OTHER }
data class BellTime(val id: Int, var start: String, var end: String)

val daysOrder = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")

val defaultBellSchedule = listOf(
    BellTime(1, "08:30", "09:00"), BellTime(2, "09:05", "09:35"), BellTime(3, "09:40", "10:10"),
    BellTime(4, "10:20", "10:50"), BellTime(5, "11:00", "11:30"), BellTime(6, "11:35", "12:05"),
    BellTime(7, "12:10", "12:40"), BellTime(8, "12:45", "13:15")
)

val defaultScheduleData = mapOf(
    "MONDAY" to listOf(Lesson("8:30", "9:00", "Укр. літ.", "216", "📚"), Lesson("9:05", "9:35", "Мистецтво", "207", "🎨"), Lesson("9:40", "10:10", "Алгебра", "218", "📐"), Lesson("10:20", "10:50", "Хімія", "315", "🧪"), Lesson("11:00", "11:30", "Фізкультура", "216", "🤸"), Lesson("11:35", "12:05", "Фізика", "316", "⚛️"), Lesson("12:10", "12:40", "Гром. освіта", "311", "🗣️"), Lesson("12:45", "13:15", "Англ. мова (Спец)", "", "🇬🇧")),
    "TUESDAY" to listOf(Lesson("8:30", "9:00", "Англ. мова", "105/312", "🇬🇧"), Lesson("9:05", "9:35", "Англ. мова", "105/312", "🇬🇧"), Lesson("9:40", "10:10", "Геометрія", "218", "📏"), Lesson("10:20", "10:50", "Історія України", "320", "🛡️"), Lesson("11:00", "11:30", "Фізика", "318", "⚛️"), Lesson("11:35", "12:05", "Фінансова грам.", "313", "💰"), Lesson("12:10", "12:40", "Фізкультура", "", "🤸"), Lesson("12:45", "13:15", "Англ. мова (Спец)", "", "🇬🇧")),
    "WEDNESDAY" to listOf(Lesson("8:30", "9:00", "Укр. мова", "216", "✍️"), Lesson("9:05", "9:35", "Географія", "216", "🌎"), Lesson("9:40", "10:10", "Англ. мова", "105/312", "🇬🇧"), Lesson("10:20", "10:50", "Біологія", "112", "🌱"), Lesson("11:00", "11:30", "Історія України", "320", "🛡️"), Lesson("11:35", "12:05", "Алгебра", "218", "📐")),
    "THURSDAY" to listOf(Lesson("8:30", "9:00", "Фізика", "318", "⚛️"), Lesson("9:05", "9:35", "Укр. мова", "218", "✍️"), Lesson("9:40", "10:10", "Укр. літ.", "216", "📚"), Lesson("10:20", "10:50", "Всесвітня історія", "320", "🗺️"), Lesson("11:00", "11:30", "Інформатика", "221/317", "💻"), Lesson("11:35", "12:05", "Хімія", "315", "🧪"), Lesson("12:10", "12:40", "Англ. мова", "105/312", "🇬🇧")),
    "FRIDAY" to listOf(Lesson("8:30", "9:00", "Біологія", "319", "🌱"), Lesson("9:05", "9:35", "Заруб. літ.", "219", "📖"), Lesson("9:40", "10:10", "Англ. мова", "105/312", "🇬🇧"), Lesson("10:20", "10:50", "Фізкультура", "", "🤸"), Lesson("11:00", "11:30", "Громадська освіта", "309", "🗣️"), Lesson("11:35", "12:05", "Геометрія", "218", "📏"))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SchoolDiary)
        super.onCreate(savedInstanceState)
        window.navigationBarColor = android.graphics.Color.BLACK
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = BlackBg, surface = Zinc900, onSurface = Color.White)) {
                AppContent()
            }
        }
    }
}

// --- МЕНЕДЖЕРИ (Ті самі, без змін) ---

class MySpaceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("school_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val rootDir = File(context.filesDir, "myspace")

    init {
        if (!rootDir.exists()) rootDir.mkdirs()
    }

    fun getActiveSubjects(): List<String> {
        val json = prefs.getString("myspace_subjects", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addSubject(subject: String) {
        val current = getActiveSubjects().toMutableList()
        if (!current.contains(subject)) {
            current.add(subject)
            prefs.edit().putString("myspace_subjects", gson.toJson(current)).apply()
            File(rootDir, subject).mkdirs()
        }
    }

    fun removeSubject(subject: String) {
        val current = getActiveSubjects().toMutableList()
        if (current.remove(subject)) {
            prefs.edit().putString("myspace_subjects", gson.toJson(current)).apply()
            try { File(rootDir, subject).deleteRecursively() } catch (e: Exception) {}
        }
    }

    fun getFilesForSubject(subject: String): List<SpaceFile> {
        val subjectDir = File(rootDir, subject)
        if (!subjectDir.exists()) return emptyList()
        return subjectDir.listFiles()?.map { file ->
            SpaceFile(file.name, file.absolutePath, getFileType(file), getFileSize(file))
        }?.sortedBy { it.name } ?: emptyList()
    }

    fun saveFileToSubject(subject: String, uri: Uri): Boolean {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(uri, contentResolver) ?: "file_${System.currentTimeMillis()}"
            val subjectDir = File(rootDir, subject)
            if (!subjectDir.exists()) subjectDir.mkdirs()
            val destFile = File(subjectDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) { false }
    }

    fun deleteFile(path: String) {
        try { File(path).delete() } catch (e: Exception) { }
    }

    fun saveSubjectsOrder(subjects: List<String>) {
        prefs.edit().putString("myspace_subjects", gson.toJson(subjects)).apply()
    }

    private fun getFileType(file: File): FileType {
        val ext = file.extension.lowercase()
        return when {
            listOf("jpg", "jpeg", "png", "webp").contains(ext) -> FileType.IMAGE
            listOf("mp3", "wav", "m4a", "ogg").contains(ext) -> FileType.AUDIO
            ext == "pdf" -> FileType.PDF
            listOf("doc", "docx", "txt").contains(ext) -> FileType.DOC
            else -> FileType.OTHER
        }
    }

    private fun getFileSize(file: File): String {
        val kb = file.length() / 1024
        return if (kb > 1024) String.format("%.1f MB", kb / 1024.0) else "$kb KB"
    }

    private fun getFileName(uri: Uri, contentResolver: android.content.ContentResolver): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) result = result?.substring(cut + 1)
        }
        return result
    }
}

class BellManager(context: Context) {
    private val prefs = context.getSharedPreferences("school_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    fun getBells(): List<BellTime> {
        val json = prefs.getString("bell_data", null)
        return if (json != null) gson.fromJson(json, object : TypeToken<List<BellTime>>() {}.type) else defaultBellSchedule
    }
    fun saveBells(bells: List<BellTime>) {
        prefs.edit().putString("bell_data", gson.toJson(bells)).apply()
    }
}

class ScheduleManager(context: Context) {
    private val prefs = context.getSharedPreferences("school_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    fun getSchedule(): Map<String, List<Lesson>> {
        val json = prefs.getString("schedule_data", null)
        return if (json != null) gson.fromJson(json, object : TypeToken<Map<String, List<Lesson>>>() {}.type) else defaultScheduleData
    }
    fun saveSchedule(schedule: Map<String, List<Lesson>>) {
        prefs.edit().putString("schedule_data", gson.toJson(schedule)).apply()
    }
    fun getUniqueSubjects(schedule: Map<String, List<Lesson>>): List<Lesson> {
        val collator = Collator.getInstance(Locale("uk", "UA"))
        return schedule.values.flatten()
            .filter { it.subject.isNotBlank() }
            .distinctBy { it.subject.trim() }
            .sortedWith { a, b -> collator.compare(a.subject, b.subject) }
    }
}

class HomeworkManager(context: Context) {
    private val prefs = context.getSharedPreferences("school_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun getHomeworkListAsync(): List<Homework> = withContext(Dispatchers.IO) {
        try {
            val json = prefs.getString("hw_data", null) ?: return@withContext emptyList()
            gson.fromJson(json, object : TypeToken<List<Homework>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun getHomeworkList(): List<Homework> {
        val json = prefs.getString("hw_data", null) ?: return emptyList()
        return gson.fromJson(json, object : TypeToken<List<Homework>>() {}.type)
    }
    fun saveHomework(hw: Homework) {
        val list = getHomeworkList().toMutableList()
        list.add(hw)
        prefs.edit().putString("hw_data", gson.toJson(list)).apply()
    }
    fun updateHomework(updatedHw: Homework) {
        val list = getHomeworkList().toMutableList()
        val index = list.indexOfFirst { it.id == updatedHw.id }
        if (index != -1) {
            list[index] = updatedHw
            prefs.edit().putString("hw_data", gson.toJson(list)).apply()
        }
    }
    fun archiveHomework(id: Long) {
        val list = getHomeworkList().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val hw = list[index]
            hw.imagePaths.forEach { try { File(it).delete() } catch (e: Exception) {} }
            hw.audioPath?.let { try { File(it).delete() } catch (e: Exception) {} }

            list[index] = hw.copy(
                isArchived = true,
                imagePaths = emptyList(),
                audioPath = null
            )
            prefs.edit().putString("hw_data", gson.toJson(list)).apply()
        }
    }

    fun deleteHomeworkComplete(id: Long) {
        val list = getHomeworkList()
        val itemToDelete = list.find { it.id == id }
        itemToDelete?.imagePaths?.forEach { try { File(it).delete() } catch (e: Exception) {} }
        itemToDelete?.audioPath?.let { try { File(it).delete() } catch (e: Exception) {} }
        val newList = list.filter { it.id != id }
        prefs.edit().putString("hw_data", gson.toJson(newList)).apply()
    }
}

class SettingsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("school_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val hwManager = HomeworkManager(context)

    var currentLanguage: String
        get() = prefs.getString("app_language", "ua") ?: "ua"
        set(value) = prefs.edit().putString("app_language", value).apply()

    var apiKey: String
        get() = prefs.getString("groq_api_key", "") ?: ""
        set(value) = prefs.edit().putString("groq_api_key", value).apply()

    fun exportData(incSchedule: Boolean, incBells: Boolean, incSpace: Boolean, incApi: Boolean, incArchived: Boolean): Uri? {
        try {
            val tempDir = File(context.cacheDir, "export_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()

            val allData = JSONObject()

            if (incSchedule) {
                allData.put("schedule_raw", prefs.getString("schedule_data", gson.toJson(defaultScheduleData)))
                val allHw = hwManager.getHomeworkList()
                val hwToExport = if (incArchived) allHw else allHw.filter { !it.isArchived }
                allData.put("homework", gson.toJson(hwToExport))
            }

            if (incBells) allData.put("bells_raw", prefs.getString("bell_data", gson.toJson(defaultBellSchedule)))
            if (incSpace) allData.put("myspace_subjects", prefs.getString("myspace_subjects", "[]"))
            if (incApi) allData.put("groq_api_key", prefs.getString("groq_api_key", ""))

            val jsonFile = File(tempDir, "data.json")
            jsonFile.writeText(allData.toString())

            val filesToZip = mutableListOf<File>()
            filesToZip.add(jsonFile)

            if (incSchedule) {
                val hwMediaDir = File(tempDir, "hw_media")
                hwMediaDir.mkdir()
                val allHw = hwManager.getHomeworkList()
                val hwToExport = if (incArchived) allHw else allHw.filter { !it.isArchived }
                val filesToCopy = mutableListOf<String>()
                hwToExport.forEach { hw ->
                    filesToCopy.addAll(hw.imagePaths)
                    hw.audioPath?.let { filesToCopy.add(it) }
                }
                filesToCopy.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.copyTo(File(hwMediaDir, file.name), true)
                    }
                }
                if (hwMediaDir.listFiles()?.isNotEmpty() == true) {
                    filesToZip.add(hwMediaDir)
                }
            }

            if (incSpace) {
                val spaceDir = File(context.filesDir, "myspace")
                if (spaceDir.exists()) {
                    val spaceExportDir = File(tempDir, "myspace")
                    spaceDir.copyRecursively(spaceExportDir, true)
                    filesToZip.add(spaceExportDir)
                }
            }

            val zipFile = File(context.cacheDir, "school_diary_backup.zip")
            ZipUtils.zip(filesToZip, zipFile)
            tempDir.deleteRecursively()
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", zipFile)
        } catch (e: Exception) { e.printStackTrace(); return null }
    }

    fun importData(uri: Uri): Boolean {
        return try {
            val tempZip = File(context.cacheDir, "import_temp.zip")
            context.contentResolver.openInputStream(uri)?.use { input -> tempZip.outputStream().use { input.copyTo(it) } }

            val unzipDir = File(context.cacheDir, "unzipped")
            unzipDir.deleteRecursively()
            unzipDir.mkdirs()

            ZipUtils.unzip(tempZip, unzipDir)

            val jsonFile = File(unzipDir, "data.json")
            if (!jsonFile.exists()) return false
            val json = JSONObject(jsonFile.readText())
            val editor = prefs.edit()

            if (json.has("schedule_raw")) editor.putString("schedule_data", json.getString("schedule_raw"))
            if (json.has("homework")) editor.putString("hw_data", json.getString("homework"))
            if (json.has("bells_raw")) editor.putString("bell_data", json.getString("bells_raw"))
            if (json.has("myspace_subjects")) editor.putString("myspace_subjects", json.getString("myspace_subjects"))
            if (json.has("groq_api_key")) editor.putString("groq_api_key", json.getString("groq_api_key"))
            editor.commit()

            val hwMediaDir = File(unzipDir, "hw_media")
            if (hwMediaDir.exists()) {
                hwMediaDir.listFiles()?.forEach { it.copyTo(File(context.filesDir, it.name), true) }
            }

            val spaceDir = File(unzipDir, "myspace")
            if (spaceDir.exists()) {
                spaceDir.copyRecursively(File(context.filesDir, "myspace"), true)
            }

            unzipDir.deleteRecursively()
            tempZip.delete()
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }
}

// --- HELPERS & UTILS ---

object ZipUtils {
    fun zip(files: List<File>, zipFile: File) {
        java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(zipFile))).use { out ->
            files.forEach { file ->
                if (file.isDirectory) {
                    zipDirectory(file, file.name, out)
                } else {
                    zipFile(file, "", out)
                }
            }
        }
    }

    private fun zipFile(file: File, parentPath: String, out: java.util.zip.ZipOutputStream) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        out.putNextEntry(java.util.zip.ZipEntry(entryName))
        file.inputStream().use { it.copyTo(out) }
    }

    private fun zipDirectory(dir: File, path: String, out: java.util.zip.ZipOutputStream) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) zipDirectory(file, "$path/${file.name}", out)
            else zipFile(file, path, out)
        }
    }

    fun unzip(zipFile: File, targetDirectory: File) {
        java.util.zip.ZipInputStream(java.io.BufferedInputStream(java.io.FileInputStream(zipFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { zis.copyTo(it) }
                }
                entry = zis.nextEntry
            }
        }
    }
}

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): File? {
        outputFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.m4a")
        recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile?.absolutePath)
            prepare()
            start()
        }
        return outputFile
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
    }
}

object SimpleAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(path: String, onComplete: () -> Unit = {}) {
        stop()
        try {
            val file = File(path)
            if (!file.exists()) {
                onComplete()
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    stop()
                    onComplete()
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    onComplete()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete()
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) { }
        mediaPlayer = null
    }
}

object GeminiClient {
    private const val URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s"

    suspend fun transcribeAudio(file: File, apiKey: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || !file.exists() || file.length() == 0L) return@withContext null

        try {
            val url = URL(String.format(URL_TEMPLATE, apiKey))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val fileBytes = file.readBytes()
            val base64Audio = Base64.encodeToString(fileBytes, Base64.NO_WRAP)

            val jsonBody = JSONObject().apply {
                put("contents", org.json.JSONArray().put(
                    JSONObject().put("parts", org.json.JSONArray()
                        .put(JSONObject().put("text", "Transcribe this audio to text. Output only the transcription."))
                        .put(JSONObject().put("inline_data", JSONObject().apply {
                            put("mime_type", "audio/mp4")
                            put("data", base64Audio)
                        }))
                    )
                ))
            }

            connection.outputStream.use { os ->
                val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            if (connection.responseCode == 200) {
                val response = Scanner(connection.inputStream).useDelimiter("\\A").let { if (it.hasNext()) it.next() else "" }
                val jsonResponse = JSONObject(response)
                val text = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return@withContext text.trim()
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val bitmap = MediaStoreUtils.getCorrectlyOrientedImage(context, uri) ?: return null
        val scaledBitmap = MediaStoreUtils.scaleBitmap(bitmap, 1920)
        val file = File(context.filesDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) { null }
}

fun getNextLessonDay(subject: String, schedule: Map<String, List<Lesson>>): String {
    val today = LocalDate.now()
    for (i in 1..14) {
        val checkDate = today.plusDays(i.toLong())
        val dayName = checkDate.dayOfWeek.name
        val lessonsForDay = schedule[dayName]
        val hasSubject = lessonsForDay?.any {
            it.subject.trim().equals(subject.trim(), ignoreCase = true)
        } == true
        if (hasSubject) {
            return dayName
        }
    }
    return "UNKNOWN"
}

fun getNextLessonDate(subject: String, schedule: Map<String, List<Lesson>>): String {
    val today = LocalDate.now()
    for (i in 1..14) {
        val checkDate = today.plusDays(i.toLong())
        val dayName = checkDate.dayOfWeek.name
        val lessonsForDay = schedule[dayName]
        val hasSubject = lessonsForDay?.any {
            it.subject.trim().equals(subject.trim(), ignoreCase = true)
        } == true
        if (hasSubject) {
            return checkDate.toString()
        }
    }
    return today.toString()
}

fun saveAudioToInternalStorage(context: Context, tempFile: File): String? {
    return try {
        if (!tempFile.exists() || tempFile.length() == 0L) return null
        val destFile = File(context.filesDir, "audio_${System.currentTimeMillis()}_${UUID.randomUUID()}.m4a")
        tempFile.copyTo(destFile, overwrite = true)
        destFile.absolutePath
    } catch (e: Exception) { null }
}

fun getSubjectThemeColor(subject: String, icon: String): Color {
    val lower = subject.lowercase()
    val hue = when {
        icon in listOf("🎨", "🎭", "💃") || "мистецт" in lower -> 270f
        icon in listOf("📐", "📏", "🔢") || "алгебра" in lower || "геомет" in lower || "матема" in lower -> 45f
        icon in listOf("🧪", "🧬", "🦠", "🌱", "⚛️") || "фізика" in lower || "хімія" in lower || "біолог" in lower -> 150f
        icon in listOf("🇬🇧", "🇺🇸", "🇩🇪", "🗣️", "✍️", "📖", "📚") || "укр" in lower || "мов" in lower || "літ" in lower -> 330f
        icon in listOf("⚽", "🏀", "🏊", "🤸") || "фізк" in lower || "спорт" in lower -> 25f
        icon in listOf("💻", "🖥️", "🖱️") || "інформ" in lower -> 190f
        icon in listOf("🛡️", "⚔️", "🗺️", "🌎") || "історія" in lower || "право" in lower -> 30f
        else -> (subject.hashCode().absoluteValue % 360).toFloat()
    }
    return Color.hsv(hue, 0.35f, 0.15f)
}

object MediaStoreUtils {
    fun getCorrectlyOrientedImage(context: Context, photoUri: Uri): Bitmap? {
        var inputStream = context.contentResolver.openInputStream(photoUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream); inputStream.close()
        inputStream = context.contentResolver.openInputStream(photoUri) ?: return originalBitmap
        val exifInterface = ExifInterface(inputStream)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        inputStream.close()
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
    }
    fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width; val originalHeight = bitmap.height
        var newWidth = originalWidth; var newHeight = originalHeight
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            if (originalWidth > originalHeight) { newWidth = maxDimension; newHeight = (newWidth * originalHeight) / originalWidth }
            else { newHeight = maxDimension; newWidth = (newHeight * originalWidth) / originalHeight }
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

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
                Column(modifier = Modifier.align(Alignment.CenterStart)) { Text("✏️", fontSize = 32.sp); Spacer(modifier = Modifier.height(12.dp)); Text(Tr.get("write_hw", lang), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(Tr.get("add_new_task", lang), color = Zinc500, fontSize = 13.sp) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(navController: NavController, settingsManager: SettingsManager, currentLang: String, onLangChange: (String) -> Unit) {
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

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            val fileUri = settingsManager.exportData(exportSchedule, exportSettings, exportSpace, exportApi, exportArchived)
            if (fileUri != null) {
                try {
                    context.contentResolver.openInputStream(fileUri)?.use { input ->
                        context.contentResolver.openOutputStream(uri)?.use { output -> input.copyTo(output) }
                    }
                    Toast.makeText(context, Tr.get("export_success", currentLang), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, Tr.get("error_save", currentLang), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            if (settingsManager.importData(uri)) {
                Toast.makeText(context, Tr.get("import_success", currentLang), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, Tr.get("error_read", currentLang), Toast.LENGTH_SHORT).show()
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
                enter = expandVertically(animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING)) +
                        fadeIn(animationSpec = tween(400)),
                exit = shrinkVertically(animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING)) +
                        fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    // 1. Заголовок і кнопки керування
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextAnimated(text = Tr.get("settings", currentLang), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                context.startActivity(Intent.makeRestartActivityTask(intent?.component))
                                Runtime.getRuntime().exit(0)
                            },
                            modifier = Modifier.background(Zinc800, CircleShape).size(40.dp)
                        ) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
                        Spacer(modifier = Modifier.width(12.dp))
                        val flag = when(currentLang) { "ua" -> "🇺🇦"; "ru" -> "🇷🇺"; else -> "🇬🇧" }
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Zinc800).clickable {
                            val nextLang = when(currentLang) { "ua" -> "ru"; "ru" -> "en"; else -> "ua" }
                            settingsManager.currentLanguage = nextLang
                            onLangChange(nextLang)
                        }.padding(10.dp)) { TextAnimated(text = flag, fontSize = 20.sp) }
                    }

                    // 2. Картки розкладу та дзвінків
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("settings_bells") }, colors = CardDefaults.cardColors(containerColor = Zinc800), shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Text("⏰", fontSize = 24.sp); Spacer(modifier = Modifier.width(16.dp)); Column { TextAnimated(text = Tr.get("bells", currentLang), fontSize = 18.sp, fontWeight = FontWeight.Bold); TextAnimated(text = Tr.get("lessons_and_bells", currentLang), color = Zinc500, fontSize = 12.sp) } }
                        }
                        daysOrder.forEach { dayKey ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("edit_day/$dayKey") }, colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(16.dp)) {
                                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    TextAnimated(text = Tr.get(dayKey, currentLang), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f)); Icon(Icons.Filled.Edit, null, tint = Zinc500)
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

            Text(Tr.get("api_setup", currentLang), color = Zinc500, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING))
            ) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Text(Tr.get("api_key_desc", currentLang), color = Zinc500, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
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
                                    if (apiKey.isEmpty()) Text(Tr.get("api_key_hint", currentLang), style = commonTextStyle.copy(color = Zinc700));
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
                                        Toast.makeText(context, Tr.get("key_saved", currentLang), Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Check, null, tint = Color.Black) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = Tr.get("get_key", currentLang), color = Zinc500, fontSize = 11.sp); Spacer(modifier = Modifier.width(4.dp)); Icon(Icons.Default.ArrowOutward, null, tint = Zinc500, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }

        // --- НИЖНІ КНОПКИ ---
        AnimatedVisibility(
            visible = !isKeyboardOpen,
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(1f),
            enter = slideInVertically(animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING)) { it } + fadeIn(tween(400)),
            exit = slideOutVertically(animationSpec = spring(stiffness = SMOOTH_STIFFNESS, dampingRatio = SMOOTH_DAMPING)) { it } + fadeOut(tween(300))
        ) {
            Box {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, BlackBg.copy(alpha = 0.8f), BlackBg), startY = 0f, endY = Float.POSITIVE_INFINITY)))
                Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f).height(56.dp).clickable { showExportMenu = true }, colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Icon(Icons.Default.Upload, null, tint = Color.White); Spacer(modifier = Modifier.width(8.dp)); TextAnimated(text = Tr.get("export", currentLang), fontWeight = FontWeight.Bold) }
                    }
                    Card(modifier = Modifier.weight(1f).height(56.dp).clickable { importLauncher.launch(arrayOf("application/zip", "application/json")) }, colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Icon(Icons.Default.Download, null, tint = Color.White); Spacer(modifier = Modifier.width(8.dp)); TextAnimated(text = Tr.get("import", currentLang), fontWeight = FontWeight.Bold) }
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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
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
                        ExportCheckboxItem(label = Tr.get("item_schedule", currentLang), checked = exportSchedule, onChecked = { exportSchedule = it })
                        ExportCheckboxItem(label = Tr.get("item_archived", currentLang), checked = exportArchived, onChecked = { exportArchived = it })
                        ExportCheckboxItem(label = Tr.get("item_settings", currentLang), checked = exportSettings, onChecked = { exportSettings = it })
                        ExportCheckboxItem(label = Tr.get("item_myspace", currentLang), checked = exportSpace, onChecked = { exportSpace = it })
                        ExportCheckboxItem(label = Tr.get("item_api", currentLang), checked = exportApi, onChecked = { exportApi = it })
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Text(Tr.get("export_btn", currentLang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExportCheckboxItem(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(checkedColor = Color.White, uncheckedColor = Zinc500, checkmarkColor = Color.Black)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun TextAnimated(text: String, color: Color = Color.White, fontSize: androidx.compose.ui.unit.TextUnit = 16.sp, fontWeight: FontWeight? = null, modifier: Modifier = Modifier) {
    AnimatedContent(targetState = text, transitionSpec = { (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut()) }, label = "textAnim") { targetText ->
        Text(text = targetText, color = color, fontSize = fontSize, fontWeight = fontWeight, modifier = modifier)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MySpaceScreen(
    navController: NavController,
    mySpaceManager: MySpaceManager,
    scheduleManager: ScheduleManager,
    lang: String
) {
    // --- ДАННЫЕ И СОСТОЯНИЕ ---
    // Основной список предметов
    val subjects = remember { mutableStateListOf<String>().apply { addAll(mySpaceManager.getActiveSubjects()) } }

    // Список ID предметов, которые сейчас анимируются на удаление (чтобы сделать красивое "схлопывание")
    val itemsPendingRemoval = remember { mutableStateListOf<String>() }

    // UI состояния для диалогов
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectPendingDelete by remember { mutableStateOf<String?>(null) }

    // Храним действие "сброса свайпа", чтобы закрыть красную кнопку "Delete" при отмене
    var activeResetAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val scope = rememberCoroutineScope()
    val allLessons = remember { scheduleManager.getSchedule().values.flatten() }
    val haptic = LocalHapticFeedback.current

    // --- НАСТРОЙКА DRAG-AND-DROP ---
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
        // =========================================================================
        // 1. СПИСОК (Слой 0)
        // =========================================================================
        if (subjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Tr.get("empty_space", lang), color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                state = state.listState,
                // Отступы сверху под Header (110dp) и снизу (100dp)
                contentPadding = PaddingValues(top = 110.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
            ) {
                items(items = subjects, key = { it }) { subject ->
                    ReorderableItem(state, key = subject) { isDragging ->

                        // --- ЛОГИКА АНИМИРОВАННОГО УДАЛЕНИЯ ---
                        // Если предмет попал в список "на удаление", переключаем флаг видимости
                        val isBeingRemoved = itemsPendingRemoval.contains(subject)
                        val visibilityState = remember { MutableTransitionState(true) }.apply { targetState = !isBeingRemoved }

                        // Когда анимация исчезновения закончилась - удаляем реально из списка и БД
                        if (!visibilityState.targetState && visibilityState.isIdle) {
                            LaunchedEffect(subject) {
                                mySpaceManager.removeSubject(subject)
                                subjects.remove(subject)
                                itemsPendingRemoval.remove(subject)
                            }
                        }

                        // --- ВИЗУАЛЬНЫЕ ЭФФЕКТЫ ---
                        val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "shadow")
                        val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")
                        // zIndex при перетаскивании делаем меньше, чем у диалогов (100+), но выше списка (0)
                        val zIndex = if (isDragging) 50f else 0f

                        val iconStr = allLessons.find { it.subject == subject }?.icon ?: "📁"
                        val revealState = remember { Animatable(0f) }

                        // Вибрация при начале перетаскивания
                        LaunchedEffect(isDragging) {
                            if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        // --- ОТОБРАЖЕНИЕ ЭЛЕМЕНТА ---
                        AnimatedVisibility(
                            visibleState = visibilityState,
                            // Схлопывание по высоте + исчезновение
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
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
                                        activeResetAction = { scope.launch { revealState.animateTo(0f) } }
                                    }
                                ) {
                                    SubjectSpaceCard(subject = subject, icon = iconStr, onClick = {
                                        if (!isDragging) navController.navigate("subject_space/${Uri.encode(subject)}")
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 2. ВЕРХНЯЯ ПАНЕЛЬ / HEADER (Слой 60)
        // =========================================================================
        // Градиентный фон под заголовком
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

        // Текст заголовка и кнопка
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

        // =========================================================================
        // 3. ДИАЛОГ ДОБАВЛЕНИЯ ПРЕДМЕТА
        // =========================================================================
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

        // =========================================================================
        // 4. ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ (Слой 100+)
        // =========================================================================

        // Анимация затемнения фона (Dimming)
        AnimatedVisibility(
            visible = subjectPendingDelete != null,
            enter = fadeIn(animationSpec = tween(440)),
            exit = fadeOut(animationSpec = tween(440)),
            modifier = Modifier.zIndex(100f).fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    // Обработка клика по фону для закрытия
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Без ripple-эффекта
                    ) {
                        subjectPendingDelete = null
                        activeResetAction?.invoke()
                    }
            )
        }

        // Анимация выезда самой карточки снизу (Slide Up)
        AnimatedVisibility(
            visible = subjectPendingDelete != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(101f)
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
                        // Кнопка ОТМЕНА
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

                        // Кнопка УДАЛИТЬ
                        Button(
                            onClick = {
                                // Вместо мгновенного удаления добавляем в список "на выбывание",
                                // чтобы сработала анимация схлопывания в списке
                                val itemToDelete = subjectPendingDelete!!
                                itemsPendingRemoval.add(itemToDelete)

                                // Закрываем диалог
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
@Composable
fun SwipeToRevealCard(
    offsetAnim: Animatable<Float, AnimationVector1D>,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Ширина кнопки (на сколько сдвигается плитка)
    val anchorWidthPx = with(density) { 85.dp.toPx() }
    // Порог срабатывания "красного" режима
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

    // Цвет меняется с серого на красный
    val backgroundColor by animateColorAsState(
        targetValue = if (isPastThreshold || isDeleteConfirmed) Color(0xFFEF4444) else Color(0xFF3F3F46),
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    // Основной контейнер.
    // ВАЖНО: Мы НЕ обрезаем его целиком clip(), чтобы кнопка могла иметь свою форму
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 1. КНОПКА УДАЛЕНИЯ (Слой снизу)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(with(density) { revealedWidth.toDp() })
                // --- ВОТ ЗДЕСЬ НАСТРАИВАЕМ ЗАКРУГЛЕНИЯ ---
                // Слева (внутренняя часть) - маленькое скругление (4.dp)
                // Справа (внешняя часть) - большое скругление как у плитки (24.dp)
                .clip(RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp,
                    topEnd = 24.dp,
                    bottomEnd = 24.dp
                ))
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

        // 2. КОНТЕНТ ПЛИТКИ (Слой сверху)
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                // Скругляем саму плитку, чтобы она была красивой
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
                            // Фиксируем удаление
                            isDeleteConfirmed = true
                            offsetAnim.animateTo(-anchorWidthPx, tween(300, easing = FastOutSlowInEasing))
                        } else {
                            // Если не дотянули - прячем обратно
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
    Card(modifier = Modifier.fillMaxWidth().height(100.dp).clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(themeColor, Color.Black), startX = 0f, endX = Float.POSITIVE_INFINITY)))
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.03f)) { drawCircle(color = Color.White, radius = size.height * 1.2f, center = Offset(size.width * 0.9f, size.height * 0.5f)); drawLine(color = Color.White, start = Offset(0f, size.height), end = Offset(size.width * 0.3f, 0f), strokeWidth = 50f) }
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = subject, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Spacer(modifier = Modifier.weight(1f)); Icon(Icons.Default.KeyboardArrowRight, null, tint = Zinc500, modifier = Modifier.size(24.dp)) }
        }
    }
}
@Composable
fun AddSubjectDialog(scheduleManager: ScheduleManager, onDismiss: () -> Unit, onSubjectSelected: (String) -> Unit, lang: String) {
    val uniqueSubjects = remember { scheduleManager.getUniqueSubjects(scheduleManager.getSchedule()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Zinc900), modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Tr.get("choose_subject", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(uniqueSubjects) { lesson -> Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onSubjectSelected(lesson.subject) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(lesson.icon, fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp)); Text(lesson.subject, color = Color.White, fontSize = 16.sp) }; Divider(color = Zinc800) } }
            }
        }
    }
}
@Composable
fun SubjectSpaceScreen(navController: NavController, mySpaceManager: MySpaceManager, subjectName: String, lang: String) {
    var files by remember { mutableStateOf(mySpaceManager.getFilesForSubject(subjectName)) }
    val context = LocalContext.current
    var viewingImage by remember { mutableStateOf<String?>(null) }
    var playingAudioId by remember { mutableStateOf<String?>(null) }
    var fileToDelete by remember { mutableStateOf<SpaceFile?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            var addedCount = 0
            uris.forEach { uri -> try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {}; if (mySpaceManager.saveFileToSubject(subjectName, uri)) addedCount++ }
            if (addedCount > 0) { files = mySpaceManager.getFilesForSubject(subjectName); Toast.makeText(context, "Додано: $addedCount", Toast.LENGTH_SHORT).show() }
        }
    }

    fun openFile(file: SpaceFile) {
        when(file.type) {
            FileType.IMAGE -> viewingImage = file.path
            FileType.AUDIO -> { if (playingAudioId == file.path) { SimpleAudioPlayer.stop(); playingAudioId = null } else { playingAudioId = file.path; SimpleAudioPlayer.play(file.path) { playingAudioId = null } } }
            else -> { try { val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(FileProvider.getUriForFile(context, "${context.packageName}.provider", File(file.path)), MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(file.path).extension.lowercase()) ?: "*/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "Немає додатку", Toast.LENGTH_SHORT).show() } }
        }
    }

    fun shareFile(file: SpaceFile) { try { val intent = Intent(Intent.ACTION_SEND).apply { type = "*/*"; putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", File(file.path))); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(Intent.createChooser(intent, null)) } catch (e: Exception) {} }

    DisposableEffect(Unit) { onDispose { SimpleAudioPlayer.stop() } }

    Box(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleHeader(navController, subjectName)
            if (files.isEmpty()) { Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.FolderOpen, null, tint = Zinc500, modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text(Tr.get("no_materials", lang), color = Zinc500) } } }
            else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(files) { file ->
                        val isPlaying = playingAudioId == file.path
                        Card(modifier = Modifier.fillMaxWidth().clickable { openFile(file) }, colors = CardDefaults.cardColors(containerColor = Zinc900), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Zinc800, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(when(file.type) { FileType.IMAGE -> Icons.Outlined.Image; FileType.AUDIO -> if(isPlaying) Icons.Filled.Stop else Icons.Outlined.Audiotrack; FileType.PDF -> Icons.Outlined.PictureAsPdf; FileType.DOC -> Icons.Outlined.Description; else -> Icons.Outlined.InsertDriveFile }, null, tint = if(isPlaying) RedDelete else Color.White) }
                                Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(file.name, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(file.sizeStr, color = Zinc500, fontSize = 12.sp) }
                                IconButton(onClick = { shareFile(file) }) { Icon(Icons.Default.Share, "Share", tint = Color.White, modifier = Modifier.size(20.dp)) }
                                IconButton(onClick = { fileToDelete = file }) { Icon(Icons.Default.Delete, null, tint = Zinc500, modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { filePicker.launch(arrayOf("*/*")) }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = BlackBg, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Default.Add, "Upload") }
        if (viewingImage != null) { ImageViewer(viewingImage!!) { viewingImage = null } }
        if (fileToDelete != null) { AlertDialog(onDismissRequest = { fileToDelete = null }, title = { Text("Видалити?", color = Color.White) }, text = { Text(fileToDelete!!.name, color = Zinc500) }, containerColor = Zinc900, confirmButton = { TextButton(onClick = { if(playingAudioId == fileToDelete!!.path) SimpleAudioPlayer.stop(); mySpaceManager.deleteFile(fileToDelete!!.path); files = mySpaceManager.getFilesForSubject(subjectName); fileToDelete = null }) { Text("Так", color = RedDelete) } }, dismissButton = { TextButton(onClick = { fileToDelete = null }) { Text("Ні", color = Color.White) } }) }
    }
}
@Composable
fun AnimatedCard(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    Card(modifier = modifier.scale(scale).clickable(interactionSource = interactionSource, indication = null, onClick = onClick), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Zinc900)) { Box(modifier = Modifier.fillMaxSize().padding(24.dp), content = content) }
}
@Composable
fun SimpleHeader(navController: NavController, title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
        Spacer(modifier = Modifier.width(8.dp)); Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

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
                        targetState = true
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

@Composable
fun TimeEditField(value: String, onValueChange: (String) -> Unit, label: String) {
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
                } else { filtered }
                onValueChange(formatted)
            },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            ),
            cursorBrush = SolidColor(BlueAction),
            modifier = Modifier
                .fillMaxWidth()
                .background(Zinc800, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp, horizontal = 4.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = TimeVisualTransformation()
        )
    }
}

// --- ОКРЕМИЙ КОМПОНЕНТ КАРТКИ ДЗВІНКА ---
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
        modifier = Modifier.fillMaxWidth().animateContentSize() // Плавна зміна розміру
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Номер уроку
            Text(
                text = "${index + 1}.",
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.width(30.dp)
            )

            // Поле початку
            Box(modifier = Modifier.weight(1f)) {
                TimeEditField(
                    value = bell.start,
                    onValueChange = { onBellChange(bell.copy(start = it)) },
                    label = Tr.get("start", lang),
                    onFocus = onFocusGained
                )
            }

            Text("—", color = Zinc500, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Light)

            // Поле кінця
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

// --- ОНОВЛЕНЕ ПОЛЕ ВВОДУ ЧАСУ (DESIGNER EDITION) ---
@Composable
fun TimeEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onFocus: () -> Unit // Callback для повідомлення про фокус
) {
    val digits = value.filter { it.isDigit() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Zinc500, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

        BasicTextField(
            value = digits,
            onValueChange = { newRaw ->
                val filtered = newRaw.filter { it.isDigit() }.take(4)
                val formatted = if (filtered.length >= 3) { filtered.substring(0, 2) + ":" + filtered.substring(2) } else { filtered }
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
                .padding(vertical = 12.dp, horizontal = 0.dp) // Відступи для комфорту
                .onFocusChanged { if (it.isFocused) onFocus() }, // ВАЖЛИВО: ловимо фокус тут
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = TimeVisualTransformation()
        )
    }
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trim = text.text; val out = StringBuilder()
        for (i in trim.indices) { out.append(trim[i]); if (i == 1) out.append(":") }
        val offsetMapping = object : OffsetMapping { override fun originalToTransformed(offset: Int): Int { if (offset <= 1) return offset; if (offset <= 4) return offset + 1; return 5 }; override fun transformedToOriginal(offset: Int): Int { if (offset <= 2) return offset; if (offset <= 5) return offset - 1; return 4 } }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditDayScreen(
    navController: NavController,
    scheduleManager: ScheduleManager,
    bellManager: BellManager,
    dayKey: String,
    lang: String
) {
    val bells = remember { bellManager.getBells() }
    val savedSchedule = remember { scheduleManager.getSchedule() }

    // Ініціалізація списку уроків
    val lessons = remember {
        mutableStateListOf<Lesson>().apply {
            if (savedSchedule.containsKey(dayKey)) {
                addAll(savedSchedule[dayKey]!!.map { it.copy() })
                while (size < bells.size) add(Lesson(bells[size].start, bells[size].end, "", "", "📝"))
            } else {
                addAll(bells.map { Lesson(it.start, it.end, "", "", "📝") })
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedLessonIndex by remember { mutableIntStateOf(-1) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val state = rememberReorderableLazyListState(onMove = { from: ItemPosition, to: ItemPosition ->
        lessons.apply { add(to.index, removeAt(from.index)) }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    })

    // --- ДІАЛОГ ВИБОРУ ЕМОДЗІ (без змін) ---
    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                if (selectedLessonIndex != -1) {
                    val targetSubject = lessons[selectedLessonIndex].subject.trim()
                    lessons[selectedLessonIndex] = lessons[selectedLessonIndex].copy(icon = emoji)
                    if (targetSubject.isNotEmpty()) {
                        for (i in lessons.indices) {
                            if (lessons[i].subject.trim().equals(targetSubject, ignoreCase = true)) {
                                lessons[i] = lessons[i].copy(icon = emoji)
                            }
                        }
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                val fullSchedule = scheduleManager.getSchedule().toMutableMap()
                                var scheduleChanged = false
                                for (day in fullSchedule.keys) {
                                    val dayLessons = fullSchedule[day]!!.toMutableList()
                                    var dayChanged = false
                                    for (i in dayLessons.indices) {
                                        if (dayLessons[i].subject.trim().equals(targetSubject, ignoreCase = true)) {
                                            dayLessons[i] = dayLessons[i].copy(icon = emoji)
                                            dayChanged = true
                                            scheduleChanged = true
                                        }
                                    }
                                    if (dayChanged) fullSchedule[day] = dayLessons
                                }
                                if (scheduleChanged) scheduleManager.saveSchedule(fullSchedule)
                            }
                        }
                    }
                }
                showEmojiPicker = false
            },
            lang = lang
        )
    }

    // --- UI ЕКРАНУ ---
    Box(modifier = Modifier
        .fillMaxSize()
        .background(BlackBg)
        .pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }
    ) {
        LazyColumn(
            state = state.listState,
            contentPadding = PaddingValues(top = 140.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .imePadding() // Список стискається над клавіатурою
        ) {
            itemsIndexed(items = lessons, key = { _, item -> item.id }) { index, lesson ->
                ReorderableItem(state, key = lesson.id) { isDragging ->
                    val bell = if (index < bells.size) bells[index] else BellTime(0, "??", "??")
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "shadow")
                    val scale by animateFloatAsState(if (isDragging) 1.02f else 1f, label = "scale")

                    // 1. Створюємо Requester для цієї конкретної картки
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = elevation.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = false
                                alpha = 1f
                            }
                            // 2. Прив'язуємо Requester до всієї картки
                            .bringIntoViewRequester(bringIntoViewRequester),
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .detectReorderAfterLongPress(state)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- ЧАС ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(44.dp)
                            ) {
                                Text(bell.start, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(bell.end, color = Zinc500, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // --- ЦЕНТРАЛЬНА ЧАСТИНА ---
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // ІКОНКА
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Zinc800)
                                            .clickable {
                                                selectedLessonIndex = index
                                                showEmojiPicker = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lesson.icon, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // НАЗВА ПРЕДМЕТУ
                                    BasicTextField(
                                        value = lesson.subject,
                                        onValueChange = { v -> lessons[index] = lessons[index].copy(subject = v) },
                                        textStyle = TextStyle(
                                            color = if (lesson.subject.isEmpty()) Zinc500 else Color.White,
                                            fontSize = 16.sp
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        modifier = Modifier
                                            .widthIn(min = 50.dp)
                                            // 3. Коли отримуємо фокус, просимо показати ВСЮ картку
                                            .onFocusEvent { focusState ->
                                                if (focusState.isFocused) {
                                                    coroutineScope.launch {
                                                        bringIntoViewRequester.bringIntoView()
                                                    }
                                                }
                                            },
                                        decorationBox = { inner ->
                                            if (lesson.subject.isEmpty()) Text(Tr.get("window", lang), color = Zinc500) else inner()
                                        }
                                    )
                                    Spacer(modifier = Modifier.weight(1f).height(32.dp))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // КАБІНЕТ
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicTextField(
                                        value = lesson.room,
                                        onValueChange = { v -> lessons[index] = lessons[index].copy(room = v) },
                                        textStyle = TextStyle(color = Zinc500, fontSize = 12.sp),
                                        cursorBrush = SolidColor(Zinc500),
                                        modifier = Modifier
                                            .widthIn(min = 50.dp)
                                            // 3. Те саме для поля кабінету
                                            .onFocusEvent { focusState ->
                                                if (focusState.isFocused) {
                                                    coroutineScope.launch {
                                                        bringIntoViewRequester.bringIntoView()
                                                    }
                                                }
                                            },
                                        decorationBox = { inner ->
                                            if (lesson.room.isEmpty()) Text(Tr.get("cabinet", lang), color = Zinc700, fontSize = 12.sp) else inner()
                                        }
                                    )
                                    Spacer(modifier = Modifier.weight(1f).height(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // КНОПКА ОЧИСТИТИ
                            IconButton(
                                onClick = { lessons[index] = lessons[index].copy(subject = "", room = "", icon = "📝") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Zinc500, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- ВЕРХНІЙ ХЕДЕР (Градієнт та Кнопки) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Еталонна висота
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackBg,                    // 0%
                            BlackBg,                    // 25%
                            BlackBg.copy(alpha = 0.9f), // 50%
                            BlackBg.copy(alpha = 0.6f), // 70%
                            BlackBg.copy(alpha = 0.3f), // 85%
                            Color.Transparent           // 100%
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Відступ від системного годинника/челки
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Tr.get(dayKey, lang),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- НИЖНЯ ПАНЕЛЬ (Кнопка Зберегти під клавіатурою) ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, BlackBg.copy(alpha = 0.9f), BlackBg)))
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val orderedLessons = lessons.mapIndexed { i, lesson ->
                                val bell = if (i < bells.size) bells[i] else BellTime(0, "??", "??")
                                lesson.copy(time = bell.start, end = bell.end)
                            }
                            val fullSchedule = scheduleManager.getSchedule().toMutableMap()
                            fullSchedule[dayKey] = orderedLessons
                            scheduleManager.saveSchedule(fullSchedule)
                        }
                        Toast.makeText(context, Tr.get("saved", lang), Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .height(56.dp), // Висота 56.dp
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = Tr.get("save", lang),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp // Додай fontSize сюди
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifiedHomeworkScreen(
    navController: NavController,
    hwManager: HomeworkManager,
    scheduleManager: ScheduleManager,
    bellManager: BellManager,
    settingsManager: SettingsManager,
    lang: String
) {
    var viewMode by remember { mutableStateOf(ViewMode.WEEK) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current

    // --- ВІБРАЦІЯ ---
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }


    // --- АНІМАЦІЯ СВАЙПУ ---
    val offsetX = remember { Animatable(0f) }
    val screenWidthPx = context.resources.displayMetrics.widthPixels.toFloat()
    val triggerThreshold = -screenWidthPx * 0.22f

    val hwList = remember { mutableStateListOf<Homework>() }
    val currentSchedule = remember { scheduleManager.getSchedule() }
    val bells = remember { bellManager.getBells() }

    val isPastThreshold by remember { derivedStateOf { offsetX.value <= triggerThreshold } }
    var hasVibrated by remember { mutableStateOf(false) }

    LaunchedEffect(isPastThreshold) {
        if (isPastThreshold && !hasVibrated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
            hasVibrated = true
        } else if (!isPastThreshold) {
            hasVibrated = false
        }
    }

    LaunchedEffect(Unit) {
        hwList.clear()
        hwList.addAll(hwManager.getHomeworkListAsync())
    }

    Box(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding()) {

        // --- ШАР 0: ПІГУЛКА (БЕЗ ЗМІН) ---
        if (offsetX.value < 0) {
            val pullDistance = offsetX.value.absoluteValue
            val arrowRotation by animateFloatAsState(if (isPastThreshold) 180f else 0f)
            val circleScale by animateFloatAsState(if (isPastThreshold) 1.15f else 1.0f)

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(80.dp)
                    .width(with(density) { pullDistance.toDp() })
            ) {
                Box(modifier = Modifier.fillMaxSize().background(color = Zinc800, shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)))
                Box(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp).size(56.dp).scale(circleScale).shadow(6.dp, CircleShape, spotColor = Color.Black.copy(0.5f)).background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = arrowRotation })
                }
            }
        }

        // --- ШАР 1: РУХОМИЙ КОНТЕНТ (З МОРФІНГОМ) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(BlackBg)
                .draggable(
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX.value + delta).coerceAtMost(0f)
                        scope.launch { offsetX.snapTo(newOffset) }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        scope.launch {
                            if (offsetX.value <= triggerThreshold) {
                                offsetX.animateTo(-screenWidthPx, tween(300))
                                navController.navigate("calendar")
                                delay(100)
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium))
                            }
                        }
                    }
                )
        ) {
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)))
                        .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.1f, animationSpec = tween(200)))
                },
                label = "contentMorph",
                modifier = Modifier.fillMaxSize()
            ) { mode ->
                if (mode == ViewMode.WEEK)
                    WeekViewContent(hwList, currentSchedule, bells, settingsManager, lang, topPadding = 100.dp, bottomPadding = 40.dp)
                else
                    ListViewContent(hwList, settingsManager, lang, topPadding = 100.dp, bottomPadding = 40.dp)
            }
        }

        // --- ШАР 2: ТІНЬ ТА ХЕДЕР (З МОРФІНГОМ ЗАГОЛОВКА) ---

        // Верхній градієнт
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
                            BlackBg.copy(alpha = 0.98f),   // Дуже густий
                            BlackBg.copy(alpha = 0.89f),   // Поступовий початок згасання
                            BlackBg.copy(alpha = 0.8f),    // М'який перехід
                            BlackBg.copy(alpha = 0.65f),   // Середина
                            BlackBg.copy(alpha = 0.45f),   // Стає легшим
                            BlackBg.copy(alpha = 0.25f),   // Напівпрозорий
                            BlackBg.copy(alpha = 0.15f),    // Ледь помітний серпанок
                            Color.Transparent           // Вихід у нуль
                        )
                    )
                )
        )

        // Хедер
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            // МОРФІНГ ЗАГОЛОВКА
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 1.1f))
                },
                label = "titleMorph",
                modifier = Modifier.weight(1f)
            ) { mode ->
                Text(
                    text = if (mode == ViewMode.WEEK) Tr.get("week_schedule", lang) else Tr.get("task_list", lang),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // МОРФІНГ ІКОНКИ В КНОПЦІ
            IconButton(
                onClick = { viewMode = if (viewMode == ViewMode.WEEK) ViewMode.LIST else ViewMode.WEEK },
                modifier = Modifier.background(Zinc800, CircleShape).size(40.dp)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200)) + rotationIn())
                            .togetherWith(fadeOut(animationSpec = tween(200)) + rotationOut())
                    },
                    label = "iconMorph"
                ) { mode ->
                    Icon(
                        imageVector = if (mode == ViewMode.WEEK) Icons.Outlined.FormatListBulleted else Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// Допоміжні анімації обертання для іконки
fun rotationIn() = fadeIn(tween(200)) + scaleIn(initialScale = 0.5f)
fun rotationOut() = fadeOut(tween(200)) + scaleOut(targetScale = 0.5f)

@Composable
fun WeekViewContent(
    hwList: MutableList<Homework>,
    currentSchedule: Map<String, List<Lesson>>,
    bells: List<BellTime>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    val daysKeys = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")

    // Smart Focus Logic (вычисляется 1 раз при создании экрана)
    val (targetIndex, todayIndex, isAfter16) = remember {
        val now = java.time.LocalDateTime.now()
        val currentDay = now.dayOfWeek.name
        val currentHour = now.hour

        val todayIdx = daysKeys.indexOf(currentDay) // Будет -1 для выходных

        val targetIdx = when (currentDay) {
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY" -> {
                if (currentHour < 16) todayIdx else todayIdx + 1
            }
            "FRIDAY" -> {
                if (currentHour < 16) todayIdx else 0 // Пт после 16:00 -> Пн
            }
            "SATURDAY", "SUNDAY" -> 0 // Выходные -> Пн
            else -> 0
        }
        // Возвращаем целевой индекс, индекс текущего дня и флаг времени (после 16:00)
        Triple(targetIdx, todayIdx, currentHour >= 16)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = targetIndex)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val hwManager = remember { HomeworkManager(context.applicationContext) }

    val hwMap by remember { derivedStateOf { hwList.filter { !it.isArchived }.groupBy { it.subject.trim() } } }

    var viewingImage by remember { mutableStateOf<String?>(null) }
    var playingAudioId by remember { mutableStateOf<Long?>(null) }
    var transcribingId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = topPadding, start = 16.dp, end = 16.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(items = daysKeys) { index, dayKey ->
            DaySection(
                dayName = Tr.get(dayKey, lang),
                lessons = currentSchedule[dayKey] ?: emptyList(),
                hwMap = hwMap,
                dayKey = dayKey,
                bells = bells,

                // Плашка "СЕГОДНЯ" показывается только если это текущий день И время до 16:00
                isToday = (index == todayIndex && !isAfter16),
                isTomorrow = (index == targetIndex && index != todayIndex),

                // Фокус (рамка и свечение) только на целевом дне
                isFocused = (index == targetIndex),

                deletedIds = emptyList(),
                playingAudioId = playingAudioId,
                transcribingId = transcribingId,
                onPlayAudio = { id, path -> if (playingAudioId == id) { SimpleAudioPlayer.stop(); playingAudioId = null } else { playingAudioId = id; SimpleAudioPlayer.play(path) { playingAudioId = null } } },
                onTranscribe = { id, path, currentText ->
                    if(settingsManager.apiKey.isBlank()) { Toast.makeText(context, Tr.get("no_api_key", lang), Toast.LENGTH_LONG).show() }
                    else {
                        transcribingId = id
                        coroutineScope.launch {
                            val transcript = GeminiClient.transcribeAudio(File(path), settingsManager.apiKey)
                            transcribingId = null
                            if (!transcript.isNullOrBlank()) {
                                val separator = if (currentText.isNotEmpty()) "\n" else ""
                                val indexHw = hwList.indexOfFirst { it.id == id }
                                if(indexHw != -1) {
                                    val newHw = hwList[indexHw].copy(text = hwList[indexHw].text + separator + transcript)
                                    hwList[indexHw] = newHw
                                    hwManager.updateHomework(newHw)
                                }
                            }
                        }
                    }
                },
                onDeleteHw = { id ->
                    coroutineScope.launch {
                        if (playingAudioId == id) { SimpleAudioPlayer.stop(); playingAudioId = null }
                        delay(300)
                        hwManager.archiveHomework(id)
                        val idx = hwList.indexOfFirst { it.id == id }
                        if (idx != -1) hwList[idx] = hwList[idx].copy(isArchived = true)
                    }
                },
                onViewImage = { path -> viewingImage = path },
                lang = lang
            )
        }
    }
    if (viewingImage != null) { ImageViewer(viewingImage!!) { viewingImage = null } }
}

@Composable
fun DaySection(
    dayName: String,
    lessons: List<Lesson>,
    hwMap: Map<String, List<Homework>>,
    dayKey: String,
    bells: List<BellTime>,
    isToday: Boolean,
    isTomorrow: Boolean,
    isFocused: Boolean,
    deletedIds: List<Long>,
    playingAudioId: Long?,
    transcribingId: Long?,
    onPlayAudio: (Long, String) -> Unit,
    onTranscribe: (Long, String, String) -> Unit,
    onDeleteHw: (Long) -> Unit,
    onViewImage: (String) -> Unit,
    lang: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun findHomeworks(subject: String): List<Homework> {
        val subjectHwList = hwMap[subject.trim()] ?: return emptyList()
        return subjectHwList.filter { hw ->
            Tr.data.values.any { langMap -> langMap[dayKey] == hw.targetDay }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 12.dp else 0.dp, // Свечение только у дня с фокусом
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .border(if (isFocused) 1.dp else 0.dp, if (isFocused) Color.White else Color.Transparent, RoundedCornerShape(16.dp)) // Рамка только у дня с фокусом
            .background(CardDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dayName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                // Плашки для "СЕГОДНЯ" и "ЗАВТРА"
                if (isToday) {
                    Text(
                        text = " ${Tr.get("today", lang)}",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else if (isTomorrow) {
                    Text(
                        text = " ${Tr.get("tomorrow", lang)}",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = {
                val sb = StringBuilder().append(dayName.uppercase()).append("\n")
                lessons.forEach { l ->
                    if(l.subject.isNotEmpty()) {
                        sb.append("• ${l.subject}")
                        val hws = findHomeworks(l.subject)
                        if (hws.isNotEmpty()) {
                            val combinedText = hws.mapNotNull { it.text.takeIf { t -> t.isNotBlank() } }.joinToString(", ")
                            if (combinedText.isNotBlank()) {
                                sb.append(" — $combinedText")
                            }
                        }
                        sb.append("\n")
                    }
                }
                clipboardManager.setText(AnnotatedString(sb.toString()))
                Toast.makeText(context, Tr.get("copied", lang), Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.size(32.dp).background(Zinc900, RoundedCornerShape(8.dp))) {
                Icon(Icons.Default.ContentCopy, "Copy", tint = Zinc500, modifier = Modifier.size(16.dp))
            }
        }

        val maxIndex = if(lessons.isNotEmpty()) lessons.indexOfLast { it.subject.isNotEmpty() } else -1
        if (maxIndex != -1) {
            for (i in 0..maxIndex) {
                val lesson = if(i < lessons.size) lessons[i] else Lesson("","","","","")
                val timeStart = if (i < bells.size) bells[i].start else ""
                val timeEnd = if (i < bells.size) bells[i].end else ""

                if (lesson.subject.isNotEmpty()) {
                    val homeworks = findHomeworks(lesson.subject)

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.width(40.dp)) { Text(timeStart, color = Color.White, fontSize = 13.sp); Text(timeEnd, color = Zinc500, fontSize = 11.sp) }
                        Spacer(modifier = Modifier.width(8.dp)); Text(lesson.icon, fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text(lesson.subject, color = Color.White, fontSize = 16.sp) }; if (lesson.room.isNotEmpty()) Text(lesson.room, color = Zinc500, fontSize = 13.sp)
                    }

                    AnimatedVisibility(visible = homeworks.isNotEmpty(), exit = shrinkVertically() + fadeOut()) {
                        Card(colors = CardDefaults.cardColors(containerColor = Zinc800), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(start = 48.dp, bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {

                                val allText = homeworks.mapNotNull { it.text.takeIf { t -> t.isNotBlank() } }.joinToString("\n\n")
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
                                            onTranscribe = { onTranscribe(hw.id, currentAudioPath, hw.text) },
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
                                        items(allImages) { path ->
                                            Box(modifier = Modifier.size(100.dp)) { AsyncImagePreview(path) { onViewImage(path) } }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text(
                                        text = Tr.get("done", lang),
                                        color = Zinc500,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            homeworks.forEach { onDeleteHw(it.id) }
                                        }.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).alpha(0.5f), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.width(40.dp)) { Text(timeStart, color = Zinc500, fontSize = 13.sp); Text(timeEnd, color = Zinc500, fontSize = 11.sp) }; Spacer(modifier = Modifier.width(8.dp)); Text("☕", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp)); Text("--- " + Tr.get("window", lang) + " ---", color = Zinc500, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
                }
                if (i < maxIndex) Divider(color = Zinc800, thickness = 0.5.dp)
            }
        } else { Text(Tr.get("no_lessons", lang), color = Zinc500, fontSize = 14.sp, modifier = Modifier.padding(8.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListViewContent(
    hwList: MutableList<Homework>,
    settingsManager: SettingsManager,
    lang: String,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    val displayList by remember(hwList) {
        derivedStateOf { hwList.filter { !it.isArchived } }
    }

    val context = LocalContext.current
    val hwManager = remember { HomeworkManager(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    var viewingImage by remember { mutableStateOf<String?>(null) }
    var playingAudioId by remember { mutableStateOf<Long?>(null) }
    var transcribingId by remember { mutableStateOf<Long?>(null) }

    if (displayList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
            Text(Tr.get("no_tasks", lang), color = Zinc500)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = topPadding, start = 16.dp, end = 16.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = displayList,
                key = { it.id }
            ) { hw ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hw.icon, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(hw.subject, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                Text("${Tr.get("recorded_on", lang)} ${hw.targetDay}", color = Zinc500, fontSize = 12.sp)
                            }
                        }

                        if (hw.text.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(hw.text, color = Color.White)
                        }

                        // --- АУДИО ---
                        hw.audioPath?.let { path ->
                            Spacer(Modifier.height(8.dp))
                            TelegramAudioPlayer(
                                isPlaying = playingAudioId == hw.id,
                                onPlayPause = {
                                    if (playingAudioId == hw.id) {
                                        SimpleAudioPlayer.stop()
                                        playingAudioId = null
                                    } else {
                                        playingAudioId = hw.id
                                        SimpleAudioPlayer.play(path) { playingAudioId = null }
                                    }
                                },
                                onTranscribe = {
                                    if(settingsManager.apiKey.isBlank()) {
                                        Toast.makeText(context, Tr.get("no_api_key", lang), Toast.LENGTH_LONG).show()
                                    } else {
                                        transcribingId = hw.id
                                        coroutineScope.launch {
                                            val transcript = GeminiClient.transcribeAudio(File(path), settingsManager.apiKey)
                                            transcribingId = null
                                            if (!transcript.isNullOrBlank()) {
                                                val separator = if (hw.text.isNotEmpty()) "\n" else ""
                                                val indexHw = hwList.indexOfFirst { it.id == hw.id }
                                                if(indexHw != -1) {
                                                    val newHw = hwList[indexHw].copy(text = hwList[indexHw].text + separator + transcript)
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

                        // --- ИСПРАВЛЕНИЕ: ДОБАВЛЕН ВЫВОД ФОТО ---
                        if (hw.imagePaths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(hw.imagePaths) { path ->
                                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))) {
                                        AsyncImagePreview(path) { viewingImage = path }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (playingAudioId == hw.id) SimpleAudioPlayer.stop()
                                    hwManager.archiveHomework(hw.id)
                                    val idx = hwList.indexOfFirst { it.id == hw.id }
                                    if(idx != -1) {
                                        hwList[idx] = hwList[idx].copy(isArchived = true)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Tr.get("done", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (viewingImage != null) ImageViewer(viewingImage!!) { viewingImage = null }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarArchiveScreen(navController: NavController, hwManager: HomeworkManager, lang: String) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var isNext by remember { mutableStateOf(true) } // Напрямок гортання

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var archivedTasks by remember { mutableStateOf<List<Homework>>(emptyList()) }
    var showTasksSheet by remember { mutableStateOf(false) }

    // Завантаження архівних завдань
    LaunchedEffect(Unit) {
        archivedTasks = hwManager.getHomeworkListAsync().filter { it.isArchived }
    }

    fun getDisplayDate(hw: Homework): LocalDate {
        return if (!hw.targetDate.isNullOrEmpty()) {
            try { LocalDate.parse(hw.targetDate) } catch (e: Exception) { LocalDate.parse(hw.date) }
        } else {
            LocalDate.parse(hw.date)
        }
    }

    val tasksForSelectedDate = remember(selectedDate, archivedTasks) {
        archivedTasks.filter { getDisplayDate(it) == selectedDate }
    }

    val eventDates = remember(archivedTasks) {
        archivedTasks.map { getDisplayDate(it) }.toSet()
    }

    val weekDays = remember(lang) {
        if (lang == "en") listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
        else listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "НД")
    }

    // --- Bottom Sheet (Список завдань за обраний день) ---
    if (showTasksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTasksSheet = false },
            containerColor = Zinc900,
            contentColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (tasksForSelectedDate.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(Tr.get("events_none", lang), color = Zinc500, fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(tasksForSelectedDate) { hw ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BlackBg),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Zinc800)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(hw.icon, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(hw.subject, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        if (hw.text.isNotEmpty()) {
                                            Text(hw.text, color = Zinc500, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ГОЛОВНИЙ ЕКРАН (БЕЗ ТІНЕЙ) ---
    Box(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ПРОСТИЙ ХЕДЕР ЗВЕРХУ
            SimpleHeader(navController, Tr.get("history_calendar", lang))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                // --- РЯДОК З МІСЯЦЕМ ТА СТРІЛКАМИ ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ШВИДКА АНІМАЦІЯ НАЗВИ МІСЯЦЯ (Вгору/Вниз)
                    AnimatedContent(
                        targetState = yearMonth,
                        transitionSpec = {
                            val animSpec = tween<IntOffset>(150, easing = FastOutSlowInEasing)
                            val fadeSpec = tween<Float>(150)
                            if (isNext) {
                                (slideInVertically(animSpec) { height -> height } + fadeIn(fadeSpec)).togetherWith(
                                    slideOutVertically(animSpec) { height -> -height } + fadeOut(fadeSpec)
                                )
                            } else {
                                (slideInVertically(animSpec) { height -> -height } + fadeIn(fadeSpec)).togetherWith(
                                    slideOutVertically(animSpec) { height -> height } + fadeOut(fadeSpec)
                                )
                            }
                        },
                        label = "MonthAnim"
                    ) { targetMonth ->
                        Text(
                            text = "${targetMonth.month.getDisplayName(java.time.format.TextStyle.FULL, if(lang=="ua") Locale("uk") else Locale.ENGLISH).replaceFirstChar { it.uppercase() }} ${targetMonth.year}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(36.dp).background(Zinc900, CircleShape).clickable {
                            isNext = false // Гортаємо назад
                            yearMonth = yearMonth.minusMonths(1)
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                        }
                        Box(modifier = Modifier.size(36.dp).background(Zinc900, CircleShape).clickable {
                            isNext = true // Гортаємо вперед
                            yearMonth = yearMonth.plusMonths(1)
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                        }
                    }
                }

                // --- ДНІ ТИЖНЯ ---
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Zinc500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // --- ШВИДКА АНІМАЦІЯ СІТКИ КАЛЕНДАРЯ (Вліво/Вправо) ---
                AnimatedContent(
                    targetState = yearMonth,
                    transitionSpec = {
                        val animSpec = tween<IntOffset>(150, easing = FastOutSlowInEasing)
                        val fadeSpec = tween<Float>(150)

                        if (isNext) {
                            (slideInHorizontally(animSpec) { width -> width } + fadeIn(fadeSpec)).togetherWith(
                                slideOutHorizontally(animSpec) { width -> -width } + fadeOut(fadeSpec)
                            )
                        } else {
                            (slideInHorizontally(animSpec) { width -> -width } + fadeIn(fadeSpec)).togetherWith(
                                slideOutHorizontally(animSpec) { width -> width } + fadeOut(fadeSpec)
                            )
                        }
                    },
                    label = "GridAnim"
                ) { targetMonth ->

                    // Розрахунок днів саме для місяця, який зараз малюється в анімації
                    val daysInMonth = remember(targetMonth) {
                        val days = mutableListOf<LocalDate?>()
                        val firstOfMonth = targetMonth.atDay(1)
                        val daysInMonthVal = targetMonth.lengthOfMonth()
                        val firstDayOfWeek = firstOfMonth.dayOfWeek.value
                        repeat(firstDayOfWeek - 1) { days.add(null) }
                        for (i in 1..daysInMonthVal) { days.add(targetMonth.atDay(i)) }
                        days
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(daysInMonth) { date ->
                            if (date == null) {
                                Spacer(modifier = Modifier.aspectRatio(1f))
                            } else {
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()
                                val hasEvent = eventDates.contains(date)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Zinc900)
                                        .border(
                                            width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                                            color = if (isSelected) Color.White else if (isToday) Zinc500 else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedDate = date
                                            showTasksSheet = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            color = if (isToday && !isSelected) BlueAction else Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        )

                                        if (hasEvent) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(if (isSelected) Color.White else BlueAction, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // Кінець AnimatedContent сітки

                Spacer(modifier = Modifier.height(24.dp))

                // --- ПЛАШКА З АРХІВОМ ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📂", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(Tr.get("archived", lang), color = Zinc500, fontSize = 12.sp)
                            Text("${tasksForSelectedDate.size} завдань за ${selectedDate.dayOfMonth}.${selectedDate.monthValue}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectSelectScreen(navController: NavController, scheduleManager: ScheduleManager, lang: String) {
    val subjects = remember { scheduleManager.getUniqueSubjects(scheduleManager.getSchedule()) }

    Box(modifier = Modifier.fillMaxSize().background(BlackBg).statusBarsPadding()) {
        if (subjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Tr.get("fill_schedule_first", lang), color = Zinc500)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(subjects) { lesson ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black)
                            .clickable { navController.navigate("write_hw/${Uri.encode(lesson.subject)}/${Uri.encode(lesson.icon)}") },
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
    val isFocused by interactionSource.collectIsFocusedAsState()

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
        Toast.makeText(context, "${Tr.get("saved", lang)} -> $targetDateReal", Toast.LENGTH_SHORT).show()
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

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            val savedPath = saveImageToInternalStorage(context, tempPhotoUri!!)
            if (savedPath != null) selectedImages.add(savedPath)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
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
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera() else Toast.makeText(context, "Потрібен дозвіл!", Toast.LENGTH_LONG).show()
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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
            if (isTranscribing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = Zinc800)

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(Tr.get("recording_new", lang), color = Zinc500, fontSize = 12.sp)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
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
                                        IconButton(
                                            onClick = { selectedImages.remove(path) },
                                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.6f), CircleShape).size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(formatSeconds(recordingSeconds), color = RedDelete, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                                                audioFile?.let { SimpleAudioPlayer.play(it.absolutePath) { isPlayingPreview = false } }
                                                            }
                                                        },
                                                        onTranscribe = {
                                                            if (apiKey.isBlank()) {
                                                                Toast.makeText(context, Tr.get("no_api_key", lang), Toast.LENGTH_LONG).show()
                                                                return@TelegramAudioPlayer
                                                            }
                                                            isTranscribing = true
                                                            coroutineScope.launch {
                                                                audioFile?.let {
                                                                    val transcript = GeminiClient.transcribeAudio(it, apiKey)
                                                                    isTranscribing = false
                                                                    if (!transcript.isNullOrBlank()) {
                                                                        val separator = if (text.isNotEmpty()) " " else ""
                                                                        text += separator + transcript
                                                                    } else {
                                                                        Toast.makeText(context, "Не розпізнано", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        isTranscribing = isTranscribing,
                                                        lang = lang,
                                                        isSaved = false
                                                    )
                                                }
                                                IconButton(onClick = { SimpleAudioPlayer.stop(); audioFile = null }) {
                                                    Icon(Icons.Default.Close, null, tint = Zinc500)
                                                }
                                            }
                                        }
                                    }
                                }
                                "HIDDEN" -> { }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().background(BlackBg).navigationBarsPadding().imePadding()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCamera() else permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc700)) { Icon(Icons.Outlined.PhotoCamera, null) }
                    Button(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Zinc700)) { Icon(Icons.Outlined.Image, null) }
                    Button(
                        onClick = {
                            if (isRecording) {
                                isRecording = false
                                audioRecorder.stopRecording()
                            } else {
                                if (audioFile == null) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        isRecording = true
                                        audioFile = audioRecorder.startRecording()
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    Toast.makeText(context, "Аудіо вже є", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) RedDelete.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = if (isRecording) RedDelete else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isRecording) BorderStroke(1.dp, RedDelete) else BorderStroke(1.dp, Zinc700),
                        enabled = audioFile == null || isRecording
                    ) {
                        Icon(if (isRecording) Icons.Filled.Stop else Icons.Outlined.Mic, null)
                    }

                    val canSave = text.isNotBlank() || selectedImages.isNotEmpty() || audioFile != null
                    val saveBtnBg by animateColorAsState(targetValue = if (canSave) Color.White else Color.Transparent, animationSpec = tween(300), label = "bg")
                    val saveBtnIconColor by animateColorAsState(targetValue = if (canSave) Color.Black else Zinc700, animationSpec = tween(300), label = "icon")
                    val saveBtnBorderColor by animateColorAsState(targetValue = if (canSave) Color.Transparent else Zinc700.copy(alpha = 0.3f), animationSpec = tween(300), label = "border")

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
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Zinc500, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
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
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(10.dp))
                    Text("A", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageViewer(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val buttonBackground = Color(0xFF27272A).copy(alpha = 0.5f)

    // Анимационные стейты
    val scale = remember { Animatable(1f) }
    // ВАЖНО: Используем VectorConverter для Offset
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val shareImage = {
        try {
            val file = File(imagePath)
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
            // 1. Объявляем переменные размеров ПРЯМО ЗДЕСЬ
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(imagePath))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            coroutineScope.launch {
                                // 2. Логика расчета
                                val newScale = (scale.value * zoom).coerceIn(1f, 4f)
                                scale.snapTo(newScale)

                                // Считаем границы
                                val maxX = (screenWidth * (newScale - 1)) / 2
                                val maxY = (screenHeight * (newScale - 1)) / 2

                                val newOffset = offset.value + pan

                                // Применяем ограничения
                                offset.snapTo(
                                    Offset(
                                        x = newOffset.x.coerceIn(-maxX, maxX),
                                        y = newOffset.y.coerceIn(-maxY, maxY)
                                    )
                                )
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offset.value.x
                        translationY = offset.value.y
                    }
            )

            // Кнопки
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .systemBarsPadding()
                    .padding(top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = shareImage, modifier = Modifier.background(buttonBackground, CircleShape)) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(buttonBackground, CircleShape)) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun EmojiPickerDialog(onDismiss: () -> Unit, onEmojiSelected: (String) -> Unit, lang: String) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Zinc900), modifier = Modifier.fillMaxWidth().height(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Tr.get("choose_icon", lang), color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(availableEmojis) { emoji -> Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Zinc800).clickable { onEmojiSelected(emoji) }, contentAlignment = Alignment.Center) { Text(emoji, fontSize = 32.sp) } } }
            }
        }
    }
}

@Composable
fun RecordingVisualizer(isRecording: Boolean) {
    Row(modifier = Modifier.height(40.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (!isRecording) { Box(Modifier.height(2.dp).width(100.dp).background(Zinc500, CircleShape)) }
        else { val infiniteTransition = rememberInfiniteTransition(label = "wave"); repeat(10) { index -> val height by infiniteTransition.animateFloat(initialValue = 4f, targetValue = if (index % 2 == 0) 30f else 15f, animationSpec = infiniteRepeatable(animation = tween(300 + (index * 50), easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "bar$index"); Box(modifier = Modifier.padding(horizontal = 2.dp).width(4.dp).height(height.dp).background(RedDelete, CircleShape)) } }
    }
}

@Composable
fun PlaybackVisualizer() {
    Row(verticalAlignment = Alignment.CenterVertically) { val infiniteTransition = rememberInfiniteTransition(label = "play"); repeat(5) { index -> val height by infiniteTransition.animateFloat(initialValue = 4f, targetValue = 16f, animationSpec = infiniteRepeatable(animation = tween(300 + (index * 70), easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "playBar$index"); Box(modifier = Modifier.padding(horizontal = 1.dp).width(3.dp).height(height.dp).background(Color.White, CircleShape)) } }
}

fun formatSeconds(seconds: Long): String { val m = seconds / 60; val s = seconds % 60; return String.format("%02d:%02d", m, s) }

@Composable
fun AsyncImagePreview(imagePath: String, onClick: () -> Unit = {}) { val context = LocalContext.current; AsyncImage(model = ImageRequest.Builder(context).data(File(imagePath)).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).clickable { onClick() }) }

enum class ViewMode {
    WEEK, LIST
}