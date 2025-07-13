package com.lkuprashvili.chat.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior

class CustomBottomNavigationBehavior @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HideBottomViewOnScrollBehavior<CustomBottomNavigationView>(context, attrs)