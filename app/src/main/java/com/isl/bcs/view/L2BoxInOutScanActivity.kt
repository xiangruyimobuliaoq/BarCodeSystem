package com.isl.bcs.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityL2InstScanBinding
import com.isl.bcs.model.*
import com.tamsiree.rxui.view.dialog.RxDialogSure
import org.litepal.LitePal
import org.litepal.extension.find

class L2BoxInOutScanActivity : BaseActivity() {

    private lateinit var instData: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instData = intent.getStringExtra("instData")?.split(",")!!
        ActivityL2InstScanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            immersive(toolbar.toolbar, false)
            (findViewById<View>(R.id.tv_title) as TextView).text =
                getString(R.string.box_label_scan)
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
            scopeLife {
                withIO {
                    if (instData[0].startsWith("O")) {
                        val boxOutRes = LitePal.where("BOX_LABEL1 = ?", code).find<BoxOut>()
                        if (boxOutRes.isNotEmpty()) {
                            withMain {
                                RxDialogSure(this@L2BoxInOutScanActivity).apply {
                                    setContent(getString(R.string.dialog_scanned))
                                    setSure(getString(R.string.dialog_ok))
                                    setSureListener {
                                        cancel()
                                    }
                                }.show()
                            }
                        } else {
                            val boxInRes = LitePal.where("BOX_LABEL1 = ?", code).find<InstItemOut>()
                            withMain {
                                if (boxInRes.isNotEmpty()) {
                                    if (boxInRes[0].ITEM_1 != instData[5]) {
                                        toast(getString(R.string.error_item))
                                    } else {
                                        openActivity<BoxInOutPreviewActivity>(
                                            "boxData" to boxInRes[0].BOX_LABEL1,
                                            "scanKey" to boxInRes[0].SCAN_KEY,
                                            "instData" to intent.getStringExtra("instData")
                                        )
                                    }
                                } else {
                                    RxDialogSure(this@L2BoxInOutScanActivity).apply {
                                        setContent(getString(R.string.dialog_not_find))
                                        setSure(getString(R.string.dialog_ok))
                                        setSureListener {
                                            cancel()
                                        }
                                    }.show()
                                }
                            }
                        }
                    } else if (instData[0].startsWith("I")) {
                        val instItemOut = LitePal.where("BOX_LABEL1 = ?", code).find<InstItemOut>()
                        if (instItemOut.isNotEmpty()) {
                            withMain {
                                RxDialogSure(this@L2BoxInOutScanActivity).apply {
                                    setContent(getString(R.string.dialog_exist))
                                    setSure(getString(R.string.dialog_ok))
                                    setSureListener {
                                        cancel()
                                    }
                                }.show()
                            }
                        }
                        val find = LitePal.where("BOX_LABEL1 = ?", code).find<BoxIn>()
                        withMain {
                            if (find.isNotEmpty()) {
                                RxDialogSure(this@L2BoxInOutScanActivity).apply {
                                    setContent(getString(R.string.dialog_scanned))
                                    setSure(getString(R.string.dialog_ok))
                                    setSureListener {
                                        cancel()
                                    }
                                }.show()
                            } else {
                                openActivity<BoxInOutPreviewActivity>(
                                    "boxData" to code,
                                    "instData" to intent.getStringExtra("instData")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        receiver.apply {
            registerReceiver(this, IntentFilter().apply {
                addAction("com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED")
            })
        }
        if (instData[0].startsWith("O")) {
            val result = LitePal.where("INST_OUT_KEY = ?", instData[0]).find<InstTrxOut>()
            if (result.isNotEmpty() && result[0].SCAN_QTY >= result[0].PKG_QTY) {
                RxDialogSure(this@L2BoxInOutScanActivity).apply {
                    setContent(getString(R.string.dialog_complete))
                    setSure(getString(R.string.dialog_ok))
                    setSureListener {
                        cancel()
                        openActivity<InOutActivity>()
                    }
                }.show()
            }
        } else if (instData[0].startsWith("I")) {
            val result = LitePal.where("INST_IN_KEY = ?", instData[0]).find<InstTrxIn>()
            if (result.isNotEmpty() && result[0].SCAN_QTY >= result[0].PKG_QTY) {
                RxDialogSure(this@L2BoxInOutScanActivity).apply {
                    setContent(getString(R.string.dialog_complete))
                    setSure(getString(R.string.dialog_ok))
                    setSureListener {
                        cancel()
                        openActivity<InOutActivity>()
                    }
                }.show()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }
}