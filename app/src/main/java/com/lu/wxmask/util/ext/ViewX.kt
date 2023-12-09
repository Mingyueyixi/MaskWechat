package com.lu.wxmask.util.ext

import android.view.View

fun View.setPadding(padding: Int) {
    this.setPadding(padding, padding, padding, padding)
}