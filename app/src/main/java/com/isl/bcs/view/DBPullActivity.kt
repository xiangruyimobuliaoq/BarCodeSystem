package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityDbpullBinding
import com.isl.bcs.model.*
import com.isl.bcs.utils.Constants
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.saveAll

class DBPullActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityDbpullBinding.inflate(layoutInflater)
        val type = intent.getStringExtra(Constants.INTENT_TYPE)
        with(mBinding) {
            setContentView(root)
            immersive(toolbar.toolbar, false)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.download)
            when (type) {
                Constants.TYPE_CHECK -> ivIntentTag.setImageResource(R.mipmap.home_icon_3)
                Constants.TYPE_INOUT -> ivIntentTag.setImageResource(R.mipmap.home_icon_1)
                Constants.TYPE_TRANSFER -> ivIntentTag.setImageResource(R.mipmap.home_icon_2)
            }
            btnDownload.setOnClickListener {
                btnDownload.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                scopeNetLife(dispatcher = Dispatchers.IO) {
                    val staffList = Get<List<Staff>?>("/master/staff")
                    val whList = Get<List<Warehouse>?>("/master/warehouse")
                    val instItemList = Get<List<InstItemOut>?>("/scan/stock")
                    val instItemCheck = Get<List<InstItemCheck>?>("/scan/stock_check")
                    LitePal.deleteAll<BoxIn>()
                    LitePal.deleteAll<BoxOut>()
                    LitePal.deleteAll<BoxTransfer>()
                    LitePal.deleteAll<InstTrxIn>()
                    LitePal.deleteAll<InstTrxOut>()
                    LitePal.deleteAll<Staff>()
                    LitePal.deleteAll<Warehouse>()
                    LitePal.deleteAll<InstItemOut>()
                    LitePal.deleteAll<InstItemCheck>()
                    staffList.await()?.saveAll()
                    whList.await()?.saveAll()
                    instItemList.await()?.saveAll()
                    instItemCheck.await()?.saveAll()
                    withMain {
                        btnNext.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.complete)
                        progressBar.isIndeterminate = false
                        progressBar.progress = 100
                    }
                }
            }
            btnNext.setOnClickListener {
                openActivity<SunMiStaffScanActivity>(
                    Constants.INTENT_TYPE to type
                )
            }
        }
    }
}