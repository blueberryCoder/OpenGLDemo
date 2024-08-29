package com.blueberry.gl.triangle

import android.opengl.GLES20
import android.opengl.GLSurfaceView.Renderer
import com.blueberry.gl.utils.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer : Renderer {

    companion object {
        private const val TAG = "TriangleRenderer"
    }

    private var vertex = floatArrayOf(
        -0.5f, -0.5f , 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.5f , 0.0f
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
        attribute vec4 vPosition;
        void main() {
            gl_Position = vPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """.trimIndent()

    private var program: Int = 0

    private fun loadShader(type: Int, source: String): Int {
        var shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val intArr = intArrayOf(1);
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

    private fun checkGLError(op:String) {
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
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (program != 0) {
            GLES20.glUseProgram(program)
            checkGLError("glUseProgram")
            val position = GLES20.glGetAttribLocation(program, "vPosition")
            checkGLError("glGetAttribLocation")
            GLES20.glEnableVertexAttribArray(position)
            checkGLError("glEnableVertexAttribArray")
            GLES20.glVertexAttribPointer(
                position, 3, GLES20.GL_FLOAT, false,
                4 *3, vertexBuffer
            )
            checkGLError("glVertexAttribPointer")
            val colorHandle = GLES20.glGetUniformLocation(program, "vColor");
            checkGLError("glGetUniformLocation")
//            GLES20.glUniform4fv(colorHandle,1, color,0)
            GLES20.glUniform4fv(colorHandle, 1, colorBuffer)
            checkGLError("glUniform3fv")
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
            checkGLError("glDrawArrays")
            GLES20.glDisableVertexAttribArray(position)
            checkGLError("glDisableVertexAttribArray")
        }
    }
}