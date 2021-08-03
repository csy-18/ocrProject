package com.baidu.paddle.lite.demo.activity.setting

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import java.io.File
import java.lang.Exception


class FileActivity : AppCompatActivity() {
    companion object {
        const val CREATE_FILE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        initViews()
    }

    private fun initViews() {
        findViewById<Button>(R.id.save_btn).apply {
            createFile("123")
        }
        findViewById<Button>(R.id.load_btn).apply {

        }
    }

    fun createFile(dirName: String) {
        try {
            val file = File(getExternalFilesDir(null) , dirName)
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: Exception) {
            e.message?.logi()
        }
    }

}