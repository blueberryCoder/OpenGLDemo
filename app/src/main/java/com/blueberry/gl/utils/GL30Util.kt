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
            val shaderType = when (type) {
                GLES30.GL_VERTEX_SHADER -> "vertex"
                GLES30.GL_FRAGMENT_SHADER -> "fragment"
                else -> "unknown"
            }
            Logger.e(TAG, "load $shaderType shader failed $info")
            GLES30.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    fun checkLinkProgram(program: Int) {
        // 检查链接状态
        val intArr = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, intArr, 0)
        if (intArr[0] == GLES30.GL_FALSE) {
            val str = GLES30.glGetProgramInfoLog(program)
            throw RuntimeException("Load program failed: $str")
        }
    }

}