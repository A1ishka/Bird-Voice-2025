package by.dis.birdvoice.helpers.utils

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import by.dis.birdvoice.R

class AnimationUtils {

    fun commonDefineObjectsVisibility(arrayOfViews: ArrayList<ViewObject>){
        if (arrayOfViews[0].view.visibility == View.VISIBLE) for (i in arrayOfViews) i.view.visibility = View.INVISIBLE
        else for (i in arrayOfViews) i.view.visibility = View.VISIBLE
    }

    fun commonObjectAppear(context: Context, arrayOfViews: ArrayList<ViewObject>, enter: Boolean = false){

        lateinit var leftCloudAnim1: Animation
        lateinit var leftCloudAnim2: Animation
        lateinit var rightCloudAnim1: Animation
        lateinit var rightCloudAnim2: Animation
        lateinit var alphaAnim: Animation

        if (enter) {
            leftCloudAnim1 = AnimationUtils.loadAnimation(context, R.anim.common_left_cloud_enter_1) //lc1
            leftCloudAnim2 = AnimationUtils.loadAnimation(context, R.anim.common_left_cloud_enter_2) //lc2
            rightCloudAnim1 = AnimationUtils.loadAnimation(context, R.anim.common_right_cloud_enter_1) //rc1
            rightCloudAnim2 = AnimationUtils.loadAnimation(context, R.anim.common_right_cloud_enter_2) //rc2
            alphaAnim = AnimationUtils.loadAnimation(context, R.anim.common_alpha_enter) //a
        } else {
            leftCloudAnim1 = AnimationUtils.loadAnimation(context, R.anim.common_left_cloud_out_1) //lc1
            leftCloudAnim2 = AnimationUtils.loadAnimation(context, R.anim.common_left_cloud_out_2) //lc2
            rightCloudAnim1 = AnimationUtils.loadAnimation(context, R.anim.common_right_cloud_out_1) //rc1
            rightCloudAnim2 = AnimationUtils.loadAnimation(context, R.anim.common_right_cloud_out_2) //rc2
            alphaAnim = AnimationUtils.loadAnimation(context, R.anim.common_alpha_out) //a
        }

        for (i in arrayOfViews){
            when (i.animationType) {
                "lc1" -> i.view.startAnimation(leftCloudAnim1)
                "lc2" -> i.view.startAnimation(leftCloudAnim2)
                "rc1" -> i.view.startAnimation(rightCloudAnim1)
                "rc2" -> i.view.startAnimation(rightCloudAnim2)
                "a" -> i.view.startAnimation(alphaAnim)
            }
        }

        commonDefineObjectsVisibility(arrayOfViews)
    }
}