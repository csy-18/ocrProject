package net.ixzyj.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import net.ixzyj.utils.MyApplication.Companion.logi
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FileUtils {
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd"
        private const val FILENAME_FORMAT_PHOTO = "yyyy-MM-dd-HH-mm-ss-SSS"
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

    fun createRootDirComm(resultDirList: List<String>, bitmap: Bitmap) {
        if (isExternalStorageWritable()) {
            val root =
                File(Environment.getExternalStorageDirectory(), File.separator + "ixzyj_net")
            val dateDir = SimpleDateFormat(
                FILENAME_FORMAT, Locale.CHINESE
            ).format(System.currentTimeMillis())
            try {
                if (!root.exists()) {
                    root.mkdirs()
                }

                val datedir = File(root.absolutePath, dateDir)
                if (!datedir.exists()) {
                    datedir.mkdirs()
                }
                resultDirList.forEach { resultDir ->
                    val photoFos = FileOutputStream(
                        File(
                            datedir.absolutePath,
                            File.separator + resultDir + ".jpg"
                        )
                    ).use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it).run {
                            it.flush()
                            it.close()
                        }
                    }
                    val resultdir = File(datedir.absolutePath, File.separator + resultDir + ".txt")
                    FileOutputStream(resultdir)
                }
            } catch (e: FileNotFoundException) {
                "不能访问".logi()
            } catch (e: IOException) {
                e.message?.logi()
            }
        }
    }
    fun createRootDir(resultDirList: List<String>, bitmap: Bitmap,context: Context) {
        if (isExternalStorageWritable()) {
            val root =
                File(context.getExternalFilesDir(null), File.separator + "ixzyj_net")
            val dateDir = SimpleDateFormat(
                FILENAME_FORMAT, Locale.CHINESE
            ).format(System.currentTimeMillis())
            try {
                if (!root.exists()) {
                    root.mkdirs()
                }

                val datedir = File(root.absolutePath, dateDir)
                if (!datedir.exists()) {
                    datedir.mkdirs()
                }
                resultDirList.forEach { resultDir ->
                    val photoFos = FileOutputStream(
                        File(
                            datedir.absolutePath,
                            File.separator + resultDir + ".jpg"
                        )
                    ).use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it).run {
                            it.flush()
                            it.close()
                        }
                    }
                    val resultdir = File(datedir.absolutePath, File.separator + resultDir + ".txt")
                    FileOutputStream(resultdir)
                }
            } catch (e: FileNotFoundException) {
                "不能访问".logi()
            } catch (e: IOException) {
                e.message?.logi()
            }
        }
    }
}