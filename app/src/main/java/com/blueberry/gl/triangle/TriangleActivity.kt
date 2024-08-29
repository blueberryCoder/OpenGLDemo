package com.blueberry.gl.triangle

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blueberry.gl.R
import com.blueberry.gl.databinding.ActTriangleBinding

class TriangleActivity : AppCompatActivity() {
    private lateinit var binding: ActTriangleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActTriangleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSurfaceView()
    }

    private fun initSurfaceView() {
        // 必须在setRenderer之前设置EGLContextClientVersion，否则会native crash
        binding.glSurfaceView.setEGLContextClientVersion(2)
        binding.glSurfaceView.setRenderer(TriangleRenderer());
//        binding.glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

    }
}