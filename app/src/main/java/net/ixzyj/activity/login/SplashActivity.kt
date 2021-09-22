package net.ixzyj.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sychen.basic.activity.BaseActivity
import kotlinx.coroutines.launch
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.ocr.R
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.logi

class SplashActivity : BaseActivity() {
    private val loginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initViews()
    }

    private fun initViews() {
        window.navigationBarColor = 0x1AC0C0C0
        lifecycleScope.launch {
            loginViewModel.version(this@SplashActivity).observe(this@SplashActivity,{
                if (!it.equals("")){
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }else{
                    DialogUtil.alertDialog("获取版本失败\n跳转到设置页面\n重新设置服务器地址", this@SplashActivity).apply {
                        setOnDismissListener {
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    SetDBActivity::class.java
                                )
                            )
                            finish()
                        }
                    }
                }
            })
        }
    }
}