package com.blueberry.gl.fbo

import android.app.Activity
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.blueberry.gl.R
import com.blueberry.gl.base.Shader
import com.blueberry.gl.utils.GL30Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FboRenderer(private val context: Activity) : GLSurfaceView.Renderer{
    private val vertex = floatArrayOf(
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f,
    )
    private val vTextCoords = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )

    private val vFboTextCoords = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    )
    private val indices = intArrayOf(0, 1, 2, 1, 3, 2)
    private val indicesBuffer = ByteBuffer.allocateDirect(indices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()
        .apply {
            put(indices)
            position(0)
        }

    private val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertex)
            position(0)
        }
    private val vTextCoordsBuffer = ByteBuffer.allocateDirect(vTextCoords.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vTextCoords)
            position(0)
        }
    private val vFboTextCoordsBuffer = ByteBuffer.allocateDirect(vFboTextCoords.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vFboTextCoords)
            position(0)
        }

    private val vShaderCode = """
        #version 300 es
        layout(location = 0) in vec4 a_position;
        layout(location = 1) in vec2 a_textCoord;
        out vec2 v_textCoord;
        void main() {
            gl_Position = a_position;
            v_textCoord = a_textCoord;
        }
    """.trimIndent()
    private val fShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 v_textCoord;
        layout(location = 0) out vec4 outColor;
        uniform sampler2D s_TextureMap;
        void main() {
            outColor = texture(s_TextureMap, v_textCoord);
        }
    """.trimIndent()
    private val fboShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 v_textCoord;
        layout(location = 0) out vec4 outColor;
        uniform sampler2D s_TextureMap;
        void main() {
           vec4 tempColor = texture(s_TextureMap, v_textCoord);
           float luminance = tempColor.r * 0.299 + tempColor.g * 0.587 + tempColor.b * 0.114;
           outColor = vec4(vec3(luminance), tempColor.a);
        }
    """.trimIndent()

    private lateinit var shader : Shader
    private lateinit var fboShader : Shader

    private var vao: Int = 0
    private var fboVao: Int = 0
    private var imgTexture: Int = 0
    private var fboTexture: Int = 0
    private var fbo: Int = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var vboArr = IntArray(4)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        shader = Shader(vShaderCode, fShaderCode)
        fboShader = Shader(vShaderCode, fboShaderCode)


        GLES30.glGenBuffers(4, vboArr, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertex.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[1])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vTextCoords.size * 4, vTextCoordsBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[2])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vFboTextCoords.size * 4, vFboTextCoordsBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vboArr[3])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indicesBuffer, GLES30.GL_STATIC_DRAW)

        val vaoArr = IntArray(2)
        GLES30.glGenVertexArrays(2, vaoArr, 0)
        GL30Util.checkGLError("glGenBuffers")
        vao = vaoArr[0]
        fboVao = vaoArr[1]

        bindVao(vboArr)
        bindFboVao(vboArr)

        val textureArr = IntArray(1)
        GLES30.glGenTextures(1,textureArr , 0)

        imgTexture = textureArr[0]
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imgTexture)
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

        createFbo()
    }

    private fun bindVao(vboArr: IntArray) {
        // VAO 绑定
        GLES30.glBindVertexArray(vao)
        GL30Util.checkGLError("glBindVertexArray")
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[0])
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[1])
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vboArr[3])
        GLES30.glBindVertexArray(GLES30.GL_NONE)

        GL30Util.checkGLError("bindVao")
    }

    private fun bindFboVao(vboArr: IntArray) {
        // FBO VAO 绑定
        GLES30.glBindVertexArray(fboVao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[0])
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArr[2])
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vboArr[3])
        GLES30.glBindVertexArray(GLES30.GL_NONE)
        GL30Util.checkGLError("bindFboVao")
    }

    private fun createFbo() {
        val textureArr = IntArray(1)
        GLES30.glGenTextures(1,textureArr , 0)

        fboTexture = textureArr[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        val fboArr = IntArray(1)
        GLES30.glGenFramebuffers(1, fboArr, 0)
        fbo = fboArr[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, fboTexture, 0)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, 500, 500, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)

        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("FrameBuffer not complete.")
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        screenWidth = width
        screenHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT,1)
        GLES30.glViewport(0,0, 500, 500)
        // FBO渲染
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        fboShader.bind()
        GLES30.glBindVertexArray(fboVao)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imgTexture)
        fboShader.setUniform1i("s_TextureMap", 0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // 普通渲染
        GLES30.glViewport(0, 0, screenWidth, screenHeight)
        shader.bind()
        GLES30.glBindVertexArray(vao)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTexture)
        shader.setUniform1i("s_TextureMap", 0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }
}