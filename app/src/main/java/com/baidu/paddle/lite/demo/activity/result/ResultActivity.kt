package com.baidu.paddle.lite.demo.activity.result

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.FileUtils
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.showToast
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sychen.basic.activity.BaseActivity
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
                setItemTouch(this)
            }
        })
        binding.saveInputBtn.setOnClickListener {
            val text = binding.inputEditText.text.toString()
            predictor.outputResult.value?.add(text)
            recyclerViewAdapter.notifyDataSetChanged()
        }
        binding.saveUploadBtn.setOnClickListener {
            predictor.outputResult().observe(this, { outResult ->
                "$outResult.size".logi()
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
//                        outResult.forEach {
//                            FileUtils().getPublicAlbumStorageDir(it)
//                        }
                    }
                }
            })
        }
        setModelStatus()
    }

    private fun setItemTouch(recyclerView: RecyclerView) {
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                "序号${viewHolder.adapterPosition}".logi()
                predictor.outputResult.value?.removeAt(viewHolder.adapterPosition)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun setModelStatus() {
        val inferenceTime = predictor.inferenceTime()
        val stringBuffer = StringBuffer()
        stringBuffer.append("运行时间：").append(inferenceTime).append("毫秒").append("\n")
        binding.runModelStatus.text = stringBuffer
    }


}