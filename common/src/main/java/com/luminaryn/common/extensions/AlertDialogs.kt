package com.luminaryn.common.extensions

import android.app.AlertDialog.Builder as AAB
import androidx.appcompat.app.AlertDialog.Builder as AXB
import android.content.DialogInterface

@JvmOverloads
fun AAB.setPositiveButton (listener: DialogInterface.OnClickListener? = null): AAB {
    return setPositiveButton(AlertDialogs.YES, listener)
}

@JvmOverloads
fun AAB.setNegativeButton (listener: DialogInterface.OnClickListener? = null): AAB {
    return setNegativeButton(AlertDialogs.NO, listener)
}

fun AAB.useAlertIcon(): AAB {
    return setIcon(AlertDialogs.ALERT)
}

fun AAB.useInfoIcon(): AAB {
    return setIcon(AlertDialogs.INFO)
}

fun AAB.confirm (title: Int, message: Int, onYes: ((DialogInterface, Int)->Unit)) {
    setTitle(title)
    setMessage(message)
    useAlertIcon()
    setPositiveButton(onYes)
    setNegativeButton()
    show()
}

@JvmOverloads
fun AXB.setPositiveButton (listener: DialogInterface.OnClickListener? = null): AXB {
    return setPositiveButton(AlertDialogs.YES, listener)
}

@JvmOverloads
fun AXB.setNegativeButton (listener: DialogInterface.OnClickListener? = null): AXB {
    return setNegativeButton(AlertDialogs.NO, listener)
}

fun AXB.useAlertIcon(): AXB {
    return setIcon(AlertDialogs.ALERT)
}

fun AXB.useInfoIcon(): AXB {
    return setIcon(AlertDialogs.INFO)
}

fun AXB.confirm (title: Int, message: Int, onYes: ((DialogInterface, Int)->Unit)) {
    setTitle(title)
    setMessage(message)
    useAlertIcon()
    setPositiveButton(onYes)
    setNegativeButton()
    show()
}

object AlertDialogs {
    const val INFO = android.R.drawable.ic_dialog_info
    const val ALERT = android.R.drawable.ic_dialog_alert
    const val YES = android.R.string.yes
    const val NO = android.R.string.no
}