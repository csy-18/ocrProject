package net.ixzyj.activity.main

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.animation.AnimationUtils
import com.google.gson.Gson
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.activity.login.LoginActivity
import net.ixzyj.activity.receipts.ReceiptsActivity
import net.ixzyj.activity.receptioninscene.RecinSceneActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.ActivityOcrMainBinding
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication.Companion.ERROR
import net.ixzyj.utils.MyApplication.Companion.NET_ERROR
import net.ixzyj.utils.MyApplication.Companion.SETTING_ERROR
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.SharedPreferencesUtil
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
                dialog.dismiss()
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
                dialog.dismiss()
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
            DialogUtil.alertDialog("获取列表失败\n请重新设定服务器和数据库", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, SetDBActivity::class.java))
            finish()
        }
    }

    private fun doNetError() {
        val alertDialog = DialogUtil.alertDialog("网络异常\n请检查手机网络", this)
        alertDialog.create()
        alertDialog.show()
    }

    private fun doErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("获取列表异常，请重新登录", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadRecSceneFailed() {
        val alertDialog = DialogUtil.alertDialog("该用户没有项目入库单", this)
        alertDialog.create()
        alertDialog.show()
    }

    private fun loadRecSceneSuccess() {
        val intent = Intent(this, RecinSceneActivity::class.java)
        bundle.getString("REC_SCENE_INFO")
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun loadReceiptsFailed() {
        DialogUtil.alertDialog("该用户没有物资入库单", this)
    }

    private fun loadReceiptsSuccess() {
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
                    try {
                        val receipts = OdooUtils.getReceipts()
                        "receipts:$receipts".logi()
                        when (receipts.size) {
                            0 -> {
                                work.sendEmptyMessage(LOAD_RECEIPTS_FAILED)
                            }
                            else -> {
                                bundle.putString("RECEIPTS_INFO", Gson().toJson(receipts))
                                work.sendEmptyMessage(LOAD_RECEIPTS_SUCCESS)
                            }
                        }
                    } catch (e: MalformedURLException) {
                        errorHandler.sendEmptyMessage(SETTING_ERROR)
                    } catch (e: XmlRpcException) {
                        errorHandler.sendEmptyMessage(NET_ERROR)
                    } catch (e: Exception) {
                        errorHandler.sendEmptyMessage(ERROR)
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
                    try {
                        val order = OdooUtils.getSendOrders()
                        "order$order".logi()
                        when (order.size) {
                            0 -> {
                                work.sendEmptyMessage(LOAD_REC_SCENE_FAILED)
                            }
                            else -> {
                                bundle.putString("REC_SCENE_INFO", Gson().toJson(order))
                                work.sendEmptyMessage(LOAD_REC_SCENE_SUCCESS)
                            }
                        }
                    } catch (e: MalformedURLException) {
                        errorHandler.sendEmptyMessage(SETTING_ERROR)
                    } catch (e: XmlRpcException) {
                        errorHandler.sendEmptyMessage(NET_ERROR)
                    } catch (e: Exception) {
                        errorHandler.sendEmptyMessage(ERROR)
                    }
                }.start()
            }, 500)
        }
    }

}