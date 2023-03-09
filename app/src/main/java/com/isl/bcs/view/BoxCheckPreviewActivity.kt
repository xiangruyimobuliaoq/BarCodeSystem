package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityCheckPreviewBinding
import com.isl.bcs.model.*
import com.isl.bcs.utils.Constants
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*

class BoxCheckPreviewActivity : BaseActivity() {

    //    private val vm: InOutViewModel by lazy {
//        ViewModelProvider(App.app, defaultViewModelProviderFactory).get(InOutViewModel::class.java)
//    }
    private lateinit var mBinding: ActivityCheckPreviewBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val boxData = intent.getSerializableExtra("boxData")!! as InstItemCheck
        mBinding = ActivityCheckPreviewBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.box_details)
            immersive(toolbar.toolbar, false)
            boxData.let { it ->
                tvLabel.text = it.BOX_LABEL1
                tvStaff.text = Constants.currentStaff?.name
                tvItem.text = it.ITEM_1
                tvName.text = it.DESCRIPTION_1
                tvWhLoc1.text = it.WH_LOC1
                tvWhLoc2.text = it.WH_LOC2
                tvDateTime.text = SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss",
                    Locale.CHINA
                ).format(System.currentTimeMillis())
                rgCondition.check(it.CHECKED_FLAG.let {
                    if (it) {
                        R.id.rb_good
                    } else {
                        R.id.rb_not_good
                    }
                })
            }

            btnOk.setOnClickListener {
//                val res =
//                    LitePal.where("SCAN_KEY=?", boxData.SCAN_KEY.toString()).find<BoxCheck>()
//                if (res.isEmpty()) {
                    BoxCheck(
                        boxData.SCAN_KEY,
                        boxData.ITEM_KEY,
                        boxData.ITEM_1,
                        rgCondition.checkedRadioButtonId == R.id.rb_good,
                        false,
                        tvDateTime.text.toString(),
                        Constants.currentStaff!!.id_no,
                        tvDateTime.text.toString()
                    ).save()
//                } else {
//                    res[0].apply {
//                        CHECKED_FLAG = true
//                        save()
//                    }
//                }
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}