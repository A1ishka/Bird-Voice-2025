package by.dis.birdvoice.helpers.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import by.dis.birdvoice.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
object CustomToast {
    @JvmStatic
    fun show(
        context: Context,
        message: CharSequence,
        @DrawableRes iconRes: Int? = null,
        duration: Int = Toast.LENGTH_SHORT,
        gravity: Int = Gravity.BOTTOM,
        yOffsetDp: Int = 64
    ) {
        val appCtx = context.applicationContext
        val inflater = LayoutInflater.from(appCtx)

        val parent: ViewGroup = (context as? Activity)
            ?.findViewById(android.R.id.content)
            ?: FrameLayout(appCtx)

        val view = inflater.inflate(R.layout.view_custom_toast, parent, false)
        val txt = view.findViewById<TextView>(R.id.toastText)
        val icon = view.findViewById<ImageView>(R.id.toastIcon)

        txt.text = message
        if (iconRes != null) {
            icon.setImageResource(iconRes)
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }

        val density = appCtx.resources.displayMetrics.density
        val toast = Toast(appCtx).apply {
            this.duration = duration
            this.view = view
            setGravity(gravity, 0, (yOffsetDp * density).toInt())
        }

        CoroutineScope(Dispatchers.Main).launch {
            toast.show()
        }
    }
}