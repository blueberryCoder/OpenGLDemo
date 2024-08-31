package com.blueberry.gl.triangle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        binding.glSurfaceView.setEGLContextClientVersion(3)
        binding.glSurfaceView.setRenderer(TriangleRenderer())

    }
}