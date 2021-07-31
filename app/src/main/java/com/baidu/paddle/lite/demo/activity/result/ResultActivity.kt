package com.baidu.paddle.lite.demo.activity.result

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.FileUtils
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.bumptech.glide.Glide
import com.sychen.basic.activity.BaseActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : BaseActivity() {
    lateinit var binding: ActivityResultBinding
    lateinit var recyclerViewAdapter: ResultListAdapter

    companion object {
        var TAG = ResultActivity.javaClass.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        Glide.with(this)
            .load(predictor.outputImage())
            .into(binding.imgResult)
        predictor.outputResult().observe(this, {
            recyclerViewAdapter = ResultListAdapter(it)
            recyclerViewAdapter.notifyDataSetChanged()
            binding.listResult.apply {
                layoutManager =
                    LinearLayoutManager(this@ResultActivity, LinearLayoutManager.VERTICAL, false)
                adapter = recyclerViewAdapter
            }
        })
        binding.saveInputBtn.setOnClickListener {
            val text = binding.inputEditText.text.toString()
            predictor.outputResult.value?.add(text)
            recyclerViewAdapter.notifyDataSetChanged()
        }
        binding.saveUploadBtn.setOnClickListener {
            predictor.outputResult().observe(this, { outResult ->
                when (outResult.size) {
                    0 -> {
                        "没有结果不能保存".logi()
                        "没有结果不能保存".showToast(this)
                    }
                    1 -> {
                        outResult[0].logi()
                    }
                    else -> {
                        "外部文件是否可写入${FileUtils().isExternalStorageWritable()}".logi()
                        outResult.forEach {
                            FileUtils().getPublicAlbumStorageDir(it)
                        }
                    }
                }
            })
        }
    }



}