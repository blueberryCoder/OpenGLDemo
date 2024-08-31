package com.blueberry.gl.utils

import android.opengl.GLES30


object GL30Util {
    private const val TAG = "GLUtil"

    fun checkGLError(op: String) {
        var error = GLES30.glGetError()

        val sb = StringBuffer()
        while (error != GLES30.GL_NO_ERROR) {
            sb.append("$op error: $error \n")
            error = GLES30.glGetError()
        }
        if (sb.isNotEmpty()) {
            throw RuntimeException("$op $sb")
        }
    }

    fun loadShader(type: Int, source: String): Int {
        var shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val intArr = intArrayOf(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, intArr, 0)
        val success = intArr[0]
        if (success == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            Logger.e(TAG, "load shader failed $info")
            GLES30.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

}