package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.net.Post
import com.drake.net.Put
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityMainBinding
import com.isl.bcs.model.*
import com.isl.bcs.utils.Constants
import com.tamsiree.rxui.view.dialog.RxDialogSureCancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityMainBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            toolbar.tvTitle.text = getString(R.string.home)
            immersive(toolbar.toolbar, false)
            ivExit.setOnClickListener {
                onBackPressed()
            }
            ivCheck.setOnClickListener {
                openActivity<DBPullActivity>(
                    Constants.INTENT_TYPE to Constants.TYPE_CHECK
                )
            }
            ivInOut.setOnClickListener {
                openActivity<DBPullActivity>(
                    Constants.INTENT_TYPE to Constants.TYPE_INOUT
                )
            }
            ivTransfer.setOnClickListener {
                openActivity<DBPullActivity>(
                    Constants.INTENT_TYPE to Constants.TYPE_TRANSFER
                )
            }
            ivUpload.setOnClickListener {
                RxDialogSureCancel(this@MainActivity).apply {
                    setTitle(getString(R.string.hint))
                    setContent(getString(R.string.update_data))
                    setSure(getString(R.string.yes))
                    setCancel(getString(R.string.dialog_cancel))
                    setSureListener {
                        scopeDialog {
                            withIO {
                                val boxIn = LitePal.findAll<BoxIn>()
                                val boxOut = LitePal.findAll<BoxOut>()
                                val boxTransfer = LitePal.findAll<BoxTransfer>()
                                val boxCheck = LitePal.findAll<BoxCheck>()
                                val instTrxIn = LitePal.findAll<InstTrxIn>()
                                val instTrxOut = LitePal.findAll<InstTrxOut>()
                                com.drake.net.utils.withDefault {
                                    if (boxIn.isNotEmpty()) {
                                        Post<String>("/scan/stock") {
                                            json(Json.encodeToString(boxIn))
                                        }.await()
                                    }
                                    if (instTrxIn.isNotEmpty()) {
                                        Put<String>("/inst/in") {
                                            json(Json.encodeToString(instTrxIn))
                                        }.await()
                                    }
                                    if (boxOut.isNotEmpty()) {
                                        Put<String>("/scan/stock_out") {
                                            json(Json.encodeToString(boxOut))
                                        }.await()
                                    }
                                    if (instTrxOut.isNotEmpty()) {
                                        Put<String>("/inst/out") {
                                            json(Json.encodeToString(instTrxOut))
                                        }.await()
                                    }
                                    if (boxTransfer.isNotEmpty()) {
                                        Put<String>("/scan/stock_trf") {
                                            json(Json.encodeToString(boxTransfer))
                                        }.await()
                                    }
                                    if (boxTransfer.isNotEmpty()) {
                                        Put<String>("/scan/stock_check") {
                                            json(Json.encodeToString(boxCheck))
                                        }.await()
                                    }
                                }
                                LitePal.deleteAll<BoxIn>()
                                LitePal.deleteAll<BoxOut>()
                                LitePal.deleteAll<InstTrxIn>()
                                LitePal.deleteAll<InstTrxOut>()
                                LitePal.deleteAll<BoxTransfer>()
                                LitePal.deleteAll<BoxCheck>()
                            }
                            dismiss()
                        }
                    }
                    setCancelListener {
                        dismiss()
                    }
                }.show()
            }
        }
    }

    private var firstTime = 0L
    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime < 2000) {
            exitProcess(0)
        } else {
            firstTime = secondTime
            toast("Click once again to exit BarCodeSystem.")
        }
    }
}