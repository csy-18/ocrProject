package net.ixzyj.view.material

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.activity.receipts.ReceiptsActivity
import net.ixzyj.ocr.R

class MaterialListAdapter(val dataList: List<MaterialModel>) :
    RecyclerView.Adapter<MaterialListAdapter.MaterialListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialListViewHolder {
        return MaterialListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.material_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MaterialListViewHolder, position: Int) {
        val data = dataList[position]
        val context = holder.itemView.context
        with(holder){
            materialTitle.text = data.title
            materialBg.setCardBackgroundColor(context.getColor(data.color))
            startCamera.setTextColor(context.getColor(data.color))
        }
        holder.itemView.setOnClickListener {
            when(data.title){
                "物资入库"->{
                    context.startActivity(Intent(context,ReceiptsActivity::class.java))
                }
            }
        }

    }

    override fun getItemCount(): Int = dataList.size

    class MaterialListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val materialTitle = itemView.findViewById<TextView>(R.id.material_title)
        val materialBg = itemView.findViewById<CardView>(R.id.material_bg)
        val startCamera = itemView.findViewById<TextView>(R.id.start_camera)
    }
}