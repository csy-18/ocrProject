package net.ixzyj.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.view.Gravity
import android.view.ViewGroup
import coil.load
import com.bumptech.glide.Glide
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.AlertDialogBinding
import net.ixzyj.ocr.databinding.PhotoDialogBinding

object DialogUtil {

    fun alertDialog(message: String,activity: Activity): Dialog {
        val alertDialogBinding =
            AlertDialogBinding.inflate(activity.layoutInflater)
        val view = alertDialogBinding.root
        val alertContent = alertDialogBinding.alertContent
        val dialogConfirm = alertDialogBinding.dialogConfirm
        return Dialog(activity, R.style.DialogTheme).apply {
            alertContent.text = message
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

    fun photoDialog(activity: Activity,bitmap: Bitmap) : Dialog{
        val photoDialogBinding =
            PhotoDialogBinding.inflate(activity.layoutInflater)
        val view = photoDialogBinding.root
        val photoImage = photoDialogBinding.photoImage
        photoImage.load(bitmap){
            crossfade(true)
            placeholder(R.drawable.ic_baseline_photo_24)
            error(R.drawable.ic_baseline_error_outline_24)
        }
        return Dialog(activity, R.style.DialogTheme).apply {
            setContentView(view)
            window!!.apply {
                setWindowAnimations(R.style.main_menu_animStyle)
                setGravity(Gravity.CENTER)
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            photoImage.setOnClickListener {
                dismiss()
            }
            show()
        }
    }
}