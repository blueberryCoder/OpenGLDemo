package com.blueberry.gl.yuv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueberry.gl.databinding.ActYuvBinding

class YuvActivity : AppCompatActivity() {
    private lateinit var binding: ActYuvBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActYuvBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSurfaceView()
    }

    private fun initSurfaceView() {
        binding.surfaceView.setEGLContextClientVersion(3)
        binding.surfaceView.setRenderer(YuvTextureRenderer(this))
    }
}