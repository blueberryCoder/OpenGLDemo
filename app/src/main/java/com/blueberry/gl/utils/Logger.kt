package com.blueberry.gl.utils

import android.util.Log

object Logger {
    private const val TAG = "GL"

    fun d(tag: String, msg: String) {
        Log.d(TAG, "$tag: $msg")
    }

    fun i(tag: String, msg: String) {
        Log.i(TAG, "$tag: $msg")
    }

    fun e(tag: String, msg: String) {
        Log.e(TAG, "$tag: $msg")
    }
}