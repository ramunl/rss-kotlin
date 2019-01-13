package ru.rian.dynamics.ui

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import ru.rian.dynamics.R
import ru.rian.dynamics.retrofit.model.Feed
import ru.rian.dynamics.utils.FragmentId

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    private fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commit()
    }

    protected fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: FragmentId) {
        supportFragmentManager.inTransaction { replace(R.id.fragmentContainer, fragment, frameId.name) }
    }


    protected fun setFloatFlashFloatButton(feed:Feed) {
      //  var res = getNotificationRes(feed)
      //  buttonFloatDynamic.setImageDrawable(ContextCompat.getDrawable(this, res))
        /*buttonFloatDynamic.setOnClickListener { v ->
            //    BottomSheetChangePushFeedsDialogFragment.onClickProcess(getFragmentFeedSelected(), null);
            val bottomSheetDialogFragment = BottomSheetChangePushFeedsDialogFragment()
            val bundle = Bundle()
            bundle.putParcelable("feed", getFragmentFeedSelected())
            bottomSheetDialogFragment.setArguments(bundle)
            bottomSheetDialogFragment.show(
                (v.context as AppCompatActivity).supportFragmentManager,
                bottomSheetDialogFragment.getTag()
            )
        }*/
    }
}