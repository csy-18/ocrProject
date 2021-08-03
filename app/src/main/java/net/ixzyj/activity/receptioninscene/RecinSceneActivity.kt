package net.ixzyj.activity.receptioninscene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.network.model.ReceptioninScene
import net.ixzyj.ocr.databinding.ActivityRecinSceneBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import com.google.gson.Gson
import net.ixzyj.activity.main.OcrMainActivity

class RecinSceneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecinSceneBinding
    private lateinit var receptioninScene: ReceptioninScene
    private lateinit var recSceneListAdapter: RecSceneListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecinSceneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initDatas()
        initViews()
    }

    private fun initDatas() {
        val bundle = intent.extras
        val data = bundle?.getString("REC_SCENE_INFO")
        data.toString().logi()
        receptioninScene = Gson().fromJson(data, ReceptioninScene::class.java)
    }

    private fun initViews() {
        recSceneListAdapter = RecSceneListAdapter(receptioninScene)
        binding.recyclerViewReceScene.apply {
            layoutManager = LinearLayoutManager(this@RecinSceneActivity,LinearLayoutManager.VERTICAL,false)
            adapter = recSceneListAdapter
        }
        binding.toolbar4.apply {
            setNavigationOnClickListener {
                startActivity(Intent(this@RecinSceneActivity, OcrMainActivity::class.java))
                finish()
            }
        }
    }
}