package com.baidu.paddle.lite.demo.activity.receipts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.paddle.lite.demo.network.model.ReceiptsModel
import com.baidu.paddle.lite.demo.ocr.databinding.ActivityReceiptsBinding
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.google.gson.Gson

class ReceiptsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptsBinding
    private lateinit var receiptsModel: ReceiptsModel
    private lateinit var receiptsListAdapter: ReceiptsListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDatas()
        initViews()
    }

    private fun initDatas() {
        val bundle = intent.extras
        val data = bundle?.getString("RECEIPTS_INFO")
        receiptsModel = Gson().fromJson(data, ReceiptsModel::class.java)
    }


    private fun initViews() {
        receiptsListAdapter = ReceiptsListAdapter(receiptsModel)
        binding.recyclerViewReceipts.apply {
            layoutManager =
                LinearLayoutManager(this@ReceiptsActivity, LinearLayoutManager.VERTICAL, false)
            adapter = receiptsListAdapter
        }
    }
}