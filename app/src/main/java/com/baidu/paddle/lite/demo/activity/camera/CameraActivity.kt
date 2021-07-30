package com.baidu.paddle.lite.demo.activity.camera

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.paddle.lite.demo.activity.result.ResultActivity
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityCameraBinding
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    lateinit var pbRunModel: ProgressDialog
    companion object{
        val TAG = CameraActivity.javaClass.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val RESPONSE_RUN_MODEL_SUCCESSED = 2
        const val RESPONSE_RUN_MODEL_FAILED = 3
        const val REQUEST_RUN_MODEL = 1
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
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        // 获得有关可修改图像捕获用例的稳定参考
        var imageCapture = imageCapture ?: return
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

    private fun runModel() {
        pbRunModel = ProgressDialog.show(this, "", "running model...", false, false)
        if (onRunModel()) {
            if (pbRunModel != null && pbRunModel.isShowing) {
                pbRunModel.dismiss()
            }
            onRunModelSuccessed()
            Log.i(TAG, "runModel: true")
        }else{
            Log.i(TAG, "runModel: false")
        }
    }

    fun onRunModel(): Boolean {
        return predictor.isLoaded() && predictor.runModel()
    }

    fun onRunModelSuccessed() {
        Log.i(TAG, "onRunModelSuccessed: ${predictor.outputResult()}")
        startActivity(Intent(this,
            ResultActivity::class.java))
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
                .setTargetResolution(Size(1280,720))
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
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        if (predictor != null) {
            predictor.releaseModel()
        }
    }
}