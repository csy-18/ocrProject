package com.baidu.paddle.lite.demo.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.baidu.paddle.lite.demo.ocr.R

class MyApplication : Application() {


    companion object {
        const val TAG = "MyApplication"

        val loadModelStatus: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
        // 物体检测的模型设置
        var modelPath = ""
        var labelPath = ""
        var imagePath = ""
        var cpuThreadNum = 1
        var cpuPowerMode = ""
        var inputColorFormat = ""
        var inputShape = longArrayOf()
        var inputMean = floatArrayOf()
        var inputStd = floatArrayOf()
        var scoreThreshold = 0.1f
        var predictor = PredictorUtil()

        fun log(message: String) {
            Log.i(TAG, message)
        }

        fun String.showToast(context: Context) {
            Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
        }

        fun String.logi() {
            Log.i(TAG, this)
        }

        fun String.loge() {
            Log.e(TAG, this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initModel()
    }

    private fun initModel() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var settingsChanged = false
        val model_path = sharedPreferences.getString(
            getString(R.string.MODEL_PATH_KEY),
            getString(R.string.MODEL_PATH_DEFAULT)
        )

        val label_path = sharedPreferences.getString(
            getString(R.string.LABEL_PATH_KEY),
            getString(R.string.LABEL_PATH_DEFAULT)
        )

        val image_path = sharedPreferences.getString(
            getString(R.string.IMAGE_PATH_KEY),
            getString(R.string.IMAGE_PATH_DEFAULT)
        )
        settingsChanged = settingsChanged or !model_path.equals(modelPath, ignoreCase = true)
        settingsChanged = settingsChanged or !label_path.equals(labelPath, ignoreCase = true)
        settingsChanged = settingsChanged or !image_path.equals(imagePath, ignoreCase = true)
        val cpu_thread_num = sharedPreferences.getString(
            getString(R.string.CPU_THREAD_NUM_KEY),
            getString(R.string.CPU_THREAD_NUM_DEFAULT)
        )!!.toInt()
        settingsChanged = settingsChanged or (cpu_thread_num != cpuThreadNum)
        val cpu_power_mode = sharedPreferences.getString(
            getString(R.string.CPU_POWER_MODE_KEY),
            getString(R.string.CPU_POWER_MODE_DEFAULT)
        )
        settingsChanged = settingsChanged or !cpu_power_mode.equals(cpuPowerMode, ignoreCase = true)
        val input_color_format = sharedPreferences.getString(
            getString(R.string.INPUT_COLOR_FORMAT_KEY),
            getString(R.string.INPUT_COLOR_FORMAT_DEFAULT)
        )
        settingsChanged =
            settingsChanged or !input_color_format.equals(inputColorFormat, ignoreCase = true)
        val input_shape = Utils.parseLongsFromString(
            sharedPreferences.getString(
                getString(R.string.INPUT_SHAPE_KEY),
                getString(R.string.INPUT_SHAPE_DEFAULT)
            ), ","
        )
        val input_mean = Utils.parseFloatsFromString(
            sharedPreferences.getString(
                getString(R.string.INPUT_MEAN_KEY),
                getString(R.string.INPUT_MEAN_DEFAULT)
            ), ","
        )
        val input_std = Utils.parseFloatsFromString(
            sharedPreferences.getString(
                getString(R.string.INPUT_STD_KEY), getString(R.string.INPUT_STD_DEFAULT)
            ), ","
        )
        settingsChanged = settingsChanged or (input_shape.size != inputShape.size)
        settingsChanged = settingsChanged or (input_mean.size != inputMean.size)
        settingsChanged = settingsChanged or (input_std.size != inputStd.size)
        if (!settingsChanged) {
            for (i in input_shape.indices) {
                settingsChanged = settingsChanged or (input_shape[i] != inputShape[i])
            }
            for (i in input_mean.indices) {
                settingsChanged = settingsChanged or (input_mean[i] != inputMean[i])
            }
            for (i in input_std.indices) {
                settingsChanged = settingsChanged or (input_std[i] != inputStd[i])
            }
        }
        val score_threshold = sharedPreferences.getString(
            getString(R.string.SCORE_THRESHOLD_KEY),
            getString(R.string.SCORE_THRESHOLD_DEFAULT)
        )!!.toFloat()
        settingsChanged = settingsChanged or (scoreThreshold != score_threshold)
        if (settingsChanged) {
            modelPath = model_path!!
            labelPath = label_path!!
            imagePath = image_path!!
            cpuThreadNum = cpu_thread_num
            cpuPowerMode = cpu_power_mode!!
            inputColorFormat = input_color_format!!
            inputShape = input_shape
            inputMean = input_mean
            inputStd = input_std
            scoreThreshold = score_threshold
            // 加载模型
            val onLoadModel = onLoadModel()
            loadModelStatus.value = onLoadModel
        }
    }

    private fun onLoadModel(): Boolean {
        return predictor.init(
            this, modelPath, labelPath, cpuThreadNum,
            cpuPowerMode,
            inputColorFormat,
            inputShape, inputMean,
            inputStd, scoreThreshold
        )
    }
}