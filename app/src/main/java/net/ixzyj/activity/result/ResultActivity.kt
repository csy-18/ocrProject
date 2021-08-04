package net.ixzyj.activity.result

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.text.InputType
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
    lateinit var viewHandler: Handler
    lateinit var dialog: Dialog

    companion object {
        var TAG = ResultActivity.javaClass.name
        var userId = -1
        const val FROM_REC = 1
        const val FROM_REC_SCENE = 2
        const val RECYCLER_VIEW_CHANGED = 3
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
        viewHandler = object : Handler(mainLooper){
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    RECYCLER_VIEW_CHANGED->recyclerViewAdapter.notifyDataSetChanged()
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
                        uploadRecScene(outResult, orderId!!, warehouseId!!)
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
        dialog = DialogUtil.progressBarDialog(this)
        binding.imgResult.apply {
            setOnClickListener {
                    DialogUtil.photoDialog(this@ResultActivity, predictor.outputImage())
                        .show()
            }
        }
        binding.inputEditText.apply {
            val digits = "0123456789-"
            inputType = InputType.TYPE_CLASS_TEXT
            setRawInputType(InputType.TYPE_CLASS_NUMBER)
        }
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
        val stringBuffer = StringBuffer()
        stringBuffer.append("运行时间：").append(inferenceTime).append("毫秒").append("\n")
        binding.runModelStatus.text = stringBuffer
    }

    private fun uploadRecScene(resultList: List<String>, orderId: Int, warehousedId: Int) {
        val errorList = arrayListOf<String>()
        val successList = arrayListOf<String>()
        resultList.forEach { result ->
            if (result.length!=11){
                Looper.prepare()
                dialog.dismiss()
                DialogUtil.alertDialog("存在数据不符合要求,请检查后上传",this)
                Looper.loop()
            }
            val sequence = result.subSequence(0,10).toString()
            val genElscodeCkCode = genElscodeCkCode(sequence)
            if (genElscodeCkCode(sequence)!=result){
                Looper.prepare()
                dialog.dismiss()
                DialogUtil.alertDialog("校验结果与铭牌不符\n请移除该铭牌:$result",this)
                Looper.loop()
            }
            val uploadResult = OdooUtils.uploadRecScene(orderId, genElscodeCkCode, warehousedId)
            if (uploadResult == null) {
                errorList.add("上传失败")
            }
            val resultModel = Gson().fromJson(uploadResult, ResultModel::class.java)
            when (resultModel.result) {
                "200" -> {
                    successList.add(result)
                }
                "500" -> errorList.add(result)
                else -> errorList.add(result)
            }
        }
        if (successList.size == resultList.size) {
            Looper.prepare()
            dialog.dismiss()
            "上传成功".showToast(this)
            saveFiles(resultList, predictor.outputImage())
            startActivity(Intent(this@ResultActivity, CameraActivity::class.java))
            Looper.loop()
        } else {
            val message = errorList.toString() + " 上传失败\n" + "上传失败总数:" + errorList.size
            Looper.prepare()
            dialog.dismiss()
            DialogUtil.alertDialog(message, this)
            Looper.loop()
        }
    }

    private fun uploadRec(resultList: List<String>, receiptsId: Int) {
        val errorList = arrayListOf<String>()
        val successList = arrayListOf<String>()
        resultList.forEach { result ->
            if (result.length!=11){
                Looper.prepare()
                dialog.dismiss()
                DialogUtil.alertDialog("存在数据不符合要求,请检查后上传",this)
                Looper.loop()
            }
            val sequence = result.subSequence(0,10).toString()
            val genElscodeCkCode = genElscodeCkCode(sequence)
            if (genElscodeCkCode(sequence)!=result){
                Looper.prepare()
                dialog.dismiss()
                DialogUtil.alertDialog("校验结果与铭牌不符\n请移除该铭牌:$result",this)
                Looper.loop()
            }
            val uploadResult = OdooUtils.uploadRec(receiptsId, genElscodeCkCode)
            if (uploadResult == null) {
                errorList.add("上传失败")
            }
            val resultModel = Gson().fromJson(uploadResult, ResultModel::class.java)
            "请求码:${resultModel.result}".logi()
            when (resultModel.result) {
                "200" -> {
                    successList.add(result)
                }
                "500" -> errorList.add(result)
                else -> errorList.add(result)
            }
        }
        if (successList.size == resultList.size) {
            Looper.prepare()
            dialog.dismiss()
            "上传成功".showToast(this)
            saveFiles(resultList, predictor.outputImage())
            startActivity(Intent(this@ResultActivity, CameraActivity::class.java))
            Looper.loop()
        } else {
            val message = errorList.toString() + " 上传失败\n" + "上传失败总数:" + errorList.size
            Looper.prepare()
            dialog.dismiss()
            DialogUtil.alertDialog(message, this)
            Looper.loop()
        }
    }

    private fun saveFiles(resultDirList: List<String>, bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            FileUtils().createRootDir(resultDirList, bitmap,this)
        }
        FileUtils().createRootDirComm(resultDirList, bitmap)
    }


}