package com.baidu.paddle.lite.demo.activity.result

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.utils.MyApplication
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.TAG
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
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
            this.findViewById<TextView>(R.id.index_reslt_list).text = (position+1).toString()
//            this.findViewById<Button>(R.id.button6).setOnClickListener {
//                saveFile(context,result)
//            }
            this.findViewById<Button>(R.id.button5).setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.set_edit, null)
                val editText = view.findViewById<EditText>(R.id.update_edit)
                editText.setText(result)
                AlertDialog.Builder(context)
                    .setTitle("修改")
                    .setView(view)
                    .setPositiveButton("确认") { dialog, which ->
                        val text = editText.text.toString()
                        text.logi()
                        predictor.outputResult.value?.remove(result)
                        predictor.outputResult.value?.add(text)
                        predictor.outputResult.value?.get(position)?.logi()
                    }
                    .create().show()
            }
        }
    }


    private suspend fun savePhoto(context: Context) {
        withContext(Dispatchers.IO) {
            /**
             * API29之前可以使用
             */
            if ((MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    predictor.outputImage(),
                    "",
                    ""
                ) == null)
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
                        Toast.makeText(
                            context,
                            "存储失败",
                            Toast.LENGTH_SHORT
                        ).show()
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
                        Toast.makeText(
                            context,
                            "存储失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = resultList.size

    class ResultListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}