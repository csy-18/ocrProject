package net.ixzyj.activity.setting

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import net.ixzyj.ocr.R
import net.ixzyj.utils.MyApplication.Companion.logi
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