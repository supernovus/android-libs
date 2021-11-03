package com.luminaryn.common.extensions

import android.app.AlertDialog
import android.content.DialogInterface

@JvmOverloads
fun AlertDialog.Builder.setPositiveButton (listener: DialogInterface.OnClickListener? = null): AlertDialog.Builder {
    return setPositiveButton(AlertDialogs.YES, listener)
}

@JvmOverloads
fun AlertDialog.Builder.setNegativeButton (listener: DialogInterface.OnClickListener? = null): AlertDialog.Builder {
    return setNegativeButton(AlertDialogs.NO, listener)
}

fun AlertDialog.Builder.useAlertIcon(): AlertDialog.Builder {
    return setIcon(AlertDialogs.ALERT)
}

fun AlertDialog.Builder.useInfoIcon(): AlertDialog.Builder {
    return setIcon(AlertDialogs.INFO)
}

object AlertDialogs {
    const val INFO = android.R.drawable.ic_dialog_info
    const val ALERT = android.R.drawable.ic_dialog_alert
    const val YES = android.R.string.yes
    const val NO = android.R.string.no
}