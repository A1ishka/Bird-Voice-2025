package by.dis.birdvoice.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.dis.birdvoice.helpers.utils.HelpFunctions
import by.dis.birdvoice.helpers.utils.AnimationUtils
import by.dis.birdvoice.helpers.utils.ViewObject
import by.dis.birdvoice.main.MainActivity
import by.dis.birdvoice.main.vm.MainVM

abstract class BaseMainFragment: Fragment() {

    val mainVM: MainVM by activityViewModels()
    val animationUtils = AnimationUtils()
    lateinit var helpFunctions : HelpFunctions

    abstract var arrayOfViews: ArrayList<ViewObject>
    lateinit var activityMain: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMain = activity as MainActivity
        helpFunctions = HelpFunctions(activityMain.getApp())
    }

    fun navigationBackAction(action: () -> Unit){
        mainVM.navigateUpWithDelay()
        action()
    }
}