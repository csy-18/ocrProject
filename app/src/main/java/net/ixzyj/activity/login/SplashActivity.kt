package net.ixzyj.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import net.ixzyj.network.OdooUtils
import net.ixzyj.ocr.R
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.logi
import com.sychen.basic.activity.BaseActivity

class SplashActivity : BaseActivity() {
    private lateinit var work: Handler

    companion object {
        private const val VERSION_SUCCESS = 0
        private const val VERSION_FAILED = 1
        private var version = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initHandler()
        initViews()

    }

    private fun initHandler() {
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    VERSION_SUCCESS -> {

                    }
                    VERSION_FAILED -> {
                        DialogUtil.alertDialog("服务器错误，请联系开发人员", this@SplashActivity)
                    }
                }
            }
        }
    }

    private fun initViews() {
        window.navigationBarColor = 0x1AC0C0C0
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
            Thread {
                //测试版本
                version = OdooUtils.getVersion()
                if (version == "") {
                    "版本$version".logi()
                    work.sendEmptyMessage(VERSION_FAILED)
                }
                version.logi()
                work.sendEmptyMessage(VERSION_SUCCESS)
            }.start()
        }, 1000)
    }
}