package com.blueberry.gl.base

import android.opengl.GLES30

class VertexBufferLayout {
    class VertexBufferElement(
        val type: Int,
        val count: Int,
        val normalized: Boolean
    ) {
        companion object {
            fun getSizeOfType(type: Int): Int {
                return when (type) {
                    GLES30.GL_FLOAT -> 4
                    GLES30.GL_UNSIGNED_INT -> 4
                    GLES30.GL_UNSIGNED_BYTE -> 1
                    else -> throw RuntimeException("unknown type")
                }
            }
        }
    }

    private val elements = mutableListOf<VertexBufferElement>()
    private var stride = 0

    fun getElements(): List<VertexBufferElement> {
        return elements
    }

    fun getStride(): Int {
        return stride
    }

    fun pushFloat(count: Int) {
        elements.add(VertexBufferElement(GLES30.GL_FLOAT, count, false))
        stride += count * VertexBufferElement.getSizeOfType(GLES30.GL_FLOAT)
    }
    fun pushInt(count: Int) {
        elements.add(VertexBufferElement(GLES30.GL_UNSIGNED_INT, count, false))
        stride += count * VertexBufferElement.getSizeOfType(GLES30.GL_UNSIGNED_INT)
    }
    fun pushByte(count:Int) {
        elements.add(VertexBufferElement(GLES30.GL_UNSIGNED_BYTE, count, false))
        stride += count * VertexBufferElement.getSizeOfType(GLES30.GL_UNSIGNED_BYTE)
    }
    inline fun <reified T> push(count: Int) {
        when (T::class) {
            Float::class -> pushFloat(count)
            Int::class -> pushInt(count)
            Byte::class -> pushByte(count)
            else -> throw RuntimeException("unknown type")
        }
    }

}