package net.ixzyj.activity.result

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.ocr.R
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.MyApplication.Companion.predictor

class ResultListAdapter(val resultList: List<String>) :
    RecyclerView.Adapter<ResultListAdapter.ResultListViewHolder>() {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListViewHolder {
        return ResultListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.result_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ResultListViewHolder, position: Int) {
        val result = resultList[position]
        val context = holder.itemView.context
        with(holder) {
            resultText.apply {
                text = result
                setTextColor(Color.parseColor("#427ccd"))
            }
            resultIndex.apply {
                text = (position+1).toString()
                setTextColor(Color.parseColor("#427ccd"))
            }
            updateBtn.apply {
                visibility = View.INVISIBLE
                setOnClickListener {
                    val view = LayoutInflater.from(context).inflate(R.layout.set_edit, null)
                    val editText = view.findViewById<EditText>(R.id.update_edit)
                    editText.apply {
                        setText(result)
                        inputType = InputType.TYPE_CLASS_TEXT
                        setRawInputType(InputType.TYPE_CLASS_NUMBER)
                    }
                    AlertDialog.Builder(context)
                        .setTitle("修改")
                        .setView(view)
                        .setPositiveButton("确认") { dialog, which ->
                            val newValue = editText.text.toString()
                            predictor.outputResult.value?.removeAt(position)
                            predictor.outputResult.value?.add(position,newValue)
                            notifyDataSetChanged()
                        }
                        .create().show()
                }
            }
            if (result.length!=11){
                resultText.setTextColor(Color.parseColor("#dddddd"))
                resultIndex.setTextColor(Color.parseColor("#dddddd"))
                updateBtn.visibility = View.VISIBLE
            }
        }
    }
    override fun getItemCount(): Int = resultList.size

    class ResultListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val  resultText = itemView.findViewById<TextView>(R.id.text_result_list)
        val  resultIndex = itemView.findViewById<TextView>(R.id.index_reslt_list)
        val  updateBtn = itemView.findViewById<Button>(R.id.update_btn)
    }
}