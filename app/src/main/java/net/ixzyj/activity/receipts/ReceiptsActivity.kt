package net.ixzyj.activity.receipts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.network.model.ReceiptsModel
import net.ixzyj.ocr.databinding.ActivityReceiptsBinding
import com.google.gson.Gson
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.activity.login.LoginActivity
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication
import net.ixzyj.utils.MyApplication.Companion.DATA_ERROR

class ReceiptsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptsBinding
    private lateinit var receiptsModel: ReceiptsModel
    private lateinit var receiptsListAdapter: ReceiptsListAdapter
    private lateinit var errorHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHandler()
        initDatas()
        initViews()
    }

    private fun initHandler() {
        errorHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    DATA_ERROR->{
                        doDataErrorWork()
                    }
                }
            }
        }
    }

    private fun doDataErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("数据传输错误\n回退到主页重新进入", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            backHome()
        }
    }

    private fun initDatas() {
        val bundle = intent.extras
        val data = bundle?.getString("RECEIPTS_INFO")
        try {
            receiptsModel = Gson().fromJson(data, ReceiptsModel::class.java)
        } catch (e: Exception) {
            errorHandler.sendEmptyMessage(DATA_ERROR)
        }
    }


    private fun initViews() {
        receiptsListAdapter = ReceiptsListAdapter(receiptsModel)
        binding.recyclerViewReceipts.apply {
            layoutManager =
                LinearLayoutManager(this@ReceiptsActivity, LinearLayoutManager.VERTICAL, false)
            adapter = receiptsListAdapter
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                backHome()
            }
        }
    }

    private fun backHome(){
        startActivity(Intent(this@ReceiptsActivity, OcrMainActivity::class.java))
        finish()
    }
}