package com.example.schooldiary

import androidx.compose.ui.graphics.Color
import java.util.UUID

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