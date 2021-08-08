package net.ixzyj.activity.main

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.animation.AnimationUtils
import net.ixzyj.activity.receipts.ReceiptsActivity
import net.ixzyj.activity.receptioninscene.RecinSceneActivity
import net.ixzyj.activity.setting.SettingsActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.ActivityOcrMainBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import com.google.gson.Gson
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.activity.login.LoginActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.utils.*
import net.ixzyj.utils.MyApplication.Companion.ERROR
import net.ixzyj.utils.MyApplication.Companion.NET_ERROR
import net.ixzyj.utils.MyApplication.Companion.SETTING_ERROR
import org.apache.xmlrpc.XmlRpcException
import java.net.MalformedURLException

class OcrMainActivity : BaseActivity() {

    lateinit var binding: ActivityOcrMainBinding

    private val bundle by lazy {
        Bundle()
    }
    private lateinit var work: Handler
    private lateinit var errorHandler: Handler
    lateinit var dialog: Dialog

    companion object {
        var UID = -1
        const val LOAD_RECEIPTS_SUCCESS = 0
        const val LOAD_RECEIPTS_FAILED = 1
        const val LOAD_REC_SCENE_SUCCESS = 2
        const val LOAD_REC_SCENE_FAILED = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UID = SharedPreferencesUtil.sharedPreferencesLoad("USER_ID", -1) as Int
        initHandler()
        initViews()
    }

    private fun initHandler() {
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    LOAD_RECEIPTS_SUCCESS -> {
                        loadReceiptsSuccess()
                    }
                    LOAD_RECEIPTS_FAILED -> {
                        loadReceiptsFailed()
                    }
                    LOAD_REC_SCENE_SUCCESS -> {
                        loadRecSceneSuccess()
                    }
                    LOAD_REC_SCENE_FAILED -> {
                        loadRecSceneFailed()
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
        val alertDialog =
            DialogUtil.alertDialog("获取列表失败\n请到设置页面重新设定服务器和数据库", this)
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, SetDBActivity::class.java))
            finish()
        }
    }

    private fun doNetError() {
        DialogUtil.alertDialog("连接服务器失败\n请检查手机网络\n或者联系开发人员解决", this)
    }

    private fun doErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("获取列表异常，请重新登录重试", this)
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadRecSceneFailed() {
        dialog.dismiss()
        DialogUtil.alertDialog("该用户不存在现场入库清单", this)
    }

    private fun loadRecSceneSuccess() {
        dialog.dismiss()
        val intent = Intent(this, RecinSceneActivity::class.java)
        bundle.getString("REC_SCENE_INFO")
        intent.putExtras(bundle)
        startActivity(intent)
    }


    private fun loadReceiptsFailed() {
        dialog.dismiss()
        DialogUtil.alertDialog("该用户不存在入库清单", this)
    }

    private fun loadReceiptsSuccess() {
        dialog.dismiss()
        val intent = Intent(this, ReceiptsActivity::class.java)
        bundle.getString("RECEIPTS_INFO")
        intent.putExtras(bundle)
        startActivity(intent)

    }

    private fun initViews() {
        dialog = DialogUtil.progressBarDialog(this)
        binding.recImg.setOnClickListener {
            dialog.show()
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_low)
            it.startAnimation(animation)
            it.postDelayed({
                Thread {
                    val receipts = try {
                        OdooUtils.getReceipts()
                    } catch (e: MalformedURLException) {
                        errorHandler.sendEmptyMessage(SETTING_ERROR)
                    } catch (e: XmlRpcException) {
                        errorHandler.sendEmptyMessage(NET_ERROR)
                    } catch (e: Exception) {
                        errorHandler.sendEmptyMessage(ERROR)
                    }
                    "receipts:$receipts".logi()
                    when (receipts) {
                        0 -> {
                            work.sendEmptyMessage(LOAD_RECEIPTS_FAILED)
                        }
                        else -> {
                            bundle.putString("RECEIPTS_INFO", Gson().toJson(receipts))
                            work.sendEmptyMessage(LOAD_RECEIPTS_SUCCESS)
                        }
                    }
                }.start()
            }, 500)

        }
        binding.recInSceneImg.setOnClickListener {
            dialog.show()
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_low)
            it.startAnimation(animation)
            it.postDelayed({
                Thread {
                    val order = try {
                        OdooUtils.getSendOrders()
                    } catch (e: MalformedURLException) {
                        errorHandler.sendEmptyMessage(SETTING_ERROR)
                    } catch (e: XmlRpcException) {
                        errorHandler.sendEmptyMessage(NET_ERROR)
                    } catch (e: Exception) {
                        errorHandler.sendEmptyMessage(ERROR)
                    }
                    "order$order".logi()
                    when (order) {
                        0 -> {
                            work.sendEmptyMessage(LOAD_REC_SCENE_FAILED)
                        }
                        else -> {
                            bundle.putString("REC_SCENE_INFO", Gson().toJson(order))
                            work.sendEmptyMessage(LOAD_REC_SCENE_SUCCESS)
                        }
                    }
                }.start()
            }, 500)
        }
    }

}