package com.example.deltasitemanager.utils

import java.text.SimpleDateFormat
import java.util.*

fun getTodayDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
