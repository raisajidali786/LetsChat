package com.rsa.letschat.utils

import android.app.ProgressDialog
import android.content.Context
import com.rsa.letschat.R

object Helper {
    var dialog: ProgressDialog? = null


    fun progressDialog(context: Context?, massage: String?) {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
        dialog = ProgressDialog(context, R.style.AlertDialogStyle)
        dialog!!.setTitle("Please Wait")
        dialog!!.setMessage(massage)
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.show()
    }

    fun dialogDismiss() {
        dialog!!.dismiss()
        dialog!!.cancel()
    }

}