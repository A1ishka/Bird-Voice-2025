package by.dis.birdvoice.main.fragments.recognition.recognition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentRecognition2Binding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.fragments.BaseMainFragment
import by.dis.birdvoice.main.rv.Recognition2Adapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class Recognition2Fragment : BaseMainFragment() {

    private lateinit var binding: FragmentRecognition2Binding
    override var arrayOfViews = arrayListOf<ViewObject>()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecognition2Binding.inflate(inflater, container, false)

        activityMain.setPopBackCallback { mainVM.recognition2Value.value = true }

        binding.emptyStateButton.setOnClickListener {
            navigationBackAction { mainVM.recognition2Value.value = true }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        activityMain.showBottomNav()

        isEmptyState()

        mainVM.setToolbarTitle(resources.getString(R.string.recognition_results))
        activityMain.setToolbarAction(R.drawable.ic_arrow_back) {
            navigationBackAction { mainVM.recognition2Value.value = true }
        }
    }

    override fun onResume() {
        super.onResume()

        isEmptyState()
    }

    private fun isEmptyState() {
        val results = mainVM.getResults()
        Log.d("is empty state", results.isEmpty().toString())
        if (results.isEmpty()) {
            showEmptyState()
        } else {
            showList()
        }
    }

    private fun showList() = with(binding) {
        emptyState.visibility = View.GONE
        recognition2Rv.visibility = View.VISIBLE
        recognition2Rv.layoutManager = LinearLayoutManager(requireContext())
        recognition2Rv.adapter = Recognition2Adapter(
            requireContext(),
            mainVM,
            activityMain,
            scope
        )
    }

    private fun showEmptyState() = with(binding) {
        recognition2Rv.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
}