package net.ixzyj.view.material

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.activity.materiallist.MateriaListActivity
import net.ixzyj.activity.receiptsin.ReceiptsInActivity
import net.ixzyj.activity.receptioninscene.RecinSceneActivity
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
                    startActivity(context,ReceiptsInActivity(),data.title,"")
                }
                "补料出库"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_supp_out")
                }
                "返场入库"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_project_back")
                }
                "退场入库"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_project_return")
                }
                "物资调拨"->{
                    startActivity(context,MateriaListActivity(),data.title,"wh_internal")
                }
                "翻新出库"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_renew_out")
                }
                "翻新入库"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_renew_in")
                }
                "项目入库清点"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_project_out")
                }
                "翻新入库清点"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_supp_out")
                }
                "项目返场清点"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_project_back")
                }
                "项目退场清点"->{
                    startActivity(context,MateriaListActivity(),data.title,"material_project_return")
                }
            }
        }
    }

    fun startActivity(context: Context,activity: Activity,title:String,data:String){
        val intent = Intent(context, activity.javaClass)
        intent.putExtra("DATA",data)
        intent.putExtra("ACTIVITY_TITLE",title)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = dataList.size

    class MaterialListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val materialTitle = itemView.findViewById<TextView>(R.id.material_title)
        val materialBg = itemView.findViewById<CardView>(R.id.material_bg)
        val startCamera = itemView.findViewById<TextView>(R.id.start_camera)
    }
}