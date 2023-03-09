package com.isl.bcs.view

import android.os.Bundle
import com.drake.net.Put
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.withDefault
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityTransferBinding
import com.isl.bcs.model.BoxTransfer
import com.tamsiree.rxui.view.dialog.RxDialogSureCancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll


/**
 * 创建者     彭龙
 * 创建时间   2021/8/16 2:30 下午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class TransferActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityTransferBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.transfer)
            immersive(toolbar.toolbar, false)
            ivUpload.setOnClickListener {
                RxDialogSureCancel(this@TransferActivity).apply {
                    setTitle(getString(R.string.hint))
                    setContent(getString(R.string.update_data))
                    setSure(getString(R.string.yes))
                    setCancel(getString(R.string.dialog_cancel))
                    setSureListener {
                        scopeDialog {
                            withIO {
                                val boxTransfer = LitePal.findAll<BoxTransfer>()
                                withDefault {
                                    if (boxTransfer.isNotEmpty()) {
                                        Put<String>("/scan/stock_trf") {
                                            json(Json.encodeToString(boxTransfer))
                                        }.await()
                                    }
                                }
                                LitePal.deleteAll<BoxTransfer>()
                            }
                            dismiss()
                            openActivity<MainActivity>()
                        }
                    }
                    setCancelListener {
                        dismiss()
                    }
                }.show()
            }
            ivScan.setOnClickListener {
                openActivity<L2BoxTransferScanActivity>()
            }
        }
    }
}