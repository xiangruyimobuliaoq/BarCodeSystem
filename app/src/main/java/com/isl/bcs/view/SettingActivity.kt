package com.isl.bcs.view

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.isl.bcs.App
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivitySettingBinding
import com.isl.bcs.utils.Constants

class SettingActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivitySettingBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            immersive(toolbar.toolbar, false)
            toolbar.tvTitle.text = getString(R.string.settings)
            etEndPoint.setText(Constants.endPoint)
            etCompanyCode.setText(Constants.companyCode)
            btnSave.setOnClickListener {
                Constants.companyCode = etCompanyCode.text.toString().trim()
                Constants.endPoint = etEndPoint.text.toString().trim()
                App.app.initNet()
                toast("Save Successful!")
                finish()
            }
        }
    }
}