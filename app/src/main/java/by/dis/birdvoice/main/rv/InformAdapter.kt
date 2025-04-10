package by.dis.birdvoice.main.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import by.dis.birdvoice.R
import by.dis.birdvoice.main.MainActivity
import com.google.android.material.button.MaterialButton

class InformAdapter(private val activity: MainActivity, private val navFun: () -> Unit) : RecyclerView.Adapter<InformAdapter.InformHolder>() {

    private val viewArray = arrayListOf<View>()

    inner class InformHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformHolder {
        val layoutResId = when(viewType) {
            ITEM_TYPE_1 -> R.layout.inform_rv_item_1
            ITEM_TYPE_2 -> R.layout.inform_rv_item_2
            ITEM_TYPE_3 -> R.layout.inform_rv_item_3
            ITEM_TYPE_4 -> R.layout.inform_rv_item_4
            ITEM_TYPE_5 -> R.layout.inform_rv_item_5
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)

        viewArray.add(view)

        view.visibility = View.INVISIBLE
        view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.common_alpha_enter))
        view.visibility = View.VISIBLE

        return InformHolder(view)
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: InformHolder, position: Int) {
        if (position == 4){
            holder.itemView.findViewById<MaterialButton>(R.id.inform_rv_item_5_try_button).setOnClickListener {
                animOut()
                activity.setRegValue(0)
                navFun()
            }
        }
    }

    override fun getItemViewType(position: Int): Int = position

    companion object {
        const val ITEM_TYPE_1 = 0
        const val ITEM_TYPE_2 = 1
        const val ITEM_TYPE_3 = 2
        const val ITEM_TYPE_4 = 3
        const val ITEM_TYPE_5 = 4
    }

    fun animOut(){
        for (i in viewArray) {
            i.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.common_alpha_out))
            i.visibility = View.INVISIBLE
        }
    }
}