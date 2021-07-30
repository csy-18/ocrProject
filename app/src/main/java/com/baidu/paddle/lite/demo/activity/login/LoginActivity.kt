package com.baidu.paddle.lite.demo.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.activity.main.SettingActivity
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityLoginBinding
import com.sychen.basic.activity.BaseActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.button2.setOnClickListener {
            startActivity(Intent(this,CameraActivity::class.java))
        }
        binding.button3.setOnClickListener {
            startActivity(Intent(this,SettingActivity::class.java))
        }
    }
}