package com.blueberry.gl.base

import android.opengl.GLES30
import java.nio.Buffer

class VertexBuffer {

    private val vboArr = IntArray(1)

    constructor(data: Buffer, size: Int) {
        GLES30.glGenBuffers(1, vboArr, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, size, data, GLES30.GL_STATIC_DRAW)
    }

    fun bind() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[0])
    }

    fun unBind() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    fun delete() {
        GLES30.glDeleteBuffers(1, vboArr,0)
    }
}