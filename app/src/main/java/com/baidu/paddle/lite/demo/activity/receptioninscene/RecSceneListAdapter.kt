package com.baidu.paddle.lite.demo.activity.receptioninscene

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baidu.paddle.lite.demo.activity.camera.CameraActivity
import com.baidu.paddle.lite.demo.network.model.ReceiptsModelItem
import com.baidu.paddle.lite.demo.network.model.ReceptioninSceneItem
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi

class RecSceneListAdapter(val resultList: List<ReceptioninSceneItem>) :
    RecyclerView.Adapter<RecSceneListAdapter.RecSceneListViewHolder>() {
    private val bundle by lazy {
        Bundle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecSceneListViewHolder {
        return RecSceneListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rec_scene_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecSceneListViewHolder, position: Int) {
        val result = resultList[position]
        val context = holder.itemView.context
        with(holder.itemView) {
            this.findViewById<TextView>(R.id.order_name_scene).text = result.name
            this.findViewById<TextView>(R.id.order_id_scene).text = result.id.toString()
            this.findViewById<TextView>(R.id.order_date_scene).text = result.date
            this.findViewById<TextView>(R.id.building_name).text = result.building_id[1].toString()
            setOnClickListener {
                bundle.apply {
                    putString("TOOLBAR_TITLE","现场接收清点扫码")
                    putInt("FLAG",2)
                    putInt("ORDER_ID",result.id)
                    putInt("WAREHOUSE_ID",result.warehouse_id[0].toString().toFloat().toInt())
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtras(this)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = resultList.size

    class RecSceneListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}