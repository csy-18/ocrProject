package com.baidu.paddle.lite.demo.activity.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.animation.AnimationUtils
import com.baidu.paddle.lite.demo.activity.receipts.ReceiptsActivity
import com.baidu.paddle.lite.demo.activity.receptioninscene.RecinSceneActivity
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityOcrMainBinding
import com.baidu.paddle.lite.demo.utils.DialogUtil
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.SharedPreferencesUtil
import com.google.gson.Gson

class OcrMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityOcrMainBinding
    private val bundle by lazy {
        Bundle()
    }
    private lateinit var work: Handler

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
        UID = SharedPreferencesUtil.sharedPreferencesLoad<Int>("USER_ID") as Int
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
    }

    private fun loadRecSceneFailed() {
    }

    private fun loadRecSceneSuccess() {
        val intent = Intent(this, RecinSceneActivity::class.java)
        bundle.getString("REC_SCENE_INFO")
        intent.putExtras(bundle)
        startActivity(intent)
    }


    private fun loadReceiptsFailed() {
        Thread {
            Looper.prepare()
            val alertDialog = DialogUtil.alertDialog( "用户名信息错误",this)
            alertDialog.create()
            alertDialog.show()
            Looper.loop()
        }.start()
    }

    private fun loadReceiptsSuccess() {
        val intent = Intent(this, ReceiptsActivity::class.java)
        bundle.getString("RECEIPTS_INFO")
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun initViews() {
        binding.inStock.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_low)
            it.startAnimation(animation)
            it.postDelayed({
                "用户id-$UID".logi()
                Thread {
                    val receipts = OdooUtils.getReceipts(UID)
                    when (receipts.size) {
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
        binding.imageView4.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_low)
            it.startAnimation(animation)
            it.postDelayed({
                "用户id-$UID".logi()
                Thread {
                    val order = OdooUtils.getSendOrders(UID)
                    when (order.size) {
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