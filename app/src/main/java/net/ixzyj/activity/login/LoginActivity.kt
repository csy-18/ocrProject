package net.ixzyj.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.network.OdooRepo
import net.ixzyj.ocr.databinding.ActivityLoginBinding
import net.ixzyj.utils.AESUtil
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.isDebug
import net.ixzyj.utils.MyApplication.Companion.showToast
import net.ixzyj.utils.SharedPreferencesUtil
import net.ixzyj.utils.SharedPreferencesUtil.sharedPreferencesSave
import java.util.*
import android.content.ComponentName
import android.net.Uri
import net.ixzyj.utils.MyApplication.Companion.logi


class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    companion object {
        const val SECRE_KEY = "ixzyjnetixzyjnet"
        var USER_ID = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun loginSuccess() {
        OdooRepo.uid = USER_ID
        OdooRepo.username = binding.userNameEdit.text.toString()
        OdooRepo.password = binding.pwdEdit.text.toString()
        when (binding.checkBox.isChecked) {
            true -> checkedTrue()
            false -> checkedFalse()
        }
    }

    private fun loginFailed() {
        DialogUtil.alertDialog("用户名或密码错误", this)
    }

    private fun checkedFalse() {
        sharedPreferencesSave("LOGIN_ISCHECKED", false)
        startActivity(Intent(this@LoginActivity, OcrMainActivity::class.java))
        finish()
    }

    private fun checkedTrue() {
        val username = binding.userNameEdit.text.toString()
        val pwd = binding.pwdEdit.text.toString()
        val encryptPwd = AESUtil.encrypt(SECRE_KEY, pwd)
        sharedPreferencesSave("USER_NAME", username)
        sharedPreferencesSave("PASS_WORD", encryptPwd)
        sharedPreferencesSave("USER_ID", USER_ID)
        sharedPreferencesSave("LOGIN_ISCHECKED", true)
        startActivity(Intent(this@LoginActivity, OcrMainActivity::class.java))
        finish()
    }

    private fun initViews() {
        binding.textView6.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://erp.tpxx.net/support")
            if (intent.resolveActivity(this.packageManager) != null) {
                val componentName = intent.resolveActivity(this.packageManager)
                this.startActivity(Intent.createChooser(intent, "请选择浏览器"))
            } else {
                "链接错误或无浏览器".showToast(this)
            }
        }
        binding.set.apply {
            setOnClickListener {
                startActivity(Intent(this@LoginActivity, SetDBActivity::class.java))
            }
            visibility = if (isDebug) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
        binding.progressBarLogin.visibility = View.INVISIBLE
        when (SharedPreferencesUtil.sharedPreferencesLoad("LOGIN_ISCHECKED", false)) {
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
                binding.progressBarLogin.visibility = View.INVISIBLE
                "用户名或密码不能为空,请重新输入".showToast(this)
                return@setOnClickListener
            }
            loginViewModel.userLogin(userName, pwd, this).observe(this, {
                binding.progressBarLogin.visibility = View.INVISIBLE
                "用户ID：$it".logi()
                when (it) {
                    -1 -> loginFailed()
                    else -> {
                        USER_ID = it
                        loginSuccess()
                    }
                }
            })
        }
    }

}