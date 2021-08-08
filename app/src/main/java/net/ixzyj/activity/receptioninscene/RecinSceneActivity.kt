package net.ixzyj.activity.receptioninscene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.network.model.ReceptioninScene
import net.ixzyj.ocr.databinding.ActivityRecinSceneBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import com.google.gson.Gson
import net.ixzyj.activity.main.OcrMainActivity
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication

class RecinSceneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecinSceneBinding
    private lateinit var receptioninScene: ReceptioninScene
    private lateinit var recSceneListAdapter: RecSceneListAdapter
    private lateinit var errorHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecinSceneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHandler()
        initDatas()
        initViews()
    }

    private fun initHandler() {
        errorHandler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MyApplication.DATA_ERROR -> {
                        doDataErrorWork()
                    }
                }
            }
        }
    }

    private fun doDataErrorWork() {
        val alertDialog =
            DialogUtil.alertDialog("数据传输错误\n回退到主页重新进入", this)
        alertDialog.create()
        alertDialog.show()
        alertDialog.setOnDismissListener {
            backHome()
        }
    }

    private fun initDatas() {
        val bundle = intent.extras
        val data = bundle?.getString("REC_SCENE_INFO")
        try {
            receptioninScene = Gson().fromJson(data, ReceptioninScene::class.java)
        } catch (e: Exception) {
            errorHandler.sendEmptyMessage(MyApplication.DATA_ERROR)
        }
    }

    private fun initViews() {
        recSceneListAdapter = RecSceneListAdapter(receptioninScene)
        binding.recyclerViewReceScene.apply {
            layoutManager =
                LinearLayoutManager(this@RecinSceneActivity, LinearLayoutManager.VERTICAL, false)
            adapter = recSceneListAdapter
        }
        binding.toolbar4.apply {
            setNavigationOnClickListener {
                backHome()
            }
        }
    }

    private fun backHome(){
        startActivity(Intent(this@RecinSceneActivity, OcrMainActivity::class.java))
        finish()
    }
}