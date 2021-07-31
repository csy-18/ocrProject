package com.baidu.paddle.lite.demo.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.baidu.paddle.lite.demo.ocr.R
import com.sychen.basic.activity.BaseActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //当存在导航栏的时候防止导航栏遮住背景图片用的,取代了window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)过时方法
        window.navigationBarColor = 0x1AC0C0C0
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1000)
    }
}