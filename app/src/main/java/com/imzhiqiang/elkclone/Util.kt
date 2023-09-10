package com.imzhiqiang.elkclone

import android.content.Context
import android.icu.text.NumberFormat
import android.widget.Toast
import androidx.annotation.StringRes
import java.text.ParseException
import java.util.Locale

fun Context.toast(@StringRes resId: Int, vararg formatArgs: Any?) {
    Toast.makeText(this, getString(resId, formatArgs), Toast.LENGTH_SHORT).show()
}

fun Double.formatToStr(): String {
    if (this == 0.0) return ""
    val numberFormat = NumberFormat.getInstance(Locale.US)
    numberFormat.isGroupingUsed = false
    numberFormat.maximumFractionDigits = 2
    return numberFormat.format(this)
}

fun String.parseToDouble(): Double {
    val numberFormat = NumberFormat.getInstance(Locale.US)
    numberFormat.isGroupingUsed = false
    numberFormat.maximumFractionDigits = 2
    val number = try {
        numberFormat.parse(this)
    } catch (e: ParseException) {
        null
    }
    return number?.toDouble() ?: 0.0
}