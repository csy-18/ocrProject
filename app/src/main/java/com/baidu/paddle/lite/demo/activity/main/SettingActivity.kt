package com.baidu.paddle.lite.demo.activity.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var work: Handler

    companion object {
        var TAG = SettingActivity.javaClass.simpleName
        const val GET_VERSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initHandler()
    }

    private fun initHandler() {
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    GET_VERSION -> {
                        Log.i(TAG, "handleMessage: GET_VERSION")

                    }
                }
            }
        }
    }

    private fun initViews() {
        binding.button4.setOnClickListener {
            Thread{
                val version = OdooUtils.getVersion()
                Log.i(TAG, "initViews: $version")
            }.start()
        }
    }
}