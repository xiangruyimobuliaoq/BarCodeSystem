package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityBoxPreviewBinding
import com.isl.bcs.model.BoxIn
import com.isl.bcs.model.BoxOut
import com.isl.bcs.model.InstTrxIn
import com.isl.bcs.model.InstTrxOut
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
                tvLabel.text = boxData
                tvStaff.text = Constants.currentStaff?.name
                tvItem.text = it[5]
                tvName.text = it[6]
                tvWhLoc1.text = it[10]
                tvWhLoc2.text = it[11]
                tvDateTime.text = SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss",
                    Locale.CHINA
                ).format(System.currentTimeMillis())
                if (it[0].startsWith("I")) ivFlag.setImageResource(R.mipmap.box_details_icon_in) else ivFlag.setImageResource(
                    R.mipmap.box_details_icon_out
                )
            }

            btnOk.setOnClickListener {
                if (instData[0].startsWith("O")) {
                    val box = BoxOut(
                        intent.getIntExtra("scanKey", 0),
                        instData[0],
                        boxData,
                        rgCondition.checkedRadioButtonId == R.id.rb_good,
                        true,
                        Constants.currentStaff!!.id_no,
                        tvDateTime.text.toString(),
                        tvDateTime.text.toString()
                    )
                    box.save()
                    val result = LitePal.where("INST_OUT_KEY = ?", instData[0]).find<InstTrxOut>()
                    if (result.isNotEmpty()) {
                        result[0].SCAN_QTY = result[0].SCAN_QTY + 1
                        if (result[0].SCAN_QTY == result[0].PKG_QTY) {
                            result[0].INST_CLOSE_FLAG = true
                        }
                        result[0].save()
                    }
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
                    val result = LitePal.where("INST_IN_KEY = ?", instData[0]).find<InstTrxIn>()
                    if (result.isNotEmpty()) {
                        result[0].SCAN_QTY = result[0].SCAN_QTY + 1
                        if (result[0].SCAN_QTY == result[0].PKG_QTY) {
                            result[0].INST_CLOSE_FLAG = true
                        }
                        result[0].save()
                    }
                }
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}