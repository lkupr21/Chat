package com.lkuprashvili.chat.utils

import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout

object AppBarManager {

    fun hide(appBar: AppBarLayout?) {
        appBar?.let {
            it.visibility = View.GONE
            it.layoutParams = it.layoutParams.apply {
                height = 0
            }
            it.requestLayout()
        }
    }

    fun show(appBar: AppBarLayout?) {
        appBar?.let {
            it.visibility = View.VISIBLE
            it.layoutParams = it.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            it.requestLayout()
        }
    }
}