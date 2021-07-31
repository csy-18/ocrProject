package com.baidu.paddle.lite.demo.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.loge
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FileUtils {
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    /* 检查外部存储是否可用于读写 */
    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /* 检查外部存储是否至少可用于读取 */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    fun getPublicAlbumStorageDir(albumName: String?): File? {
        // 获取用户公共图片目录的目录。
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            ), albumName
        )
        if (!file.mkdirs()) {
            "Directory not created".loge()
        }
        return file
    }

    fun saveFile(context: Context, fileName: String) {
        val fos: FileOutputStream
        val 文件夹 = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()).toString()

        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(fileName.toByteArray())
            fos.close()
            "保存成功".showToast(context)
            context.startActivity(Intent(context, CameraActivity::class.java))
        } catch (e: FileNotFoundException) {
            "保存失败".showToast(context)
            e.printStackTrace()
        } catch (e: IOException) {
            "保存失败".showToast(context)
            e.printStackTrace()
        }
    }
}