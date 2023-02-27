package com.lu.wxmask.util

//超期计算器
class TimeExpiredCalculator(val expiredMills: Long) {
    var lastTime = 0L

    @JvmOverloads
    fun updateLastTime(newTIme: Long = System.currentTimeMillis()) {
        lastTime = newTIme
    }

    fun isExpiredAuto(): Boolean {
        return isExpiredInternal(true)
    }

    fun isExpired(): Boolean {
        return isExpiredInternal(false)
    }

    private fun isExpiredInternal(autoUpdate: Boolean): Boolean {
        val curr = System.currentTimeMillis()
        val result = curr - lastTime > expiredMills
        if (result && autoUpdate) {
            updateLastTime(curr)
        }
        return result
    }
}