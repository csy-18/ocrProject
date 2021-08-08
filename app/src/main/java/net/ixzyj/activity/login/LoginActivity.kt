package net.ixzyj.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.OdooUtils.userLogin
import net.ixzyj.ocr.databinding.ActivityLoginBinding
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.isDebug
import net.ixzyj.utils.MyApplication.Companion.showToast
import net.ixzyj.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.utils.AESUtil
import net.ixzyj.utils.MyApplication
import net.ixzyj.utils.MyApplication.Companion.ERROR
import net.ixzyj.utils.MyApplication.Companion.NET_ERROR
import net.ixzyj.utils.MyApplication.Companion.SETTING_ERROR
import net.ixzyj.utils.MyApplication.Companion.logi
import org.apache.xmlrpc.XmlRpcException
import java.lang.Exception
import java.net.MalformedURLException

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var work: Handler
    private lateinit var errorHandler: Handler

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
        errorHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ERROR -> {
                        doErrorWork()
                    }
                    NET_ERROR -> {
                        doNetError()
                    }
                    SETTING_ERROR -> {
                        doSettingError()
                    }
                }
            }
        }
    }

    private fun doSettingError() {
        val alertDialog = DialogUtil.alertDialog("数据库和服务器查询不到该用户\n请到进入重新设置服务器和数据库", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, SetDBActivity::class.java))
            finish()
        }
    }

    private fun doNetError() {
        val alertDialog = DialogUtil.alertDialog("连接服务器失败\n请检查手机网络", this)
        alertDialog.create()
        alertDialog.show()
    }

    private fun doErrorWork() {
        val alertDialog = DialogUtil.alertDialog("登录失败\n请联系开发人员", this)
        alertDialog.create()
        alertDialog.show()
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
        startActivity(Intent(this@LoginActivity, OcrMainActivity::class.java))
        finish()
    }

    private fun loginFailed() {
        binding.progressBarLogin.visibility = View.INVISIBLE
        val alertDialog = DialogUtil.alertDialog("用户名或密码错误", this)
        alertDialog.create()
        alertDialog.show()
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
                binding.progressBarLogin.visibility = View.INVISIBLE
                "用户名或密码未输入,请输入后重试".showToast(this)
                return@setOnClickListener
            }
            Thread {
                try {
                    USER_ID = userLogin(userName, pwd)
                } catch (e: MalformedURLException) {
                    errorHandler.sendEmptyMessage(SETTING_ERROR)
                } catch (e: XmlRpcException) {
                    errorHandler.sendEmptyMessage(NET_ERROR)
                } catch (e:ClassCastException){
                    //密码错误
                }catch (e:Exception){
                    errorHandler.sendEmptyMessage(ERROR)
                }
                "用户ID$USER_ID".logi()
                when (USER_ID) {
                    -1 -> work.sendEmptyMessage(USER_LOGIN_FAILED)
                    else -> work.sendEmptyMessage(USER_LOGIN_SUCCESS)
                }
            }.start()
        }
    }

}