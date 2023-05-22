package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.Put
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityInOutBinding
import com.isl.bcs.model.*
import com.tamsiree.rxui.view.dialog.RxDialogSureCancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll
import org.litepal.extension.saveAll

class InOutActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityInOutBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.in_out)
            immersive(toolbar.toolbar, false)
            ivUpload.setOnClickListener {
                RxDialogSureCancel(this@InOutActivity).apply {
                    setTitle(getString(R.string.hint))
                    setContent(getString(R.string.update_data))
                    setSure(getString(R.string.yes))
                    setCancel(getString(R.string.dialog_cancel))
                    setSureListener {
                        scopeDialog {
                            withIO {
                                val boxIn = LitePal.findAll<BoxIn>()
                                val boxOut = LitePal.findAll<BoxOut>()
                                val instTrxIn = LitePal.findAll<InstTrxIn>()
                                val instTrxOut = LitePal.findAll<InstTrxOut>()
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
                                val remain = Get<List<Remain>?>("/inst/remain").await()
                                LitePal.deleteAll<Remain>()
                                remain?.saveAll()
                                LitePal.deleteAll<BoxIn>()
                                LitePal.deleteAll<BoxOut>()
                                LitePal.deleteAll<InstTrxIn>()
                                LitePal.deleteAll<InstTrxOut>()
                            }
                            dismiss()
                            openActivity<MainActivity>()
                            finish()
                        }
                    }
                    setCancelListener {
                        dismiss()
                    }
                }.show()
            }
            ivScan.setOnClickListener {
                openActivity<L2InstScanActivity>()
            }
        }
    }
}