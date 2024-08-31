package com.blueberry.gl.utils

import android.opengl.GLES20

object GL20Util {
    private const val TAG = "GLUtil"

    fun checkGLError(op:String) {
        var error  = GLES20.glGetError()

        val sb = StringBuffer()
        while (error != GLES20.GL_NO_ERROR) {
            sb.append("$op error: $error \n")
            error = GLES20.glGetError()
        }
        if (sb.isNotEmpty()) {
            throw RuntimeException("$op $sb")
        }
    }

    fun loadShader(type: Int, source: String): Int {
        var shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val intArr = intArrayOf(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, intArr, 0)
        val success = intArr[0]
        if (success == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            Logger.e(TAG, "load shader failed $info")
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

}