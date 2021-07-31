package com.baidu.paddle.lite.demo.activity.camera

import android.Manifest
import android.R
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.baidu.paddle.lite.demo.activity.result.ResultActivity
import com.baidu.paddle.lite.demo.activity.setting.SettingsActivity
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityCameraBinding
import com.baidu.paddle.lite.demo.ocr.demo.MainActivity
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sychen.basic.activity.ActivityCollector
import com.sychen.basic.activity.BaseActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity() {
    lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
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
        initViews()
    }

    private fun initViews() {
        if (allPermissionsGranted()) {
            startCamera()
            initHandler()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }
        binding.photoAlbum.setOnClickListener { getPhotoAlbum() }
        cameraExecutor = Executors.newSingleThreadExecutor()
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
    private fun takePhoto() {
        // 获得有关可修改图像捕获用例的稳定参考
        val imageCapture = imageCapture ?: return
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
        startActivity(
            Intent(
                this,
                ResultActivity::class.java
            )
        )
    }

    private fun onRunModelFailed() {
        if (pbRunModel.isShowing) {
            pbRunModel.dismiss()
        }
        AlertDialog.Builder(this)
            .setTitle("OCR模型出错")
            .setMessage("需要重启软件重新初始化OCR模型")
            .setNegativeButton("确定") { dialog, which ->
                ActivityCollector.finishAll()
            }
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
                .setTargetResolution(Size(400, 400))
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        if (predictor != null) {
            predictor.releaseModel()
        }
    }
}