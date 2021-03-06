package net.ixzyj.activity.result

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.InputType
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.activity.login.LoginActivity
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.network.OdooRepo
import net.ixzyj.network.model.ResultModel
import net.ixzyj.ocr.databinding.ActivityResultBinding
import net.ixzyj.utils.*
import net.ixzyj.utils.CodeUtils.genElscodeCkCode
import net.ixzyj.utils.MyApplication.Companion.flagPage
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.MyApplication.Companion.predictor
import net.ixzyj.utils.MyApplication.Companion.showToast
import org.apache.xmlrpc.XmlRpcException
import java.net.MalformedURLException

class ResultActivity : BaseActivity() {
    lateinit var binding: ActivityResultBinding
    lateinit var recyclerViewAdapter: ResultListAdapter
    lateinit var work: Handler
    lateinit var netHandler: Handler
    lateinit var adapterHandler: Handler
    lateinit var errorHandler: Handler
    lateinit var dialog: Dialog
    var resultModel = ResultModel("", "500")
    val errorResultString by lazy { MutableLiveData<StringBuffer>() }
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
        initHandler()
        initDatas()
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
                        try {
                            resultModel = Gson().fromJson(msg.obj.toString(), ResultModel::class.java)
                            uploadResult(resultModel)
                        } catch (e: Exception) {
                            val alertDialog =
                                DialogUtil.alertDialog("??????????????????\n????????????????????????????????????", this@ResultActivity)
                            alertDialog.create()
                            alertDialog.show()
                            alertDialog.setOnDismissListener {
                                startActivity(Intent(this@ResultActivity, OcrMainActivity::class.java))
                                finish()
                            }
                        }
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
        errorHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                dialog.dismiss()
                when (msg.what) {
                    MyApplication.ERROR -> {
                        doErrorWork()
                    }
                    MyApplication.NET_ERROR -> {
                        doNetError()
                    }
                    MyApplication.SETTING_ERROR -> {
                        doSettingError()
                    }
                }
            }
        }
    }

    private fun doSettingError() {
        val alertDialog =
            DialogUtil.alertDialog("????????????\n????????????:?????????????????????????????????\n????????????????????????????????????", this)
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, SetDBActivity::class.java))
            finish()
        }
    }

    private fun doNetError() {
        DialogUtil.alertDialog("????????????\n????????????:????????????????????????\n?????????????????????\n?????????????????????", this)
    }

    private fun doErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("????????????\n?????????????????????", this)
        alertDialog.setOnDismissListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fromRecScene() {
        val bundle = intent.extras
        val orderId = bundle?.getInt("ORDER_ID")
        val warehouseId = bundle?.getInt("WAREHOUSE_ID")
        "ResultActivity-orderId:$orderId".logi()
        "ResultActivity-warehouseId:$warehouseId".logi()
        predictor.outputResult().observe(this, { outResult ->
            when (outResult.size) {
                0 -> {
                    dialog.dismiss()
                    "????????????????????????".showToast(this)
                }
                else -> {
                    Thread {
                        val stringBuffer = StringBuffer()
                        outResult.forEach {
                            stringBuffer.append(it).append("\n")
                        }
                        try {
                            val uploadResult = OdooRepo.uploadRecScene(orderId, stringBuffer.toString(), warehouseId)
                            "uploadResult$uploadResult".logi()
                            val message = Message()
                            message.obj = uploadResult
                            message.what = UPLOAD_SUCCESS_RESULT
                            netHandler.sendMessage(message)
                        } catch (e: MalformedURLException) {
                            errorHandler.sendEmptyMessage(MyApplication.SETTING_ERROR)
                        } catch (e: XmlRpcException) {
                            errorHandler.sendEmptyMessage(MyApplication.NET_ERROR)
                        } catch (e: Exception) {
                            errorHandler.sendEmptyMessage(MyApplication.ERROR)
                        }
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
                    "????????????????????????".showToast(this)
                }
                else -> {
                    Thread {
                        val stringBuffer = StringBuffer()
                        outResult.forEach {
                            stringBuffer.append(it).append("\n")
                        }
                        try {
                            val uploadResult = OdooRepo.uploadRec(receiptsId!!, stringBuffer.toString())
                            "uploadResult$uploadResult".logi()
                            val message = Message()
                            message.obj = uploadResult
                            message.what = UPLOAD_SUCCESS_RESULT
                            netHandler.sendMessage(message)
                        } catch (e: MalformedURLException) {
                            errorHandler.sendEmptyMessage(MyApplication.SETTING_ERROR)
                        } catch (e: XmlRpcException) {
                            errorHandler.sendEmptyMessage(MyApplication.NET_ERROR)
                        } catch (e: Exception) {
                            errorHandler.sendEmptyMessage(MyApplication.ERROR)
                        }
                    }.start()
                }
            }
        })
    }

    private fun initDatas() {
        userId = SharedPreferencesUtil.sharedPreferencesLoad("USER_ID", -1) as Int
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViews() {
        dialog = DialogUtil.progressBarDialog(this)
        binding.imgResult.apply {
            Glide.with(this)
                .load(predictor.outputImage())
                .into(this)
            setOnClickListener {
                DialogUtil.photoDialog(this@ResultActivity, predictor.outputImage())
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
            if (text == "" || text.isEmpty()) {
                "????????????????????????".showToast(this)
                return@setOnClickListener
            }
            predictor.outputResult.value?.add(text)
            recyclerViewAdapter.notifyDataSetChanged()
        }
        errorResultString.observe(this, { errorResult ->
            binding.saveUploadBtn.setOnClickListener {
                "errorResult-saveUploadBtn.setOnClickListener:$errorResult".logi()
                dialog.show()
                if (errorResult.isNotEmpty()) {
                    dialog.dismiss()
                    DialogUtil.alertDialog("?????????????????????????????????\n????????????????????????", this)
                    return@setOnClickListener
                }
                when (flagPage) {
                    1 -> work.sendEmptyMessage(FROM_REC)
                    2 -> work.sendEmptyMessage(FROM_REC_SCENE)
                }
            }
        })
        binding.outResult.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("????????????")
                .setNegativeButton("??????") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("??????") { dialog, which ->
                    dialog.dismiss()
                    startActivity(Intent(this, OcrMainActivity::class.java))
                    finish()
                }
                .create().show()
        }
        setModelStatus()
        setErrorResult()
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
                "??????${viewHolder.adapterPosition}".logi()
                predictor.outputResult.value?.removeAt(viewHolder.adapterPosition)
                recyclerViewAdapter.notifyDataSetChanged()
                setErrorResult()
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun setModelStatus() {
        val inferenceTime = predictor.inferenceTime()
        val stringBuilder = StringBuilder()
        stringBuilder.append("???????????????").append(inferenceTime).append("??????")
        binding.runModelStatus.text = stringBuilder
    }

    private fun setErrorResult() {
        var index = 1
        predictor.outputResult().observe(this, { resultList ->
            val stringBuffer = StringBuffer()
            resultList?.forEach {
                var verify = false
                val sequence = CodeUtils.getSubstring(CodeUtils.CODE_REGEX_CC, it)
                if (!sequence.isEmpty()) {
                    val genElscodeCkCode = genElscodeCkCode(sequence.substring(0, sequence.length - 1))
                    verify = genElscodeCkCode.equals(it)
                }
                if (sequence.isEmpty() || it.length != sequence.length || !verify) {
                    index.toString().logi()
                    stringBuffer.append("$index").append(":").append(it).append("\n")
                }
                index++
            }
            errorResultString.postValue(stringBuffer)
            errorResultString.observe(this, {
                binding.errorResult.text = it
            })
        })
    }

    private fun uploadResult(resultModel: ResultModel) {
        dialog.dismiss()
        when (resultModel.result) {
            "200" -> uploadSuccess()
            else -> uploadFailed(resultModel)
        }
    }

    private fun uploadFailed(resultModel: ResultModel) {
        when (resultModel.result) {
            "500" -> {
                val alertDialog =
                    DialogUtil.alertDialog("????????????\n???????????????\n${resultModel.message}", this)
                alertDialog.create()
                alertDialog.show()
            }
            else -> {
                val alertDialog = DialogUtil.alertDialog("????????????\n??????????????????", this)
                alertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun uploadSuccess() {
        "????????????".showToast(this)
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