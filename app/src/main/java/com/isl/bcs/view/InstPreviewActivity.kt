package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityInstPreviewBinding
import com.isl.bcs.model.InstTrxIn
import com.isl.bcs.model.InstTrxOut
import com.isl.bcs.utils.Constants
import com.tamsiree.rxui.view.dialog.RxDialogSure
import com.tamsiree.rxui.view.dialog.RxDialogSureCancel
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*

class InstPreviewActivity : BaseActivity() {
    //    private val vm: InOutViewModel by lazy {
//        ViewModelProvider(App.app, defaultViewModelProviderFactory).get(InOutViewModel::class.java)
//    }
    private lateinit var mBinding: ActivityInstPreviewBinding
    private lateinit var data: List<String>
    private lateinit var mInstDataOut: InstTrxOut
    private lateinit var mInstDataIn: InstTrxIn

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = intent.getStringExtra("data")!!.split(",")
        mBinding = ActivityInstPreviewBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.ins_view)
            immersive(toolbar.toolbar, false)
            data.let {
                tvDocNum.text = it[0]
                tvDate.text = it[2]
                tvItem.text = it[5]
                tvName.text = it[6]
                tvWhLoc1.text = it[10]
                tvWhLoc2.text = it[11]
                tvTotalQty.text = it[7]
                if (it[0].startsWith("I")) {
                    ivFlag.setImageResource(R.mipmap.box_details_icon_in)
                } else {
                    ivFlag.setImageResource(R.mipmap.box_details_icon_out)
                }
            }
            btnScanBox.setOnClickListener {
                if (data[0].startsWith("I")) {
                    if (mInstDataIn.SCAN_QTY >= mInstDataIn.PKG_QTY) {
                        RxDialogSure(this@InstPreviewActivity).apply {
                            setContent(getString(R.string.dialog_complete))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                            }
                        }.show()
                    } else
                        openActivity<SunMiBoxInOutScanActivity>(
                            "instData" to intent.getStringExtra("data")
                        )
                } else if (data[0].startsWith("O")) {
                    if (mInstDataOut.SCAN_QTY >= mInstDataOut.PKG_QTY) {
                        RxDialogSure(this@InstPreviewActivity).apply {
                            setContent(getString(R.string.dialog_complete))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                            }
                        }.show()
                    } else
                        openActivity<SunMiBoxInOutScanActivity>(
                            "instData" to intent.getStringExtra("data")
                        )
                }
            }
            btnScanInst.setOnClickListener {
                if (data[0].startsWith("I")) {
                    if (mInstDataIn.SCAN_QTY < mInstDataIn.PKG_QTY) {
                        RxDialogSureCancel(this@InstPreviewActivity).apply {
                            setTitle(getString(R.string.notice))
                            setContent(getString(R.string.dialog_complete_exit))
                            setSure(getString(R.string.dialog_ok))
                            setCancel(getString(R.string.dialog_cancel))
                            setSureListener {
                                openActivity<InOutActivity>()
                            }
                            setCancelListener {
                                this.cancel()
                            }
                        }.show()
                    } else {
                        openActivity<InOutActivity>()
                    }
                } else if (data[0].startsWith("O")) {
                    if (mInstDataOut.SCAN_QTY < mInstDataOut.PKG_QTY) {
                        RxDialogSureCancel(this@InstPreviewActivity).apply {
                            setTitle(getString(R.string.notice))
                            setContent(getString(R.string.dialog_complete_exit))
                            setSure(getString(R.string.dialog_ok))
                            setCancel(getString(R.string.dialog_cancel))
                            setSureListener {
                                openActivity<InOutActivity>()
                            }
                            setCancelListener {
                                this.cancel()
                            }
                        }.show()
                    } else {
                        openActivity<InOutActivity>()
                    }
                }
            }
        }
    }

    private fun getCurrentInst() {
        if (data[0].startsWith("I")) {
            val result =
                LitePal.where("INST_IN_KEY = ?", data[0])
                    .find<InstTrxIn>()
            if (result.isEmpty()) {
                mInstDataIn = InstTrxIn(
                    data[0],
                    data[7].toDouble().toInt(),
                    data[12].toInt(),
                    data[13] == "1",
                    SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss",
                        Locale.CHINA
                    ).format(System.currentTimeMillis()),
                    Constants.currentStaff?.id_no
                )
                mInstDataIn.save()
            } else {
                mInstDataIn = result[0]
            }
            mBinding.tvScanQty.text = mInstDataIn.SCAN_QTY.toString()
            mBinding.tvRemianQty.text = (mInstDataIn.PKG_QTY - mInstDataIn.SCAN_QTY).toString()
        } else if (data[0].startsWith("O")) {
            val result =
                LitePal.where("INST_OUT_KEY = ?", data[0])
                    .find<InstTrxOut>()
            if (result.isEmpty()) {
                mInstDataOut = InstTrxOut(
                    data[0],
                    data[7].toDouble().toInt(),
                    data[12].toInt(),
                    data[13] == "1",
                    SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss",
                        Locale.CHINA
                    ).format(System.currentTimeMillis()),
                    Constants.currentStaff?.id_no
                )
                mInstDataOut.save()
            } else {
                mInstDataOut = result[0]
            }
            mBinding.tvScanQty.text = mInstDataOut.SCAN_QTY.toString()
            mBinding.tvRemianQty.text = (mInstDataOut.PKG_QTY - mInstDataOut.SCAN_QTY).toString()
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentInst()
    }

    override fun onBackPressed() {
        if (data[0].startsWith("I")) {
            if (mInstDataIn.SCAN_QTY < mInstDataIn.PKG_QTY) {
                RxDialogSureCancel(this).apply {
                    setTitle(getString(R.string.notice))
                    setContent(getString(R.string.dialog_complete_exit))
                    setSure(getString(R.string.dialog_ok))
                    setCancel(getString(R.string.dialog_cancel))
                    setSureListener {
                        super.onBackPressed()
                    }
                    setCancelListener {
                        cancel()
                    }
                }.show()
            } else {
                super.onBackPressed()
            }
        } else if (data[0].startsWith("O")) {
            if (mInstDataOut.SCAN_QTY < mInstDataOut.PKG_QTY) {
                RxDialogSureCancel(this).apply {
                    setTitle(getString(R.string.notice))
                    setContent(getString(R.string.dialog_complete_exit))
                    setSure(getString(R.string.dialog_ok))
                    setCancel(getString(R.string.dialog_cancel))
                    setSureListener {
                        super.onBackPressed()
                    }
                    setCancelListener {
                        cancel()
                    }
                }.show()
            } else {
                super.onBackPressed()
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (data[0].startsWith("I")) {
                    if (mInstDataIn.SCAN_QTY < mInstDataIn.PKG_QTY) {
                        RxDialogSureCancel(this).apply {
                            setTitle(getString(R.string.notice))
                            setContent(getString(R.string.dialog_complete_exit))
                            setSure(getString(R.string.dialog_ok))
                            setCancel(getString(R.string.dialog_cancel))
                            setSureListener {
                                finish()
                            }
                            setCancelListener {
                                cancel()
                            }
                        }.show()
                    } else {
                        finish()
                    }
                } else if (data[0].startsWith("O")) {
                    if (mInstDataOut.SCAN_QTY < mInstDataOut.PKG_QTY) {
                        RxDialogSureCancel(this).apply {
                            setTitle(getString(R.string.notice))
                            setContent(getString(R.string.dialog_complete_exit))
                            setSure(getString(R.string.dialog_ok))
                            setCancel(getString(R.string.dialog_cancel))
                            setSureListener {
                                finish()
                            }
                            setCancelListener {
                                cancel()
                            }
                        }.show()
                    } else {
                        finish()
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}