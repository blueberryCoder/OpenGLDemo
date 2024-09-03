package com.blueberry.gl.fbo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueberry.gl.databinding.ActFboBinding

class FboActivity : AppCompatActivity() {
    private lateinit var binding: ActFboBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActFboBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSurfaceView()
    }

    private fun initSurfaceView() {
        binding.surfaceView.setEGLContextClientVersion(3)
        binding.surfaceView.setRenderer(FboRenderer(this))
    }
}