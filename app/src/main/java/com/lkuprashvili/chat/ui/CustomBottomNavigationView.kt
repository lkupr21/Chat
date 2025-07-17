package com.lkuprashvili.chat.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lkuprashvili.chat.R


class CustomBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val navHome: LinearLayout
    private val navProfile: LinearLayout

    private var listener: OnNavigationItemSelectedListener? = null

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.custom_bottom_navigation, this, true)

        navHome = findViewById(R.id.navHome)
        navProfile = findViewById(R.id.navProfile)

        setupListeners()
    }

    private fun setupListeners() {
        navHome.setOnClickListener {
            listener?.onHomeSelected()
            setHomeSelected(true)
            setProfileSelected(false)
        }

        navProfile.setOnClickListener {
            listener?.onProfileSelected()
            setHomeSelected(false)
            setProfileSelected(true)
        }

    }

    fun setHomeSelected(selected: Boolean) {
        navHome.background =
            if (selected) ContextCompat.getDrawable(context, R.color.black) else null
    }

    fun setProfileSelected(selected: Boolean) {
        navProfile.background =
            if (selected) ContextCompat.getDrawable(context, R.color.black) else null
    }

    interface OnNavigationItemSelectedListener {
        fun onHomeSelected()
        fun onProfileSelected()
    }
}