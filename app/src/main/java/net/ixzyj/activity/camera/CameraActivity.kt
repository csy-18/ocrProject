package net.ixzyj.activity.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import net.ixzyj.activity.result.ResultActivity
import net.ixzyj.ocr.databinding.ActivityCameraBinding
import net.ixzyj.utils.MyApplication.Companion.flagPage
import net.ixzyj.utils.MyApplication.Companion.loadModelStatus
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.MyApplication.Companion.predictor
import net.ixzyj.utils.MyApplication.Companion.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sychen.basic.activity.ActivityCollector
import com.sychen.basic.activity.BaseActivity
import kotlinx.coroutines.launch
import net.ixzyj.activity.materiallist.MateriaListActivity
import net.ixzyj.activity.receiptsin.ReceiptsInActivity
import net.ixzyj.activity.receptioninscene.RecinSceneActivity
import net.ixzyj.ocr.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity() {
    lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val bundle by lazy {
        Bundle()
    }
    private val flashLightState by lazy {
        MutableLiveData(false)
    }
    lateinit var pbRunModel: ProgressDialog
    lateinit var work: Handler

    companion object {
        val TAG = CameraActivity.javaClass.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val RESPONSE_RUN_MODEL_SUCCESSED = 2
        const val RESPONSE_RUN_MODEL_FAILED = 3
        const val REQUEST_RUN_MODEL = 1
        const val OPEN_GALLERY_REQUEST_CODE = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHandler()
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.toolbar3.apply {
            setNavigationOnClickListener {
                when (flagPage) {
                    1 -> {
                        startActivity(Intent(this@CameraActivity, ReceiptsInActivity::class.java))
                        finish()
                    }
                    2 -> {
                        startActivity(Intent(this@CameraActivity, MateriaListActivity::class.java))
                        finish()
                    }
                }
            }
        }
        binding.cameraToolBar.text = intent.extras?.getString("TOOLBAR_TITLE")
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        binding.flashLight.setOnClickListener { changeFlashLightState() }
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }
        binding.photoAlbum.setOnClickListener { getPhotoAlbum() }
        loadModelStatus.observe(this, {
            when (it) {
                true -> binding.info.text = "模型初始化成功\n可以进行扫码操作"
                false -> binding.info.text = "模型初始化失败，请检查运行设置，或联系开发人员"
            }
        })

    }

    private fun changeFlashLightState() {
        flashLightState.value = flashLightState.value?.not()
        flashLightState.observe(this, {
            when (it) {
                true -> binding.flashLight.load(R.drawable.ic_baseline_flash_on_24) {}
                false -> binding.flashLight.load(R.drawable.ic_baseline_flash_off_24) {}
            }
        })
    }

    /**
     * 从相册获取照片
     */
    private fun getPhotoAlbum() {
        if (requestAllPermissions()) {
            openGallery()
        }
    }

    /**
     * 打开相册
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }

    /**
     * 请求拍照权限
     */
    private fun requestAllPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                0
            )
            return false
        }
        return true
    }

    private fun initHandler() {
        work = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    REQUEST_RUN_MODEL -> {
                        lifecycleScope.launch {
                            if (onRunModel()) {
                                if (pbRunModel.isShowing) {
                                    pbRunModel.dismiss()
                                }
                                onRunModelSuccessed()
                                "图片识别成功".logi()
                            } else {
                                onRunModelFailed()
                                "图片识别失败".logi()
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 拍照
     */
    @SuppressLint("RestrictedApi")
    private fun takePhoto() {
        // 获得有关可修改图像捕获用例的稳定参考
        val imageCapture = imageCapture ?: return
        // 闪光灯设置
        flashLightState.observe(this, {
            imageCapture.camera?.cameraControl?.enableTorch(it)
        })
        // 创建带时间戳的输出文件以保存图像
        val photoFile = File(
            this.cacheDir,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
        // 创建包含文件+元数据的输出选项对象
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // 设置图像捕捉监听器，该功能在拍照后触发
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                /**
                 * 图片保存成功
                 */
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Glide.with(this@CameraActivity)
                        .asBitmap()
                        .load(savedUri)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                //运行模型
                                predictor.setInputImage(resource)
                                runModel()
                            }
                        })
                    Log.i(TAG, "onImageSaved: $savedUri")
                }
            })
    }

    /**
     * 运行模型
     */
    private fun runModel() {
        pbRunModel = ProgressDialog.show(this, "", "正在识别中", false, false)
        work.sendEmptyMessage(REQUEST_RUN_MODEL)
    }

    fun onRunModel(): Boolean {
        return predictor.isLoaded() && predictor.runModel()
    }

    fun onRunModelSuccessed() {
        "onRunModelSuccessed: ${predictor.outputResult()}".logi()
        "flagPage$flagPage".logi()
        when (flagPage) {
            1 -> toResultActivityWithRec()
            2 -> toResultActivityWithRecScene()
        }
    }

    private fun toResultActivityWithRecScene() {
        bundle.apply {
            putInt("ORDER_ID", intent.extras?.getInt("ORDER_ID")!!)
            putInt("FLAG", intent.extras?.getInt("FLAG")!!)
            putInt("WAREHOUSE_ID", intent.extras?.getInt("WAREHOUSE_ID")!!)
            val intent = Intent(this@CameraActivity, ResultActivity::class.java)
            intent.putExtras(this)
            startActivity(intent)
        }
    }

    private fun toResultActivityWithRec() {
        bundle.apply {
            putInt("RECEIPTS_ID", intent.extras?.getInt("RECEIPTS_ID")!!)
            putInt("FLAG", intent.extras?.getInt("FLAG")!!)
            val intent = Intent(this@CameraActivity, ResultActivity::class.java)
            intent.putExtras(this)
            startActivity(intent)
        }
    }

    private fun onRunModelFailed() {
        if (pbRunModel.isShowing) {
            pbRunModel.dismiss()
        }
        AlertDialog.Builder(this)
            .setTitle("OCR模型出错")
            .setMessage("需要从内存清除，请从任务管理器关闭该软件并重新启动")
            .setNegativeButton("确定") { dialog, which ->
                ActivityCollector.finishAll()
            }
            .show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // 用于将摄像机的生命周期绑定到生命周期所有者
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1080, 1920))
                .build()

            // 默认选择前置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // 重新绑定之前取消绑定用例
                cameraProvider.unbindAll()
                // 将用例绑定到相机
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "用例绑定失败", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                "用户未授予的权限.".showToast(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                OPEN_GALLERY_REQUEST_CODE -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        data.data?.let {
                            val bitmap = getBitmapFromUri(uri = it)
                            //运行模型
                            predictor.setInputImage(bitmap)
                            runModel()
                        }
                    }

                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver
        .openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        imageCapture?.onDetached()
    }
}