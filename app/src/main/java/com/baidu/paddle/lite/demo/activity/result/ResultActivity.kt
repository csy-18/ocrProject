package com.baidu.paddle.lite.demo.activity.result

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.activity.main.OcrMainActivity
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.CodeUtils.genElscodeCkCode
import com.baidu.paddle.lite.demo.utils.FileUtils
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.baidu.paddle.lite.demo.utils.SharedPreferencesUtil
import com.bumptech.glide.Glide
import com.sychen.basic.activity.BaseActivity
import java.util.*

class ResultActivity : BaseActivity() {
    lateinit var binding: ActivityResultBinding
    lateinit var recyclerViewAdapter: ResultListAdapter
    lateinit var work : Handler
    companion object {
        var TAG = ResultActivity.javaClass.name
        var userId = -1
        var flag = -1
        const val FROM_REC = 1
        const val FROM_REC_SCENE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDatas()
        initHandler()
        initViews()
    }

    private fun initHandler() {
        work = object : Handler(mainLooper){
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    FROM_REC->{
                        fromRec()
                    }
                    FROM_REC_SCENE->{
                        fromRecScene()
                    }
                }
            }
        }
    }

    private fun fromRecScene() {
        val bundle = intent.extras
        val orderId = bundle?.getInt("ORDER_ID")
        val warehouseId = bundle?.getInt("WAREHOUSE_ID")
        predictor.outputResult().observe(this, { outResult ->
            "$outResult.size".logi()
            when (outResult.size) {
                0 -> {
                    "没有结果不能保存".logi()
                    "没有结果不能保存".showToast(this)
                }
                1 -> {
                    outResult[0].logi()
                    Thread {
                        val uploadResult = OdooUtils.uploadRecScene(userId, orderId, outResult[0],warehouseId)
                        uploadResult.logi()
                    }.start()
                }
                else -> {
                    "外部文件是否可写入${FileUtils().isExternalStorageWritable()}".logi()
                    outResult.forEach {
                        Thread {
                            val uploadRecScene =
                                OdooUtils.uploadRecScene(userId, orderId, it, warehouseId)
                            uploadRecScene.logi()
                        }.start()
//                            FileUtils().getPublicAlbumStorageDir(it)
                    }
                }
            }
        })
    }

    private fun fromRec() {
        val bundle = intent.extras
        val receiptsId = bundle?.getInt("RECEIPTS_ID")
        predictor.outputResult().observe(this, { outResult ->
            "$outResult.size".logi()
            when (outResult.size) {
                0 -> {
                    "没有结果不能保存".logi()
                    "没有结果不能保存".showToast(this)
                }
                1 -> {
                    outResult[0].logi()
                    Thread {
                        val uploadResult = OdooUtils.uploadRec(userId, receiptsId, outResult[0])
                        uploadResult.logi()
                    }.start()
                }
                else -> {
//                    "外部文件是否可写入${FileUtils().isExternalStorageWritable()}".logi()
                    outResult.forEach {
                        Thread {
                            val genElscodeCkCode = genElscodeCkCode(it)
                            val uploadResult = OdooUtils.uploadRec(userId, receiptsId, genElscodeCkCode)
                            uploadResult.logi()
                        }.start()
//                            FileUtils().getPublicAlbumStorageDir(it)
                    }
                }
            }
        })
    }

    private fun initDatas() {
        userId = SharedPreferencesUtil.sharedPreferencesLoad<Int>("USER_ID") as Int
        val bundle = intent.extras
        flag = bundle?.getInt("FLAG")!!
    }

    private fun initViews() {
        Glide.with(this)
            .load(predictor.outputImage())
            .into(binding.imgResult)
        predictor.outputResult().observe(this, {
            recyclerViewAdapter = ResultListAdapter(it)
            recyclerViewAdapter.notifyDataSetChanged()
            binding.listResult.apply {
                layoutManager =
                    LinearLayoutManager(this@ResultActivity, LinearLayoutManager.VERTICAL, false)
                adapter = recyclerViewAdapter
                setItemTouch(this)
            }
        })
        binding.toolbarResult.apply {
            setNavigationOnClickListener {
                startActivity(Intent(this@ResultActivity, CameraActivity::class.java))
                finish()
            }
        }
        binding.saveInputBtn.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()
            if (text.equals("") || text == "") {
                "未输入数据！".showToast(this)
                return@setOnClickListener
            }
            predictor.outputResult.value?.add(text)
            recyclerViewAdapter.notifyDataSetChanged()
        }
        binding.saveUploadBtn.setOnClickListener {
            when(flag){
               1->work.sendEmptyMessage(FROM_REC)
               2->work.sendEmptyMessage(FROM_REC_SCENE)
            }
        }
        binding.outResult.setOnClickListener {
            startActivity(Intent(this, OcrMainActivity::class.java))
            finish()
        }
        setModelStatus()
    }

    private fun setItemTouch(recyclerView: RecyclerView) {
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                "序号${viewHolder.adapterPosition}".logi()
                predictor.outputResult.value?.removeAt(viewHolder.adapterPosition)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun setModelStatus() {
        val inferenceTime = predictor.inferenceTime()
        val stringBuffer = StringBuffer()
        stringBuffer.append("运行时间：").append(inferenceTime).append("毫秒").append("\n")
        binding.runModelStatus.text = stringBuffer
    }


}