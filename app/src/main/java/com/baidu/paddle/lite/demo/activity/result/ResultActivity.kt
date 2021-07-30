package com.baidu.paddle.lite.demo.activity.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityResultBinding
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.predictor
import com.bumptech.glide.Glide
import com.sychen.basic.activity.BaseActivity

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

    }


}