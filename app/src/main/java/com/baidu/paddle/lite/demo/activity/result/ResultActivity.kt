package com.baidu.paddle.lite.demo.activity.result

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.activity.main.OcrMainActivity
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.network.model.ResultModel
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.CodeUtils.genElscodeCkCode
import com.baidu.paddle.lite.demo.utils.DialogUtil
import com.baidu.paddle.lite.demo.utils.FileUtils
import com.baidu.paddle.lite.demo.utils.MyApplication
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.flagPage
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.baidu.paddle.lite.demo.utils.SharedPreferencesUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sychen.basic.activity.BaseActivity
import java.util.*

class ResultActivity : BaseActivity() {
    lateinit var binding: ActivityResultBinding
    lateinit var recyclerViewAdapter: ResultListAdapter
    lateinit var work: Handler

    companion object {
        var TAG = ResultActivity.javaClass.name
        var userId = -1
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
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    FROM_REC -> {
                        fromRec()
                    }
                    FROM_REC_SCENE -> {
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
            when (outResult.size) {
                0 -> {
                    "没有结果不能保存".showToast(this)
                }
                else -> {
                    Thread{
                        uploadRecScene(outResult,orderId!!,warehouseId!!)
                    }.start()
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
                    "没有结果不能保存".showToast(this)
                }
                else -> {
                    Thread {
                        uploadRec(outResult, receiptsId!!)
                    }.start()
                }
            }
        })
    }

    private fun initDatas() {
        userId = SharedPreferencesUtil.sharedPreferencesLoad("USER_ID", -1) as Int
    }

    private fun initViews() {
        binding.imgResult.isFocusable = true
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
            when (flagPage) {
                1 -> work.sendEmptyMessage(FROM_REC)
                2 -> work.sendEmptyMessage(FROM_REC_SCENE)
            }
        }
        binding.outResult.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("退出扫码")
                .setNegativeButton("取消"){dialog, which ->

                }
                .setPositiveButton("确认") { dialog, which ->
                    startActivity(Intent(this, OcrMainActivity::class.java))
                    finish()
                }
                .create().show()
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

    private fun uploadRecScene(resultList: List<String>,orderId:Int,warehousedId:Int) {
        val errorList = arrayListOf<String>()
        val successList = arrayListOf<String>()
        resultList.forEach { result->
            val genElscodeCkCode = genElscodeCkCode(result)
            val uploadResult = OdooUtils.uploadRecScene(orderId, genElscodeCkCode, warehousedId)
            if (uploadResult==null){
                errorList.add("上传失败")
            }
            val resultModel = Gson().fromJson(uploadResult, ResultModel::class.java)
            when (resultModel.result) {
                "200" -> {
                    successList.add(result)
//                    predictor.outputResult.value?.remove(result)
                }
                "500" -> errorList.add(result)
                else -> errorList.add(result)
            }
        }
        if (successList.size == resultList.size) {
            Looper.prepare()
            "上传成功".showToast(this)
            startActivity(Intent(this@ResultActivity, CameraActivity::class.java))
            Looper.loop()
        } else {
            val message = errorList.toString() + " 上传失败\n" + "上传失败总数:" + errorList.size
            Looper.prepare()
            DialogUtil.alertDialog(message, this)
            Looper.loop()
        }
    }

    private fun uploadRec(resultList: List<String>, receiptsId: Int) {
        val errorList = arrayListOf<String>()
        val successList = arrayListOf<String>()
        resultList.forEach { result ->
            val genElscodeCkCode = genElscodeCkCode(result)
            val uploadResult = OdooUtils.uploadRec(receiptsId, genElscodeCkCode)
            if (uploadResult==null){
                errorList.add("上传失败")
            }
            val resultModel = Gson().fromJson(uploadResult, ResultModel::class.java)
            when (resultModel.result) {
                "200" -> {
                    successList.add(result)
//                    predictor.outputResult.value?.remove(result)
                }
                "500" -> errorList.add(result)
                else -> errorList.add(result)
            }
        }
        if (successList.size == resultList.size) {
            Looper.prepare()
            "上传成功".showToast(this)
            startActivity(Intent(this@ResultActivity, CameraActivity::class.java))
            Looper.loop()
        } else {
            val message = errorList.toString() + " 上传失败\n" + "上传失败总数:" + errorList.size
            Looper.prepare()
            DialogUtil.alertDialog(message, this)
            Looper.loop()
        }
    }


}