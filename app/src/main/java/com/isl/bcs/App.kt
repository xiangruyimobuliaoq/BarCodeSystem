package com.isl.bcs

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.drake.net.NetConfig
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.interceptor.RequestInterceptor
import com.drake.net.okhttp.*
import com.drake.net.request.BaseRequest
import com.drake.tooltip.dialog.BubbleDialog
import com.isl.bcs.utils.Constants
import com.isl.bcs.utils.SerializationConverter
import com.tamsiree.rxkit.RxTool
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import org.litepal.LitePal
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier
import javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.TrustManager as TrustManager1

/**
 * 创建者     彭龙
 * 创建时间   2021/6/11 1:05 下午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class App : Application(), ViewModelStoreOwner {
    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }
    override fun onCreate() {
        super.onCreate()
        app = this
        initNet()
        LitePal.initialize(this)
        RxTool.init(this)
    }

    companion object {
        lateinit var app: App
    }

    fun initNet() {
        MMKV.initialize(this)
        Constants.endPoint?.let {
            // LogCat异常日志
            NetConfig // 添加日志记录器
                .init(it, fun OkHttpClient.Builder.() {
                    setLog(true) // LogCat异常日志
                    sslSocketFactory(createIgnoreVerifySSL("SSL"), object : X509TrustManager {
                        override fun checkClientTrusted(
                            p0: Array<out X509Certificate>?,
                            p1: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            p0: Array<out X509Certificate>?,
                            p1: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate?> {
                            return arrayOfNulls(0)
                        }

                    })
                    hostnameVerifier { _, _ -> true }
                    addInterceptor(LogRecordInterceptor(true)) // 添加日志记录器
                    setRequestInterceptor(object : RequestInterceptor {
                        override fun interceptor(request: BaseRequest) {
                            Constants.token?.let { it ->
                                request.setHeader("Authorization", it)
                            }
                        }
                    })
                    setConverter(SerializationConverter())
                    setDialogFactory { it1 ->
                        BubbleDialog(it1).apply {
                            title = "Loading..."
                        }
                    }
                })
        }
    }

    private fun createIgnoreVerifySSL(sslVersion: String): SSLSocketFactory {
        var sc = SSLContext.getInstance(sslVersion);
        val trustAllCerts: Array<TrustManager1> = arrayOf(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate>, authType: String
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        })
        sc!!.init(null, trustAllCerts, SecureRandom())

        return sc.socketFactory
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }
}