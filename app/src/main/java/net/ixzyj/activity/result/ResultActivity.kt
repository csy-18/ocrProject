package net.ixzyj.activity.result

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.text.InputType
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.model.ResultModel
import net.ixzyj.ocr.databinding.ActivityResultBinding
import net.ixzyj.utils.CodeUtils.genElscodeCkCode
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.FileUtils
import net.ixzyj.utils.MyApplication.Companion.flagPage
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.MyApplication.Companion.predictor
import net.ixzyj.utils.MyApplication.Companion.showToast
import net.ixzyj.utils.SharedPreferencesUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sychen.basic.activity.BaseActivity
import java.util.*

class ResultActivity : BaseActivity() {
    lateinit var binding: ActivityResultBinding
    lateinit var recyclerViewAdapter: ResultListAdapter
    lateinit var work: Handler
    lateinit var netHandler: Handler
    lateinit var adapterHandler: Handler
    lateinit var dialog: Dialog

    val viewModel by lazy {
        ViewModelProvider(this).get(ResultViewModel::class.java)
    }

    companion object {
        var TAG = ResultActivity.javaClass.name
        var userId = -1
        const val FROM_REC = 1
        const val FROM_REC_SCENE = 2
        const val UPLOAD_SUCCESS_RESULT = 3
        const val UPLOAD_REC_SCENE_RESULT = 4
        const val RESULT_ADAPTER = 5
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
        netHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    UPLOAD_SUCCESS_RESULT -> {
                        val resultModel =
                            Gson().fromJson(msg.obj.toString(), ResultModel::class.java)
                        uploadResult(resultModel)
                    }
                }
            }
        }
        adapterHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    RESULT_ADAPTER -> {
                        val index = msg.obj
                        index.toString().logi()
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
                    dialog.dismiss()
                    "没有结果不能保存".showToast(this)
                }
                else -> {
                    Thread {
                        val stringBuffer = StringBuffer()
                        outResult.forEach {
                            stringBuffer.append(it).append("\n")
                        }
                        val uploadResult =
                            OdooUtils.uploadRecScene(orderId, stringBuffer.toString(), warehouseId)
                        val message = Message()
                        message.obj = uploadResult
                        message.what = UPLOAD_SUCCESS_RESULT
                        netHandler.sendMessage(message)
                    }.start()
                }
            }
        })
    }

    private fun fromRec() {
        val bundle = intent.extras
        val receiptsId = bundle?.getInt("RECEIPTS_ID")
        predictor.outputResult().observe(this, { outResult ->
            when (outResult.size) {
                0 -> {
                    dialog.dismiss()
                    "没有结果不能保存".showToast(this)
                }
                else -> {
                    Thread {
                        val stringBuffer = StringBuffer()
                        outResult.forEach {
                            stringBuffer.append(it).append("\n")
                        }
                        val uploadResult = OdooUtils.uploadRec(receiptsId!!, stringBuffer.toString())
                        val message = Message()
                        message.obj = uploadResult
                        message.what = UPLOAD_SUCCESS_RESULT
                        netHandler.sendMessage(message)
                    }.start()
                }
            }
        })
    }

    private fun initDatas() {
        userId = SharedPreferencesUtil.sharedPreferencesLoad("USER_ID", -1) as Int
    }

    private fun initViews() {
        "初始化视图".logi()
        dialog = DialogUtil.progressBarDialog(this)
        binding.imgResult.apply {
            Glide.with(this)
                .load(predictor.outputImage())
                .into(this)
            setOnClickListener {
                DialogUtil.photoDialog(this@ResultActivity, predictor.outputImage())
                    .show()
            }
        }
        binding.inputEditText.apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setRawInputType(InputType.TYPE_CLASS_NUMBER)
        }
        predictor.outputResult().observe(this, {
            recyclerViewAdapter = ResultListAdapter(it, adapterHandler)
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
            "上传按钮点击".logi()
            dialog.show()
            when (flagPage) {
                1 -> work.sendEmptyMessage(FROM_REC)
                2 -> work.sendEmptyMessage(FROM_REC_SCENE)
            }
        }
        binding.outResult.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("退出扫码")
                .setNegativeButton("取消") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("确认") { dialog, which ->
                    dialog.dismiss()
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
        val stringBuilder = StringBuilder()
        stringBuilder.append("运行时间：").append(inferenceTime).append("毫秒").append("\n")
        stringBuilder.append("以下数据不合格,请修改后上传：\n")
        var index = 1
        val resultList = predictor.outputResult().value
        resultList?.forEach {
            var verify = false
            if (it.length > 10) {
                val sequence = it.subSequence(0, 10).toString()
                val genElscodeCkCode = genElscodeCkCode(sequence)
                verify = genElscodeCkCode.equals(it)
            }
            if (it.length != 11 || !verify) {
                index.toString().logi()
                it.logi()
                stringBuilder.append("序号:").append("$index,").append("内容:").append(it).append("\n")
            }
            index++
        }
        binding.runModelStatus.text = stringBuilder
    }

    private fun uploadResult(resultModel: ResultModel) {
        when (resultModel.result) {
            "200" -> uploadSuccess()
            else -> uploadFailed(resultModel)
        }
    }

    private fun uploadFailed(resultModel: ResultModel) {
        dialog.dismiss()
        DialogUtil.alertDialog("上传失败\n详情：${resultModel.message}", this)
    }

    private fun uploadSuccess() {
        dialog.dismiss()
        "上传成功".showToast(this)
        predictor.outputResult().observe(this, {
            saveFiles(it, predictor.outputImage())
        })
        startActivity(Intent(this, CameraActivity::class.java))
        finish()
    }


    private fun saveFiles(resultDirList: List<String>, bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileUtils().createRootDir(resultDirList, bitmap, this)
        }
        FileUtils().createRootDirComm(resultDirList, bitmap)
    }


}