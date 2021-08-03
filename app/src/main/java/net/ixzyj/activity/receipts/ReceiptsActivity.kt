package net.ixzyj.activity.receipts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.network.model.ReceiptsModel
import net.ixzyj.ocr.databinding.ActivityReceiptsBinding
import com.google.gson.Gson
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.activity.main.OcrMainActivity

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
        binding.toolbar.apply {
            setNavigationOnClickListener {
                startActivity(Intent(this@ReceiptsActivity, OcrMainActivity::class.java))
                finish()
            }
        }
    }
}