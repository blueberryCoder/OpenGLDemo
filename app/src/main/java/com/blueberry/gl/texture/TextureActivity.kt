package com.blueberry.gl.texture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blueberry.gl.databinding.ActTextureBinding

class TextureActivity : AppCompatActivity() {

    private lateinit var binding: ActTextureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActTextureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSurfaceView()
    }

   private fun initSurfaceView() {
      binding.surfaceView.setEGLContextClientVersion(3)
      binding.surfaceView.setRenderer(TextureRenderer(this))
   }
}