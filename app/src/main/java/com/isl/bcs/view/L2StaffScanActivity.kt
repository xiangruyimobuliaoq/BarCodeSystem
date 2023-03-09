package com.isl.bcs.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityL2StaffScanBinding
import com.isl.bcs.model.Staff
import com.isl.bcs.utils.Constants
import com.isl.bcs.utils.Constants.Companion.currentStaff
import com.tamsiree.rxui.view.dialog.RxDialogSure
import org.litepal.LitePal
import org.litepal.extension.find

class L2StaffScanActivity : BaseActivity() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val code = p1?.getStringExtra("data")
            val arr = p1?.getByteArrayExtra("source_byte")
            Log.e("123", code + "   " + arr.toString())
            val staff = LitePal.where("id_no = ? ", code).find<Staff>()
            if (staff.isNotEmpty()) {
                currentStaff = staff[0]
                when (intent.getStringExtra(Constants.INTENT_TYPE)) {
                    Constants.TYPE_INOUT -> openActivity<InOutActivity>()
                    Constants.TYPE_TRANSFER -> openActivity<TransferActivity>()
                    Constants.TYPE_CHECK -> openActivity<CheckActivity>()
                }
                finish()
            } else {
                RxDialogSure(this@L2StaffScanActivity).apply {
                    setContent(getString(R.string.staff_code_error))
                    setSure(getString(R.string.dialog_ok))
                    setSureListener {
                        cancel()
                    }
                }.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityL2StaffScanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            immersive(toolbar.toolbar, false)
            (findViewById<View>(R.id.tv_title) as TextView).text = getString(R.string.staff_scan)
        }
    }

    override fun onResume() {
        receiver.apply {
            registerReceiver(this, IntentFilter().apply {
                addAction("com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED")
            })
        }
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }
}