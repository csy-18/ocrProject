package net.ixzyj.activity.receiptsin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.network.OdooRepo
import net.ixzyj.network.model.ReceiptsModel
import net.ixzyj.ocr.databinding.ActivityReceiptsBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import com.google.gson.reflect.TypeToken
import com.google.gson.GsonBuilder
import net.ixzyj.network.model.ReceiptsModelItem


// 物资入库
class ReceiptsInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptsBinding
    private lateinit var receiptsModel: ReceiptsModel
    private lateinit var receiptsInListAdapter: ReceiptsInListAdapter

    private val viewModel by lazy {
        ViewModelProvider(this).get(ReceiptsInViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDatas()
    }


    private fun initDatas() {
        viewModel.getReceiptsInList().observe(this, {
            receiptsModel =
                Gson().fromJson(it, ReceiptsModel::class.java)
            initViews()
        })
    }


    private fun initViews() {
        if (::receiptsModel.isInitialized) {
            receiptsInListAdapter = ReceiptsInListAdapter(receiptsModel)
            binding.recyclerViewReceipts.apply {
                layoutManager =
                    LinearLayoutManager(
                        this@ReceiptsInActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                adapter = receiptsInListAdapter
            }
        }
        binding.toolbar.apply {
            setNavigationOnClickListener {
                backHome()
            }
        }
    }

    private fun backHome() {
        startActivity(Intent(this@ReceiptsInActivity, OcrMainActivity::class.java))
        finish()
    }
}