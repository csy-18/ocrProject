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
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.activity.setting.SettingsActivity
import org.apache.xmlrpc.XmlRpcException
import java.lang.Exception
import java.net.MalformedURLException

class SplashActivity : BaseActivity() {
    private lateinit var work: Handler

    companion object {
        private const val VERSION_SUCCESS = 0
        private const val VERSION_FAILED = 1
        private const val VERSION_NET_FAILED = 2
        private const val VERSION_SET_FAILED = 3
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
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                    VERSION_FAILED -> {
                        val alertDialog =
                            DialogUtil.alertDialog("获取版本失败\n请联系开发人员解决", this@SplashActivity)
                        alertDialog.setOnDismissListener {
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                    VERSION_SET_FAILED -> {
                        val alertDialog =
                            DialogUtil.alertDialog("获取版本失败\n请到设置页面重新设定服务器", this@SplashActivity)
                        alertDialog.setOnDismissListener {
                            startActivity(Intent(this@SplashActivity, SetDBActivity::class.java))
                            finish()
                        }
                    }
                    VERSION_NET_FAILED -> {
                        DialogUtil.alertDialog("连接服务器失败\n请检查手机网络\n或者联系开发人员解决", this@SplashActivity)
                    }
                }
            }
        }
    }

    private fun initViews() {
        window.navigationBarColor = 0x1AC0C0C0
        Handler(Looper.myLooper()!!).postDelayed({
            Thread {
                try {
                    version = OdooUtils.getVersion()
                } catch (e: MalformedURLException) { //服务器连接出错抛出异常
                    work.sendEmptyMessage(VERSION_SET_FAILED)
                } catch (e: XmlRpcException) { //手机没有网络情况抛出异常
                    work.sendEmptyMessage(VERSION_NET_FAILED)
                } catch (e: Exception) {
                    work.sendEmptyMessage(VERSION_FAILED)
                }
                if (version != "") {
                    work.sendEmptyMessage(VERSION_SUCCESS)
                }
            }.start()
        }, 1000)
    }
}