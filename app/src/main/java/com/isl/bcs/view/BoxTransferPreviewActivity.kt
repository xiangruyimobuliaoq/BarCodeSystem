package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityTransferPreviewBinding
import com.isl.bcs.model.*
import com.isl.bcs.utils.Constants
import com.tamsiree.rxkit.view.RxToast
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*

class BoxTransferPreviewActivity : BaseActivity() {

    //    private val vm: InOutViewModel by lazy {
//        ViewModelProvider(App.app, defaultViewModelProviderFactory).get(InOutViewModel::class.java)
//    }
    private lateinit var mBinding: ActivityTransferPreviewBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val boxData = intent.getSerializableExtra("boxData")!! as InstItemOut
        mBinding = ActivityTransferPreviewBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.box_details)
            immersive(toolbar.toolbar, false)
            boxData.let {
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
            }
            tvWhLoc3.setOnClickListener {
                initPopupView(tvWhLoc3, LitePal.where("type=?", "W").find())
            }
            tvWhLoc4.setOnClickListener {
                initPopupView(tvWhLoc4, LitePal.where("type=?", "L").find())
            }
            btnOk.setOnClickListener {
                if (tvWhLoc3.text.toString().isEmpty() || tvWhLoc4.text.toString().isEmpty()) {
                    RxToast.info("To LOC_1 and to LOC_2 should be selected.")
                    return@setOnClickListener
                }
                val res =
                    LitePal.where("SCAN_KEY=?", boxData.SCAN_KEY.toString()).find<BoxTransfer>()
                if (res.isEmpty()) {
                    BoxTransfer(
                        boxData.SCAN_KEY,
                        boxData.ITEM_1,
                        boxData.ITEM_KEY,
                        tvWhLoc3.text.toString(),
                        tvWhLoc4.text.toString(),
                        rgCondition.checkedRadioButtonId == R.id.rb_good,
                        Constants.currentStaff!!.id_no,
                        tvDateTime.text.toString()
                    ).save()
                } else {
                    res[0].apply {
                        WH_LOC1 = tvWhLoc3.text.toString()
                        WH_LOC2 = tvWhLoc4.text.toString()
                        save()
                    }
                }
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }

    private fun initPopupView(tv: TextView, items: List<Warehouse>) {
        PopupMenu(this, tv).apply {
            items.forEach { menu.add(it.wh_code) }
            setOnMenuItemClickListener {
                tv.text = it.title
                false
            }
            show()
        }
    }
}