package net.ixzyj.activity.receipts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.activity.camera.CameraActivity
import net.ixzyj.network.model.ReceiptsModelItem
import net.ixzyj.ocr.R
import net.ixzyj.utils.MyApplication.Companion.flagPage

class ReceiptsListAdapter(val resultList: List<ReceiptsModelItem>) :
    RecyclerView.Adapter<ReceiptsListAdapter.ReceiptsListViewHolder>() {
    private val bundle by lazy {
        Bundle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptsListViewHolder {
        return ReceiptsListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.receipts_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReceiptsListViewHolder, position: Int) {
        val result = resultList[position]
        val context = holder.itemView.context
        with(holder.itemView) {
            this.findViewById<TextView>(R.id.building_id).text = result.building_id[1].toString()
            this.findViewById<TextView>(R.id.order_name).text = result.name
            this.findViewById<TextView>(R.id.order_id).text = result.order_id[1].toString()
            this.findViewById<TextView>(R.id.partner_name).text = result.partner_id[1].toString()
            this.findViewById<TextView>(R.id.date).text = result.date
            setOnClickListener {
                bundle.apply {
                    putInt("RECEIPTS_ID", result.id)
                    putString("TOOLBAR_TITLE","物资入库扫码")
                    flagPage = 1
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtras(this)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = resultList.size

    class ReceiptsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}