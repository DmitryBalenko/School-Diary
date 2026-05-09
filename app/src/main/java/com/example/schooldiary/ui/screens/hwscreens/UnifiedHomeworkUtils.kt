package com.example.schooldiary.ui.screens.hwscreens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.schooldiary.Homework
import java.time.LocalDate
import java.time.LocalTime

enum class ViewMode { WEEK, LIST }

fun isOverdue(hw: Homework): Boolean {
    if (hw.isArchived) return false
    val tDateStr = hw.targetDate?.trim() ?: return false
    return try {
        val cleanStr =
            if (tDateStr.contains("T")) tDateStr.substringBefore("T") else tDateStr.substringBefore(
                " "
            )
        val tDate = LocalDate.parse(cleanStr)
        val today = LocalDate.now()

        if (tDate.isBefore(today)) return true
        if (tDate.isEqual(today) && LocalTime.now().hour >= 16) return true

        false
    } catch (e: Exception) {
        false
    }
}

fun rotationIn() = fadeIn(tween(200)) + scaleIn(initialScale = 0.5f)
fun rotationOut() = fadeOut(tween(200)) + scaleOut(targetScale = 0.5f)

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)