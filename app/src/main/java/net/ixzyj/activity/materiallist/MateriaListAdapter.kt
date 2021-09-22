package net.ixzyj.activity.materiallist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.network.model.ReceptioninSceneItem
import net.ixzyj.ocr.R
import net.ixzyj.utils.MyApplication

class MateriaListAdapter(private val resultList: List<ReceptioninSceneItem>) :
    RecyclerView.Adapter<MateriaListAdapter.MateriaListViewHolder>() {

    class MateriaListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val bundle by lazy {
        Bundle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaListViewHolder {
        return MateriaListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rec_scene_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MateriaListViewHolder, position: Int) {
        val result = resultList[position]
        val context = holder.itemView.context
        with(holder.itemView) {
            this.findViewById<TextView>(R.id.order_name_scene).text = result.name
            this.findViewById<TextView>(R.id.order_id_scene).text = result.id.toString()
            this.findViewById<TextView>(R.id.order_date_scene).text = result.date
            this.findViewById<TextView>(R.id.building_name).text = result.building_id[1].toString()
            setOnClickListener {
                bundle.apply {
                    putString("TOOLBAR_TITLE", "项目入库清点-${result.building_id[1]}")
                    putInt("ORDER_ID", result.id)
                    putInt("WAREHOUSE_ID", result.warehouse_id[0].toString().toFloat().toInt())
                    MyApplication.flagPage = 2
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtras(this)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = resultList.size
}