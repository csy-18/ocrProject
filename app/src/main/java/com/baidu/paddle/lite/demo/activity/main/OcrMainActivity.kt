package com.baidu.paddle.lite.demo.activity.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityOcrMainBinding

class OcrMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityOcrMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.inStock.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_low)
            it.startAnimation(animation)
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

}