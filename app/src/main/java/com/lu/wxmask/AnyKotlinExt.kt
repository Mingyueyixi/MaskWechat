package com.lu.wxmask


fun Any?.toElseString(defaultValue: String): String {
    return this?.toString() ?: defaultValue
}

fun Any?.toElseEmptyString(): String {
    return toElseString("")
}