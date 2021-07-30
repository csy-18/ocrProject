package com.baidu.paddle.lite.demo.activity.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.MyApplication
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.bumptech.glide.Glide

class ResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        Glide.with(this)
            .load(predictor.outputImage())
            .into(binding.imgResult)
        binding.textResult.text = predictor.outputResult()
    }
}