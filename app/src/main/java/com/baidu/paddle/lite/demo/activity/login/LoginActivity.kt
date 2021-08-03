package com.baidu.paddle.lite.demo.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.baidu.paddle.lite.demo.activity.main.OcrMainActivity
import com.baidu.paddle.lite.demo.activity.setting.FileActivity
import com.baidu.paddle.lite.demo.activity.setting.SetDBActivity
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.network.OdooUtils.userLogin
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityLoginBinding
import com.baidu.paddle.lite.demo.utils.DialogUtil
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.isDebug
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.baidu.paddle.lite.demo.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var work: Handler
    private val odooUtils = OdooUtils()

    companion object {
        const val USER_LOGIN_SUCCESS = 0
        const val USER_LOGIN_FAILED = 1
        var USER_ID = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHandler()
        initViews()
    }

    private fun initHandler() {
        work = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    USER_LOGIN_SUCCESS -> {
                        loginSuccess()
                    }
                    USER_LOGIN_FAILED -> {
                        loginFailed()
                    }
                }
            }
        }
    }

    private fun loginSuccess() {
        binding.progressBarLogin.visibility = View.INVISIBLE
        OdooUtils.uid = USER_ID.toString()
        OdooUtils.username = binding.userNameEdit.text.toString()
        OdooUtils.password = binding.pwdEdit.text.toString()
        val checked = binding.checkBox.isChecked
        when (checked) {
            true -> checkedTrue()
            false -> checkedFalse()
        }
    }

    private fun checkedFalse() {
        SharedPreferencesUtil.sharedPreferencesSave("LOGIN_ISCHECKED", false)
        startActivity(Intent(this@LoginActivity, OcrMainActivity::class.java))
        finish()
    }

    private fun checkedTrue() {
        val username = binding.userNameEdit.text.toString()
        val pwd = binding.pwdEdit.text.toString()
        SharedPreferencesUtil.sharedPreferencesSave("USER_NAME", username)
        SharedPreferencesUtil.sharedPreferencesSave("PASS_WORD", pwd)
        SharedPreferencesUtil.sharedPreferencesSave("USER_ID", USER_ID)
        SharedPreferencesUtil.sharedPreferencesSave("LOGIN_ISCHECKED", true)
        OdooUtils.url.logi()
        startActivity(Intent(this@LoginActivity, OcrMainActivity::class.java))
        finish()
    }

    private fun loginFailed() {
        Thread {
            Looper.prepare()
            binding.progressBarLogin.visibility = View.INVISIBLE
            DialogUtil.alertDialog("登录失败",this)
            Looper.loop()
        }.start()
    }

    private fun initViews() {
        binding.toolbarLogin.apply {
            this.inflateMenu(R.menu.mymenu)
            setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.set_menu -> {
                        startActivity(Intent(this@LoginActivity,SetDBActivity::class.java))
                    }
                    R.id.set_file ->{
                        startActivity(Intent(this@LoginActivity,FileActivity::class.java))
                    }
                }
                isDebug
            }
        }
        binding.progressBarLogin.visibility = View.INVISIBLE
        val loginIsChecked =
            SharedPreferencesUtil.sharedPreferencesLoad("LOGIN_ISCHECKED",false)
        when (loginIsChecked) {
            true -> {
                binding.userNameEdit.setText(
                    SharedPreferencesUtil.sharedPreferencesLoad("USER_NAME","").toString()
                )
                binding.pwdEdit.setText(
                    SharedPreferencesUtil.sharedPreferencesLoad("PASS_WORD","").toString()
                )
                binding.checkBox.isChecked = true
            }
            false -> {
            }
        }
        binding.loginBtn.setOnClickListener {
            binding.progressBarLogin.visibility = View.VISIBLE
            val userName = binding.userNameEdit.text.toString()
            val pwd = binding.pwdEdit.text.toString()
            if (userName == "" || pwd == "") {
                "未输入数据，请输入数据重试".showToast(this)
                return@setOnClickListener
            }
            Thread {
                val uid = userLogin(userName, pwd)
                USER_ID = uid
                "用户id-$USER_ID".logi()
                when (uid) {
                    -1 -> work.sendEmptyMessage(USER_LOGIN_FAILED)
                    else -> work.sendEmptyMessage(USER_LOGIN_SUCCESS)
                }
            }.start()
        }
    }

}