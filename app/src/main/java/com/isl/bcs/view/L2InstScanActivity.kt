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
import com.isl.bcs.databinding.ActivityL2InstScanBinding
import com.tamsiree.rxui.view.dialog.RxDialogSure

class L2InstScanActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityL2InstScanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            immersive(toolbar.toolbar, false)
            (findViewById<View>(R.id.tv_title) as TextView).text = getString(R.string.ins_scan)
            btnExit.setOnClickListener {
                finish()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val code = p1?.getStringExtra("data")
            val arr = p1?.getByteArrayExtra("source_byte")
            Log.e("123", code + "   " + arr.toString())
            if (!code.isNullOrEmpty()) {
                val split = code.split(",")
                if (split.isEmpty() || split.size < 16) {
                    RxDialogSure(this@L2InstScanActivity).apply {
                        setContent(getString(R.string.error_code))
                        setSure(getString(R.string.dialog_ok))
                        setSureListener {
                            cancel()
                        }
                    }.show()
                } else {
                    openActivity<InstPreviewActivity>(
                        "data" to code
                    )
                }
            } else {
                RxDialogSure(this@L2InstScanActivity).apply {
                    setContent(getString(R.string.error_code))
                    setSure(getString(R.string.dialog_ok))
                    setSureListener {
                        cancel()
                    }
                }.show()
            }
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

