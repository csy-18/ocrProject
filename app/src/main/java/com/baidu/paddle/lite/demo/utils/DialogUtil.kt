package com.baidu.paddle.lite.demo.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.ocr.databinding.AlertDialogBinding
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.DONE
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion._context
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.getContext

object DialogUtil {
    var dialog: Dialog? = null

    fun alertDialog(message: String,activity: Activity): Dialog {
        val alertDialogBinding =
            AlertDialogBinding.inflate(activity.layoutInflater)
        val view = alertDialogBinding.root
        val alertContent = alertDialogBinding.alertContent
        val dialogConfirm = alertDialogBinding.dialogConfirm
        return Dialog(activity, R.style.DialogTheme).apply {
            alertContent.setText(message)
            setContentView(view)
            window!!.apply {
                setGravity(Gravity.CENTER)
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            show()
            dialogConfirm.setOnClickListener {
                dismiss()
            }
        }
    }

    fun progressBarDialog(activity: Activity) : Dialog{
        return Dialog(activity, R.style.DialogTheme).apply {
            setContentView(R.layout.progress_dialog)
            window!!.apply {
                setGravity(Gravity.CENTER)
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            create()
        }
    }
}