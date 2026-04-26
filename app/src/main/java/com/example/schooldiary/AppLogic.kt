package com.example.schooldiary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Collator
import java.time.LocalDate
import java.util.Locale
import java.util.Scanner
import java.util.UUID
import kotlin.math.absoluteValue

// --- МЕНЕДЖЕРИ ---

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