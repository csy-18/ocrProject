package net.ixzyj.activity.result

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ixzyj.ocr.R
import net.ixzyj.utils.CodeUtils
import net.ixzyj.utils.MyApplication.Companion.predictor

class ResultListAdapter(val resultList: List<String>, val handler: Handler) :
    RecyclerView.Adapter<ResultListAdapter.ResultListViewHolder>() {

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
                text = (position + 1).toString()
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
                            predictor.outputResult.value?.add(position, newValue)
                            notifyDataSetChanged()
                        }
                        .create().show()
                }
            }
            var verify = false
            if (result.length > 10) {
                val sequence = result.subSequence(0, 10).toString()
                val genElscodeCkCode = CodeUtils.genElscodeCkCode(sequence)
                verify = genElscodeCkCode.equals(result)
            }
            if (result.length != 11 || !verify) {
                resultText.setTextColor(Color.parseColor("#dddddd"))
                resultIndex.setTextColor(Color.parseColor("#dddddd"))
                updateBtn.visibility = View.VISIBLE
                val message = Message()
                message.what = ResultActivity.RESULT_ADAPTER
                message.obj = position+1
                handler.sendMessage(message)
            }
        }
    }

    override fun getItemCount(): Int = resultList.size

    class ResultListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultText = itemView.findViewById<TextView>(R.id.text_result_list)
        val resultIndex = itemView.findViewById<TextView>(R.id.index_reslt_list)
        val updateBtn = itemView.findViewById<Button>(R.id.update_btn)
    }
}