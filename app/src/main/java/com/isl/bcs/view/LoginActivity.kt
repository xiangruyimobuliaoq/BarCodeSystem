package com.isl.bcs.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersiveRes
import com.drake.tooltip.toast
import com.isl.bcs.App
import com.isl.bcs.R
import com.isl.bcs.databinding.ActivityLoginBinding
import com.isl.bcs.model.UserInfo
import com.isl.bcs.utils.Constants
import org.json.JSONObject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LoginActivity : AppCompatActivity() {

    private val userId = "system"
    private val password = "f"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersiveRes(R.color.green)
        val mBinding = ActivityLoginBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(root)
            etUserId.setText(this@LoginActivity.userId)
            etPassword.setText(this@LoginActivity.password)
            btnSetting.setOnClickListener {
                openActivity<SettingActivity>()
            }
            btnLogin.setOnClickListener {
                when {
                    Constants.companyCode.isNullOrEmpty() ->
                        toast("You should input the company code in setting.")
                    Constants.endPoint.isNullOrEmpty() ->
                        toast("You should input the endpoint in setting.")
                    else ->
                        scopeDialog {
                            val resp = Post<UserInfo?>("/access/authenticate") {
                                json(
                                    JSONObject().put("username", etUserId.text.toString().trim())
                                        .put(
                                            "password",
                                            Constants.toMD5(etPassword.text.toString().trim())
                                        )
                                        .put("company_code", Constants.companyCode).toString()
                                )
                            }.await()
                            Log.e("123", resp.toString())
                            if (resp?.tokenID?.isNotEmpty() == true) {
                                Constants.token = resp?.tokenID
                                App.app.initNet()
                                openActivity<MainActivity>()
                                finish()
                            }
                        }
                }
            }
        }
    }
}