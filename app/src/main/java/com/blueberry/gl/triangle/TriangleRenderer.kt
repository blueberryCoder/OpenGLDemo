package com.blueberry.gl.triangle

import android.opengl.GLES30
import android.opengl.GLSurfaceView.Renderer
import com.blueberry.gl.utils.GL20Util
import com.blueberry.gl.utils.GL30Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer : Renderer {

    companion object {
        private const val TAG = "TriangleRenderer"
    }

    private var vertex = floatArrayOf(
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.5f, 0.0f
    )
    private var color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertex)
            position(0)
        }
    private var colorBuffer: FloatBuffer = ByteBuffer.allocateDirect(color.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(color)
            position(0)
        }

    private val vertexShaderCode = """
        #version 300 es
        layout(location = 0) in vec3 vPosition;
        void main() {
            gl_Position = vec4(vPosition, 1.0);
        }
    """.trimIndent()

//    private val fragmentShaderCode = """
//        #version 300 es
//        precision mediump float;
//        uniform vec4 vColor;
//        out vec4 fragColor;
//        void main() {
//            fragColor = vColor;
//        }
//    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        out vec4 fragColor;
        void main() {
            fragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
    """.trimIndent()

    private var program: Int = 0
    private var vbo: Int = 0


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        val vertexShader = GL30Util.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GL30Util.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(program)
            throw RuntimeException(info)
        }

        if (program != 0) {
            //  生成VBO
            val vboArr = IntArray(1)
            GLES30.glGenBuffers(1, vboArr, 0)
            vbo = vboArr[0]
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                vertex.size * 4,
                vertexBuffer,
                GLES30.GL_STATIC_DRAW
            )
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        if (program != 0) {
            GLES30.glUseProgram(program)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
            GLES30.glEnableVertexAttribArray(0)
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 4 * 3, 0)
//            val colorHandle = GLES30.glGetUniformLocation(program, "vColor")
//            GLES30.glUniform4fv(colorHandle, 1, colorBuffer)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
            GLES30.glDisableVertexAttribArray(0)
        }
    }
}