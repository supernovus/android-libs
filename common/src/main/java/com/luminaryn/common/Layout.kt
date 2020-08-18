package com.luminaryn.common

import android.content.Context
import android.util.TypedValue

object Layout {
    fun dp2px(context: Context, dp: Int) : Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    fun getDim(context: Context, resourceId: Int) : Int {
        return context.resources.getDimension(resourceId).toInt()
    }
}