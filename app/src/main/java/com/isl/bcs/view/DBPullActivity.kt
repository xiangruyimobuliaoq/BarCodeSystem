package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
                    Log.e("123","0")
                    val staffList = Get<List<Staff>?>("/master/staff").await()
                    val whList = Get<List<Warehouse>?>("/master/warehouse").await()
                    val instItemList = Get<List<InstItemOut>?>("/scan/stock").await()
                    val instItemCheck = Get<List<InstItemCheck>?>("/scan/stock_check").await()
                    val remain = Get<List<Remain>?>("/inst/remain").await()
                    LitePal.deleteAll<BoxIn>()
                    LitePal.deleteAll<BoxOut>()
                    LitePal.deleteAll<BoxTransfer>()
                    LitePal.deleteAll<InstTrxIn>()
                    LitePal.deleteAll<InstTrxOut>()
                    LitePal.deleteAll<Staff>()
                    LitePal.deleteAll<Warehouse>()
                    LitePal.deleteAll<InstItemOut>()
                    LitePal.deleteAll<InstItemCheck>()
                    LitePal.deleteAll<Remain>()
                    staffList?.saveAll()
                    whList?.saveAll()
                    instItemList?.saveAll()
                    instItemCheck?.saveAll()
                    remain?.saveAll()
                    withMain {
                        btnNext.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.complete)
                        progressBar.isIndeterminate = false
                        progressBar.progress = 100
                    }
                }
            }
            btnNext.setOnClickListener {
                openActivity<L2StaffScanActivity>(
                    Constants.INTENT_TYPE to type
                )
            }
        }
    }
}