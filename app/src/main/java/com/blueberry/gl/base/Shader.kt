package com.blueberry.gl.base

import android.opengl.GLES30
import com.blueberry.gl.utils.GL30Util

class Shader(
    vertexShaderCode:String,
    fragmentShaderCode:String) {

    private var program: Int = 0

    private val uniformCache = HashMap<String,Int>()

    init {

        val vertexShader = GL30Util.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GL30Util.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
            GL30Util.checkLinkProgram(it)
        }
    }

    fun bind() {
        GLES30.glUseProgram(program)
    }

    fun unBind() {
        GLES30.glUseProgram(0)
    }

    fun delete() {
        GLES30.glDeleteProgram(program)
    }

    fun setUniform1i(name: String, value: Int) {
        val location = getUniformLocation(name)
        GLES30.glUniform1i(location, value)
    }

    private fun getUniformLocation(name: String): Int {
        return uniformCache.getOrPut(name) {
            GLES30.glGetUniformLocation(program, name)
        }
    }
}