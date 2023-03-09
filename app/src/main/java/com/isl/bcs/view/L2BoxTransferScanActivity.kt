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
import com.isl.bcs.databinding.ActivityBoxScanBinding
import com.isl.bcs.model.InstItemCheck
import com.isl.bcs.model.InstItemOut
import com.tamsiree.rxui.view.dialog.RxDialogSure
import org.litepal.LitePal
import org.litepal.extension.find

class L2BoxTransferScanActivity : BaseActivity() {

    private lateinit var instData: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instData = intent.getStringExtra("instData")?.split(",")!!
        ActivityBoxScanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            immersive(toolbar.toolbar, false)
            (findViewById<View>(R.id.tv_title) as TextView).text =
                getString(R.string.box_label_scan)
            imageView5.setImageResource(R.mipmap.home_icon_2)
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
            val instItemCheck = LitePal.where("INST_IN_KEY = ?", code).find<InstItemOut>()
            if (instItemCheck.isNotEmpty()) {
                openActivity<BoxCheckPreviewActivity>(
                    "boxData" to instItemCheck[0]
                )
            }else{
                RxDialogSure(this@L2BoxTransferScanActivity).apply {
                    setContent(getString(R.string.dialog_content))
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