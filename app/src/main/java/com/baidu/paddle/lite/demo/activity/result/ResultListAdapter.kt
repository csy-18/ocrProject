package com.baidu.paddle.lite.demo.activity.result

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.utils.MyApplication
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.TAG
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ResultListAdapter(val resultList: List<String>) :
    RecyclerView.Adapter<ResultListAdapter.ResultListViewHolder>() {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListViewHolder {
        return ResultListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.result_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ResultListViewHolder, position: Int) {
        val result = resultList[position]
        val context = holder.itemView.context
        with(holder.itemView) {
            this.findViewById<TextView>(R.id.textView2).text = result
            this.findViewById<Button>(R.id.button6).setOnClickListener {
                saveFile(context,result)
            }
            this.findViewById<Button>(R.id.button5).setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.set_edit, null)
                val editText = view.findViewById<EditText>(R.id.update_edit)
                AlertDialog.Builder(context)
                    .setTitle("修改")
                    .setView(view)
                    .setPositiveButton("确认"){dialog,which->
                        saveFile(context,editText.text.toString())
                    }
                    .create().show()
            }
        }
    }

    fun saveFile(context: Context,content:String) {
        GlobalScope.launch {
            savePhoto(context)
        }
        val FILENAME = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".txt"

        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
            fos?.write(content.toByteArray())
            fos?.close()
            Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context,CameraActivity::class.java))
        } catch (e: FileNotFoundException) {
            Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private suspend fun savePhoto(context: Context) {
        withContext(Dispatchers.IO) {
            /**
             * API29之前可以使用
             */
            if ((MediaStore.Images.Media.insertImage(context.contentResolver,
                    predictor.outputImage() ,
                    "",
                    "") == null)
            ) {
                Toast.makeText(context, "存储失败", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "存储成功！", Toast.LENGTH_SHORT).show()
            }
            /**
             * API29之后
             */
            val insert =
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                ) ?: kotlin.run {
                    MainScope().launch {
                        Toast.makeText(context,
                            "存储失败",
                            Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
            context.contentResolver.openOutputStream(insert).use {
                if ((predictor.outputImage().compress(Bitmap.CompressFormat.JPEG, 90, it))) {
                    MainScope().launch {
                        Toast.makeText(context, "存储成功！", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(context,
                            "存储失败",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = resultList.size

    class ResultListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}