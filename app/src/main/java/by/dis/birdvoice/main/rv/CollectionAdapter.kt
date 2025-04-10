package by.dis.birdvoice.main.rv

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import by.dis.birdvoice.databinding.CollectionDialogBinding
import by.dis.birdvoice.databinding.CollectionRvItemBinding
import by.dis.birdvoice.db.objects.CollectionBird
import by.dis.birdvoice.helpers.utils.AnimationUtils
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.MainActivity
import by.dis.birdvoice.main.vm.MainVM
import coil.load
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollectionAdapter(
    private val activity: MainActivity,
    private val list: ArrayList<CollectionBird>,
    private val scope: CoroutineScope,
    private val mainVM: MainVM
): RecyclerView.Adapter<CollectionAdapter.CollectionHolder>() {

    inner class CollectionHolder(val binding: CollectionRvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionHolder {
        val binding = CollectionRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: CollectionAdapter.CollectionHolder, position: Int) {
        holder.binding.apply {
            collectionRvItemTitle.text = list[position].name
            collectionRvItemImage.load(list[position].image) {
                crossfade(true)
                transformations(RoundedCornersTransformation(16f))
            }

            collectionRvItemDelete.setOnClickListener { initDeletionDialog(list[position], position) }
        }
    }

    override fun getItemViewType(position: Int): Int = position + 1

    private fun initDeletionDialog(item: CollectionBird, position: Int){
        val dialog = Dialog(activity)
        val dialogBinding = CollectionDialogBinding.inflate(LayoutInflater.from(activity))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = activity.resources.displayMetrics.widthPixels - (activity.resources.displayMetrics.widthPixels / 10)
        dialog.window?.attributes = layoutParams
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var arrayOfViews1: ArrayList<ViewObject>
        var arrayOfViews2: ArrayList<ViewObject>
        dialogBinding.apply {
            arrayOfViews1 = arrayListOf(
                ViewObject(dialog1Bird),
                ViewObject(dialog1TopLeftCloud, "lc1"),
                ViewObject(dialog1TopRightCloud, "rc1"),
                ViewObject(dialog1Header),
                ViewObject(dialog1Text),
                ViewObject(dialog1ButtonHeader)
            )
            arrayOfViews2 = arrayListOf(
                ViewObject(dialog2Bird),
                ViewObject(dialog2Text),
                ViewObject(dialog2BottomRightCloud, "rc2"),
                ViewObject(dialog2TopRightCloud, "rc1"),
                ViewObject(dialog2TopLeftCloud, "lc1")
            )
        }

        val animUtils = AnimationUtils()

        animUtils.commonDefineObjectsVisibility(arrayOfViews1)
        animUtils.commonObjectAppear(activity, arrayOfViews1, true)

        dialogBinding.dialog1No.setOnClickListener { dialog.dismiss() }
        dialogBinding.dialog1Yes.setOnClickListener {
            scope.launch {
                activity.getCollectionDao().delete(item)
                list.remove(item)

                activity.runOnUiThread {
                    notifyItemRemoved(position)
                    if (list == arrayListOf<CollectionBird>()) mainVM.isCollectionEmptyInt.value = 1
                }

                delay(1500)
                dialog.dismiss()
            }

            animUtils.commonObjectAppear(activity, arrayOfViews1)
            animUtils.commonObjectAppear(activity, arrayOfViews2, true)
        }

        dialog.show()
    }
}