package net.ixzyj.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.activity.setting.FileActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.OdooUtils.userLogin
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.ActivityLoginBinding
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.isDebug
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.MyApplication.Companion.showToast
import net.ixzyj.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.utils.AESUtil

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var work: Handler
    private val odooUtils = OdooUtils()

    companion object {
        const val USER_LOGIN_SUCCESS = 0
        const val USER_LOGIN_FAILED = 1
        const val SECRE_KEY = "ixzyjnetixzyjnet"
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
        val encryptPwd = AESUtil.encrypt(SECRE_KEY, pwd)
        SharedPreferencesUtil.sharedPreferencesSave("USER_NAME", username)
        SharedPreferencesUtil.sharedPreferencesSave("PASS_WORD", encryptPwd)
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
            DialogUtil.alertDialog("登录失败", this)
            Looper.loop()
        }.start()
    }

    private fun initViews() {
        binding.set.apply {
            setOnClickListener {
                startActivity(Intent(this@LoginActivity, SetDBActivity::class.java))
            }
            if (isDebug) {
                visibility = View.VISIBLE
            } else {
                visibility = View.INVISIBLE
            }
        }
        binding.progressBarLogin.visibility = View.INVISIBLE
        val loginIsChecked =
            SharedPreferencesUtil.sharedPreferencesLoad("LOGIN_ISCHECKED", false)
        when (loginIsChecked) {
            true -> {
                binding.userNameEdit.setText(
                    SharedPreferencesUtil.sharedPreferencesLoad("USER_NAME", "").toString()
                )
                val pwd =
                    SharedPreferencesUtil.sharedPreferencesLoad("PASS_WORD", "").toString()
                binding.pwdEdit.setText(
                    AESUtil.decrypt(SECRE_KEY, pwd)
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
                USER_ID = userLogin(userName, pwd)
                when (USER_ID) {
                    -1 -> work.sendEmptyMessage(USER_LOGIN_FAILED)
                    else -> work.sendEmptyMessage(USER_LOGIN_SUCCESS)
                }
            }.start()
        }
    }

}