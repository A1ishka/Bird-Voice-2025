package by.dis.birdvoice.main.rv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.Recognition2RvItemBinding
import by.dis.birdvoice.db.objects.CollectionBird
import by.dis.birdvoice.main.MainActivity
import by.dis.birdvoice.main.vm.MainVM
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Recognition2Adapter(
    private val context: Context,
    private val mainVM: MainVM,
    private val activity: MainActivity,
    private val coroutineScope: CoroutineScope
    ) : RecyclerView.Adapter<Recognition2Adapter.Recognition2Holder>() {

    private val viewArray = arrayListOf<Recognition2RvItemBinding>()

    inner class Recognition2Holder(val binding: Recognition2RvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Recognition2Holder {
        val binding = Recognition2RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        viewArray.add(binding)
        enterAnimation(binding.root)

        return Recognition2Holder(binding)
    }

    override fun getItemCount(): Int = mainVM.getResults().size

    override fun onBindViewHolder(holder: Recognition2Holder, position: Int) {
        val databaseObject = CollectionBird(mainVM.getResults()[position].name, mainVM.getResults()[position].image)

        holder.binding.apply {
            var collectionList: List<CollectionBird>
            val isItemInCollection = mainVM.createMutableLiveInt()

            coroutineScope.launch {
                collectionList = activity.getCollectionDao().getCollection()

                activity.runOnUiThread {
                    if (!collectionList.contains(databaseObject)) isItemInCollection.value = 1
                    else isItemInCollection.value = 0

                    isItemInCollection.observe(activity) {
                        if (it == 1) addBirdAction(recognition2RvItemButton, databaseObject, isItemInCollection)
                        else if (it == 0) removeBirdAction(recognition2RvItemButton, databaseObject, isItemInCollection)
                    }
                }
            }

            recognition2RvItemImage.load(mainVM.getResults()[position].image) {
                crossfade(true)
                transformations(RoundedCornersTransformation(16f))
            }

//            val title = "${mainVM.getResults()[position].name} (${mainVM.getResults()[position].startTime}-${mainVM.getResults()[position].endTime} ${activity.resources.getText(R.string.sec)})"
            val title = mainVM.getResults()[position].name
            recognition2RvItemTitle.text = title
        }

        mainVM.recognition2Value.observe(activity) {
            if (it == true) for (i in viewArray) outAnimation(i.root)
        }
    }

    private fun enterAnimation(view: View){
        view.visibility = View.INVISIBLE
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.common_alpha_enter))
        view.visibility = View.VISIBLE
    }

    private fun outAnimation(view: View){
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.common_alpha_out))
        view.visibility = View.INVISIBLE
    }

    private fun addBirdAction(button: MaterialButton, databaseObject: CollectionBird, isItemInCollection: MutableLiveData<Int>) {
        activity.runOnUiThread {
            button.apply {
                setIconResource(R.drawable.ic_plus)
                text = ContextCompat.getString(activity, R.string.add_to_collection)

                setOnClickListener {
                    coroutineScope.launch { activity.getCollectionDao().insert(databaseObject) }
                    isItemInCollection.value = 0
                    mainVM.isCollectionEmptyInt.value = 0
                }
            }
        }
    }

    private fun removeBirdAction(button: MaterialButton, databaseObject: CollectionBird, isItemInCollection: MutableLiveData<Int>) {
        activity.runOnUiThread {
            button.apply {
                setIconResource(R.drawable.ic_added)
                text = ContextCompat.getString(activity, R.string.added)

                setOnClickListener {
                    coroutineScope.launch { activity.getCollectionDao().delete(databaseObject) }
                    isItemInCollection.value = 1
                }
            }
        }
    }
}