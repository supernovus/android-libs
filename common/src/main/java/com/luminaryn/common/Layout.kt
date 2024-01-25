package com.luminaryn.common

import android.content.Context
import android.util.TypedValue

object Layout {
    fun dp2px(context: Context, dp: Int) : Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    fun resDim(context: Context, resourceId: Int): Float {
        return context.resources.getDimension(resourceId)
    }

    fun intDim(context: Context, resourceId: Int) : Int {
        return resDim(context, resourceId).toInt()
    }

    @Deprecated("getDim() is an old API, use intDim() or resDim() instead", ReplaceWith("intDim(context, resourceId)"))
    fun getDim(context: Context, resourceId: Int) : Int {
        return intDim(context, resourceId)
    }

}