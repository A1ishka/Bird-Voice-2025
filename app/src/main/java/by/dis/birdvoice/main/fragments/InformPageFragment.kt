package by.dis.birdvoice.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import by.dis.birdvoice.R
import by.dis.birdvoice.databinding.FragmentInformPageBinding
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.rv.InformAdapter

class InformPageFragment: BaseMainFragment() {

    private lateinit var binding: FragmentInformPageBinding
    private lateinit var informAdapter: InformAdapter

    private val navFun = {
        mainVM.navigateToWithDelay(R.id.action_informPageFragment_to_main_graph_record)
    }

    override var arrayOfViews = arrayListOf<ViewObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        informAdapter = InformAdapter(activityMain, navFun)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInformPageBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.informRv.apply {
            layoutManager = LinearLayoutManager(activityMain.getApp().getContext())
            adapter = informAdapter
        }

        activityMain.setPopBackCallback{
            informAdapter.animOut()
            activityMain.setRegValue(0)
        }

        activityMain.setToolbarAction(R.drawable.ic_arrow_back) {
            navigationBackAction {
                informAdapter.animOut()
                activityMain.setRegValue(0)
            }
        }

        mainVM.setToolbarTitle(resources.getString(R.string.how_it_works))
    }
}