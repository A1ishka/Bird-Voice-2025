package by.dis.birdvoice.main.fragments.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentCollectionBinding
import by.dis.birdvoice.db.objects.CollectionBird
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import by.dis.birdvoice.main.rv.CollectionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CollectionFragment: BaseMainFragment() {

    private lateinit var binding: FragmentCollectionBinding
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    override var arrayOfViews = arrayListOf<ViewObject>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCollectionBinding.inflate(layoutInflater)
        binding.apply {
            arrayOfViews = arrayListOf(
                ViewObject(collectionTopRightCloud, "rc1"),
                ViewObject(collectionTopLeftCloud, "lc2"),
                ViewObject(collectionBottomLeftCloud, "lc1")
            )
        }

        animationUtils.commonDefineObjectsVisibility(arrayOfViews)
        animationUtils.commonObjectAppear(activityMain.getApp().getContext(), arrayOfViews, true)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        mainVM.setToolbarTitle(resources.getString(R.string.collection))

        activityMain.showBottomNav()

        mainVM.isCollectionEmptyInt.observe(activityMain) {
            if (it == 1) {
                val alphaInAnim = AnimationUtils.loadAnimation(activityMain, R.anim.common_alpha_enter)
                binding.apply {
                    collectionBird.startAnimation(alphaInAnim)
                    collectionText.startAnimation(alphaInAnim)

                    collectionBird.visibility = View.VISIBLE
                    collectionText.visibility = View.VISIBLE
                }
            } else if (it == 0) {
                val alphaOutAnim = AnimationUtils.loadAnimation(activityMain, R.anim.common_alpha_enter)
                binding.apply {
                    collectionBird.startAnimation(alphaOutAnim)
                    collectionText.startAnimation(alphaOutAnim)

                    collectionBird.visibility = View.GONE
                    collectionText.visibility = View.GONE
                }
            }
        }

        scope.launch {
            val list = activityMain.getCollectionDao().getCollection() as ArrayList
            val reversedList = arrayListOf<CollectionBird>()

            if (list == arrayListOf<CollectionBird>()) {
                activityMain.runOnUiThread {
                    mainVM.isCollectionEmptyInt.value = 1
                }
            }

            for (i in list.size - 1 downTo 0) reversedList.add(list[i])

            activityMain.runOnUiThread {
                binding.collectionRv.apply {
                    layoutManager = LinearLayoutManager(activityMain.getApp().getContext())
                    adapter = CollectionAdapter(
                        activityMain,
                        reversedList,
                        scope,
                        mainVM
                    )
                }
            }
        }

        activityMain.setToolbarAction(R.drawable.ic_menu) { activityMain.openDrawer() }
    }
}