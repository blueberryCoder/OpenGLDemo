package com.blueberry.gl

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueberry.gl.databinding.ActMainBinding
import com.blueberry.gl.triangle.TriangleActivity
import com.blueberry.gl.utils.Logger

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private lateinit var binding: ActMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate ,currentThread:${Thread.currentThread()}")
        binding = ActMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTriangle.setOnClickListener {
            startActivity(Intent(this, TriangleActivity::class.java))
        }
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val deviceConfigurationInfo = activityManager.deviceConfigurationInfo
        val openGlVersion = deviceConfigurationInfo.glEsVersion

        Logger.d(TAG, "OpenGL Version: $openGlVersion")
    }

}
