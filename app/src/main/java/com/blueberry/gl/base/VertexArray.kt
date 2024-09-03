package com.blueberry.gl.base

import android.opengl.GLES30

class VertexArray {

    private val vaoArr = IntArray(1)

    constructor() {
        GLES30.glGenVertexArrays(1, vaoArr, 0)
    }

    fun bind() {
        GLES30.glBindVertexArray(vaoArr[0])
    }

    fun unBind() {
        GLES30.glBindVertexArray(0)
    }

    fun delete() {
        GLES30.glDeleteVertexArrays(1, vaoArr, 0)
    }

    fun addBuffer(vbo: VertexBuffer, layout: VertexBufferLayout) {
        bind()
        vbo.bind()
        val elements = layout.getElements()
        var offset = 0
        for (i in elements.indices) {
            val element = elements[i]
            GLES30.glEnableVertexAttribArray(i)
            GLES30.glVertexAttribPointer(i, element.count, element.type, element.normalized, layout.getStride(), offset)
            offset += element.count * VertexBufferLayout.VertexBufferElement.getSizeOfType(element.type)
        }
    }
}