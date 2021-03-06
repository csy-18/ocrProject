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
                true -> binding.info.text = "?????????????????????\n????????????????????????"
                false -> binding.info.text = "?????????????????????????????????????????????????????????????????????"
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
     * ?????????????????????
     */
    private fun getPhotoAlbum() {
        if (requestAllPermissions()) {
            openGallery()
        }
    }

    /**
     * ????????????
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }

    /**
     * ??????????????????
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
                                "??????????????????".logi()
                            } else {
                                onRunModelFailed()
                                "??????????????????".logi()
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * ??????
     */
    @SuppressLint("RestrictedApi")
    private fun takePhoto() {
        // ??????????????????????????????????????????????????????
        val imageCapture = imageCapture ?: return
        // ???????????????
        flashLightState.observe(this, {
            imageCapture.camera?.cameraControl?.enableTorch(it)
        })
        // ????????????????????????????????????????????????
        val photoFile = File(
            this.cacheDir,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
        // ??????????????????+??????????????????????????????
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // ?????????????????????????????????????????????????????????
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                /**
                 * ??????????????????
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
                                //????????????
                                predictor.setInputImage(resource)
                                runModel()
                            }
                        })
                    Log.i(TAG, "onImageSaved: $savedUri")
                }
            })
    }

    /**
     * ????????????
     */
    private fun runModel() {
        pbRunModel = ProgressDialog.show(this, "", "???????????????", false, false)
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
            .setTitle("OCR????????????")
            .setMessage("???????????????????????????????????????????????????????????????????????????")
            .setNegativeButton("??????") { dialog, which ->
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
            // ???????????????????????????????????????????????????????????????
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

            // ???????????????????????????
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // ????????????????????????????????????
                cameraProvider.unbindAll()
                // ????????????????????????
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "??????????????????", exc)
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
                "????????????????????????.".showToast(this)
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
                            //????????????
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