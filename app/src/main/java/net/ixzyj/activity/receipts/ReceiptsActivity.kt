package net.ixzyj.activity.receipts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.activity.result.ResultActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.model.ReceiptsModel
import net.ixzyj.ocr.databinding.ActivityReceiptsBinding
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication
import net.ixzyj.utils.MyApplication.Companion.DATA_ERROR
import net.ixzyj.utils.MyApplication.Companion.logi
import org.apache.xmlrpc.XmlRpcException
import java.net.MalformedURLException
// 物资入库
class ReceiptsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptsBinding
    private lateinit var receiptsModel: ReceiptsModel
    private lateinit var receiptsListAdapter: ReceiptsListAdapter
    private lateinit var errorHandler: Handler
    private lateinit var work: Handler
    companion object{
        const val GET_DATA_SUCCESS = 1
        const val GET_DATA_FAILED = 2
    }
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
                    DATA_ERROR -> {
                        doDataErrorWork()
                    }
                }
            }
        }
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    GET_DATA_SUCCESS->{
                        try {
                            receiptsModel = Gson().fromJson(msg.obj.toString(), ReceiptsModel::class.java)
                        } catch (e: Exception) {
                            errorHandler.sendEmptyMessage(DATA_ERROR)
                            e.message?.logi()
                        }
                    }
                    GET_DATA_FAILED->{}
                }
            }
        }
    }

    private fun doDataErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("数据传输错误\n返回主页面重新进入", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            backHome()
        }
    }

    private fun initDatas() {
        Thread {
            try {
                val receipts = OdooUtils.getReceipts()
                when (receipts.size) {
                    0 -> {
                        work.sendEmptyMessage(OcrMainActivity.LOAD_RECEIPTS_FAILED)
                    }
                    else -> {
                        val message = Message()
                        message.obj = Gson().toJson(receipts)
                        message.what = GET_DATA_SUCCESS
                        work.sendMessage(message)
                    }
                }
            } catch (e: MalformedURLException) {
                errorHandler.sendEmptyMessage(MyApplication.SETTING_ERROR)
            } catch (e: XmlRpcException) {
                errorHandler.sendEmptyMessage(MyApplication.NET_ERROR)
            } catch (e: Exception) {
                errorHandler.sendEmptyMessage(MyApplication.ERROR)
            }
        }.start()
    }


    private fun initViews() {
        if (::receiptsModel.isInitialized) {
            receiptsListAdapter = ReceiptsListAdapter(receiptsModel)
            binding.recyclerViewReceipts.apply {
                layoutManager =
                    LinearLayoutManager(this@ReceiptsActivity, LinearLayoutManager.VERTICAL, false)
                adapter = receiptsListAdapter
            }
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                backHome()
            }
        }
    }

    private fun backHome() {
        startActivity(Intent(this@ReceiptsActivity, OcrMainActivity::class.java))
        finish()
    }
}