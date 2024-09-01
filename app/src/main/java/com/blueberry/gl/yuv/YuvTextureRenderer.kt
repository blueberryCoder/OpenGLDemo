package com.blueberry.gl.yuv

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.blueberry.gl.utils.GL30Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class YuvTextureRenderer(private val context: YuvActivity) : GLSurfaceView.Renderer {

    private val vertexShaderCode = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;
        layout(location = 1) in vec2 a_textCoord;
        out vec2 v_textCoord;
        void main() {
            gl_Position = vPosition;
            v_textCoord = a_textCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 v_textCoord;
        layout(location = 0) out vec4 outColor;
        uniform sampler2D y_texture;
        uniform sampler2D uv_texture;
        void main() {
            vec3 yuv;
            yuv.x = texture(y_texture, v_textCoord).r;
            yuv.y = texture(uv_texture, v_textCoord).a - 0.5;
            yuv.z = texture(uv_texture, v_textCoord).r - 0.5;
            highp vec3 rgb = mat3(1, 1, 1, 
                0, -0.344, 1.770,
                1.403, -0.714, 0
            ) * yuv;
            outColor = vec4(rgb, 1.0);
        }
    """.trimIndent()

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
    private var indices = intArrayOf(0, 1, 2, 0, 2, 3)

    private val indicesBuffer = ByteBuffer.allocateDirect(indices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()
        .apply {
            put(indices)
            position(0)
        }

    private var program: Int = 0
    private var vao: Int = 0
    private var vbo: Int = 0
    private var ibo: Int = 0
    private var yTexture: Int = 0
    private var uvTexture: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val vertexShader = GL30Util.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GL30Util.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        // 检查链接状态
        val intArr = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, intArr, 0)
        if (intArr[0] == GLES30.GL_FALSE) {
            val str = GLES30.glGetProgramInfoLog(program)
            throw RuntimeException("Load program failed: $str")
        }

        val vaoArr = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        vao = vaoArr[0]

        val vboArr = IntArray(2)
        GLES30.glGenBuffers(2, vboArr, 0)
        vbo = vboArr[0]
        ibo = vboArr[1]

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertex.size * 4 + textureCoords.size * 4,
            null,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, vertex.size * 4, vertexBuffer)
        GLES30.glBufferSubData(
            GLES30.GL_ARRAY_BUFFER,
            vertex.size * 4,
            textureCoords.size * 4,
            textureCoordsBuffer
        )

        GLES30.glVertexAttribPointer(
            0,
            3,
            GLES30.GL_FLOAT,
            false,
            3 * 4,
            0
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 2 * 4, vertex.size * 4)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * 4,
            indicesBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glBindVertexArray(GLES30.GL_NONE)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, GLES30.GL_NONE)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, GLES30.GL_NONE)

        val textureArr = IntArray(2)
        GLES30.glGenTextures(2, textureArr, 0)
        yTexture = textureArr[0]
        uvTexture = textureArr[1]

        val inStream = context.assets.open("YUV_Image_840x1074.NV21")
        var nv21Data : ByteArray
        inStream.use {
            val len = inStream.available()
            nv21Data = ByteArray(len)
            inStream.read(nv21Data)
        }
        val nv32Buffer = ByteBuffer.allocateDirect(nv21Data.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(nv21Data)
                position(0)
            }

        // 840 * 1074 = 902160
        // 840 * 1074 / 2 = 1353240
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, yTexture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, 840, 1074, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, nv32Buffer)
        nv32Buffer.position(840 * 1074)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, uvTexture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, 840/2, 1074/2, 0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, nv32Buffer)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(program)
        GLES30.glBindVertexArray(vao)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, yTexture)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, uvTexture)

        val ySampler  = GLES30.glGetUniformLocation(program, "y_texture")
        val uvSampler = GLES30.glGetUniformLocation(program, "uv_texture")

        GLES30.glUniform1i(ySampler, 0)
        GLES30.glUniform1i(uvSampler, 1)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glBindVertexArray(GLES30.GL_NONE)
    }
}