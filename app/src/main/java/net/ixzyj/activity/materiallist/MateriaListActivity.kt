package net.ixzyj.activity.materiallist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.network.model.ReceptioninSceneItem
import net.ixzyj.ocr.databinding.ActivityRecinSceneBinding
import net.ixzyj.utils.DialogUtil

class MateriaListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecinSceneBinding
    private lateinit var materiaListAdapter: MateriaListAdapter
    private val viewModel by lazy {
        ViewModelProvider(this).get(MateriaViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecinSceneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
    }

    private fun initData() {
        val any = intent.extras?.get("DATA")
        viewModel.getListData(any.toString().trim()).observe(this, {
            val json:List<ReceptioninSceneItem> = Gson().fromJson(it, object : TypeToken<List<ReceptioninSceneItem>>() {}.type)
            when(json.size){
                0->doCreateTable()
                else->initView(json)
            }
        })
    }

    private fun initView(resultList: List<ReceptioninSceneItem>) {
        materiaListAdapter = MateriaListAdapter(resultList,intent.extras?.get("ACTIVITY_TITLE").toString())
        binding.recyclerViewReceScene.apply {
            layoutManager =
                LinearLayoutManager(this@MateriaListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = materiaListAdapter
            materiaListAdapter.notifyDataSetChanged()
        }
        binding.toolbar4.apply {
            title = intent.extras?.get("ACTIVITY_TITLE").toString()
            setNavigationOnClickListener {
                backHome()
            }
        }
    }

    private fun backHome() {
        startActivity(Intent(this, OcrMainActivity::class.java))
        finish()
    }

    fun doCreateTable() {
        DialogUtil.alertDialog("当前没有可以扫码的单据\n请在电脑端创建", this).apply {
            setOnDismissListener {
                startActivity(Intent(this@MateriaListActivity, OcrMainActivity::class.java))
                finish()
            }
        }
    }
}