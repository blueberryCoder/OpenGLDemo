package com.blueberry.gl.texture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView.Renderer
import com.blueberry.gl.R
import com.blueberry.gl.utils.GL30Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TextureRenderer(private val context: Context) : Renderer {
    private var vertex = floatArrayOf(
        -1.0f, 0.5f, 0.0f,
        -1.0f, -0.5f, 0.0f,
        1.0f, -0.5f, 0.0f,
        1.0f, 0.5f, 0.0f
    )

    private var textureCoords = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertex)
            position(0)
        }

    private var textureCoordsBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(textureCoords)
            position(0)
        }

    private var indices = intArrayOf(0, 1, 2, 0, 2,3)

    private val indicesBuffer = ByteBuffer.allocateDirect(indices.size *4)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()
        .apply {
            put(indices)
            position(0)
        }

    private var program: Int = 0
    private var vbo = 0
    private var vao = 0
    private var ebo = 0
    private var texture = 0

    private val vertexShaderCode = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;
        layout(location = 1) in vec2 textCoord;
        out vec2 v_textCoord;
        void main() {
            gl_Position = vPosition;
            v_textCoord = textCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 v_textCoord;
        out vec4 fragColor;
        uniform sampler2D s_TextureMap;
        void main() {
            fragColor = texture(s_TextureMap, v_textCoord);
        }
    """.trimIndent()


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1.0f)

        val vertexShader = GL30Util.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GL30Util.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }

        // 检查链接状态
        val intArr = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, intArr, 0)
        if (intArr[0] == GLES30.GL_FALSE) {
            val str = GLES30.glGetProgramInfoLog(program)
            throw RuntimeException("Load program failed: $str")
        }

        val vboArr = IntArray(2)
        GLES30.glGenBuffers(2, vboArr, 0)
        vbo = vboArr[0]
        ebo = vboArr[1]

        val vaoArr = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        vao = vaoArr[0]

        GLES30.glBindVertexArray(vao)

        // 绑定 VBO 并上传数据
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertex.size * 4 + textureCoords.size * 4,null, GLES30.GL_STATIC_DRAW)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, vertex.size * 4, vertexBuffer)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, vertex.size * 4,textureCoords.size *4, textureCoordsBuffer)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1,2, GLES30.GL_FLOAT, false, 2 * 4, vertex.size * 4)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size *4, indicesBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0) // 解绑 VBO
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)

        val textureArr = IntArray(1)
        GLES30.glGenTextures(1, textureArr,0)
        texture = textureArr[0]
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        val bitmap  = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

        val bitmapBuffer = ByteBuffer.allocateDirect(bitmap.byteCount).order(ByteOrder.nativeOrder())
        bitmap.copyPixelsToBuffer(bitmapBuffer)
        bitmapBuffer.flip()

        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA,
            bitmap.width,
            bitmap.height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            bitmapBuffer
        )
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(program)
        GLES30.glBindVertexArray(vao)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)

        val samplerUniform = GLES30.glGetUniformLocation(program, "s_TextureMap")
        GLES30.glUniform1i(samplerUniform, 0)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
    }
}