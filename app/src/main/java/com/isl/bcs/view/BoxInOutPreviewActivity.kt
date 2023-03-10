package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withIO
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityBoxPreviewBinding
import com.isl.bcs.model.*
import com.isl.bcs.utils.Constants
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*

class BoxInOutPreviewActivity : BaseActivity() {
//    private val vm: InOutViewModel by lazy {
//        ViewModelProvider(App.app, defaultViewModelProviderFactory).get(InOutViewModel::class.java)
//    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val instData = intent.getStringExtra("instData")?.split(",")!!
        val boxData = intent.getStringExtra("boxData")!!
        val mBinding = ActivityBoxPreviewBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.box_details)
            immersive(toolbar.toolbar, false)
            instData.let {
                if (instData[0].startsWith("O")) {
                    val instItemOut =
                        LitePal.where("BOX_LABEL1 = ?", boxData).find<InstItemOut>()[0]
                    tvQty.text = instItemOut.ITEM_QTY.toString()
                }
                tvLabel.text = boxData
                tvStaff.text = Constants.currentStaff?.name
                tvItem.text = it[5]
                tvName.text = it[6]
                tvWhLoc1.text = it[9]
                tvWhLoc2.text = it[10]
                tvDateTime.text = SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss",
                    Locale.CHINA
                ).format(System.currentTimeMillis())
                if (it[0].startsWith("I")) ivFlag.setImageResource(R.mipmap.box_details_icon_in) else ivFlag.setImageResource(
                    R.mipmap.box_details_icon_out
                )
            }

            btnOk.setOnClickListener {
                scopeLife {
                    withIO {
                        if (instData[0].startsWith("O")) {
                            val instItemOut =
                                LitePal.where("BOX_LABEL1 = ?", boxData).find<InstItemOut>()[0]
                            val box = BoxOut(
                                intent.getIntExtra("scanKey", 0),
                                instData[0],
                                boxData,
                                rgCondition.checkedRadioButtonId == R.id.rb_good,
                                true,
                                Constants.currentStaff!!.id_no,
                                tvDateTime.text.toString(),
                                tvDateTime.text.toString(),
                                instData[4].toInt(),
                                instData[5],
                                instData[9],
                                instData[10],
                                instData[6],
                                instItemOut.ITEM_QTY
                            )
                            box.save()
                            val result =
                                LitePal.where("INST_OUT_KEY = ?", instData[0]).find<InstTrxOut>()[0]
                            if (result.SCAN_QTY < result.PKG_QTY - 1 && result.PCS_QTY > result.SCAN_ITEM_QTY + instItemOut.ITEM_QTY) {
                                result.SCAN_QTY += 1
                                result.SCAN_ITEM_QTY += instItemOut.ITEM_QTY
                            } else if (result.SCAN_QTY == result.PKG_QTY - 1) {
                                if (result.PCS_QTY == result.SCAN_ITEM_QTY + instItemOut.ITEM_QTY) {
                                    result.SCAN_QTY += 1
                                    result.SCAN_ITEM_QTY += instItemOut.ITEM_QTY
                                    result.INST_CLOSE_FLAG = true
                                } else {
                                    toast(getString(R.string.error_item))
                                }
                            }
                            result.save()
                        } else if (instData[0].startsWith("I")) {
                            val box = BoxIn(
                                instData[0],
                                instData[4].toInt(),
                                boxData,
                                instData[3],
                                instData[5],
                                tvDateTime.text.toString(),
                                instData[9],
                                instData[10],
                                rgCondition.checkedRadioButtonId == R.id.rb_good,
                                Constants.currentStaff!!.id_no,
                                tvDateTime.text.toString(),
                                Constants.currentStaff!!.id_no,
                                tvDateTime.text.toString(),
                                instData[6]
                            )
                            box.save()
                            val result =
                                LitePal.where("INST_IN_KEY = ?", instData[0]).find<InstTrxIn>()[0]
                            result.SCAN_QTY = result.SCAN_QTY + 1
                            if (result.SCAN_QTY == result.PKG_QTY) {
                                result.INST_CLOSE_FLAG = true
                            }
                            result.save()
                        } else {
                        }
                    }
                    finish()
                }
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}