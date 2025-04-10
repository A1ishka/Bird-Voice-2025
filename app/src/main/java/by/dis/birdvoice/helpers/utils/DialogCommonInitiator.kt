package by.dis.birdvoice.helpers.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import by.dis.birdvoice.databinding.DialogLogOutBinding
import by.dis.birdvoice.main.MainActivity

class DialogCommonInitiator {

    @Suppress("DEPRECATION")
    fun initDialog(activity: MainActivity, dialogBindingAction: (Dialog) -> Unit) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogBindingAction(dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = width - (width/5)
        dialog.window?.attributes = layoutParams

        dialog.show()
    }

    fun initCommonDialog(activity: MainActivity, dialogTextResources: ArrayList<String>, action: (Dialog) -> Unit, onCancelled: (() -> Unit)? = null) {
        initDialog(activity) { dialog ->
            val dialogBinding = DialogLogOutBinding.inflate(LayoutInflater.from(activity))
            dialog.setContentView(dialogBinding.root)

            dialogBinding.apply {

                dialogCommonTitle.text = dialogTextResources[0]
                dialogCommonBody.text = dialogTextResources[1]
                dialogCommonNo.text = dialogTextResources[2]
                dialogCommonYes.text = dialogTextResources[3]

                dialogCommonNo.setOnClickListener {
                    if (onCancelled != null) onCancelled()
                    dialog.dismiss()
                }
                dialogCommonYes.setOnClickListener { action(dialog) }
            }
        }
    }
}