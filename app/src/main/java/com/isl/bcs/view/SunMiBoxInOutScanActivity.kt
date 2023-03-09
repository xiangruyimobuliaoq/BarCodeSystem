package com.isl.bcs.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.drake.serialize.intent.openActivity
import com.drake.tooltip.toast
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.model.*
import com.isl.bcs.utils.SoundUtils
import com.sunmi.scan.Config
import com.sunmi.scan.Image
import com.sunmi.scan.ImageScanner
import com.sunmi.scan.Symbol
import com.tamsiree.rxkit.RxAnimationTool.ScaleUpDowm
import com.tamsiree.rxui.view.dialog.RxDialogSure
import org.litepal.LitePal
import org.litepal.extension.find

class SunMiBoxInOutScanActivity : BaseActivity(), SurfaceHolder.Callback {
    private var mCamera: Camera? = null
    private var mHolder: SurfaceHolder? = null
    private var surface_view: SurfaceView? = null
    private var scanner: ImageScanner? = null
    private var autoFocusHandler: Handler? = null
    var soundUtils: SoundUtils? = null
    var use_auto_focus = true
    var decode_count = 0
    var data_init = false
    var imgae_data // = new Image(previewSize_width,previewSize_height, "Y800");
            : Image? = null
    var sb = StringBuilder()
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun verifyStoragePermissions(activity: Activity?) {
        val mPremissionList: MutableList<String> = ArrayList()
        for (permission in PERMISSIONS) {
            val isGranted = ActivityCompat.checkSelfPermission(activity!!, permission)
            if (isGranted != PackageManager.PERMISSION_GRANTED) {
                mPremissionList.add(permission)
            }
        }
        if (mPremissionList.size > 0) {
            val strings = arrayOf(mPremissionList[0])
            ActivityCompat.requestPermissions(activity!!, strings, REQUEST_EXTERNAL_STORAGE)
            return
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(this)
        } else {
            Toast.makeText(applicationContext, "需开启相机、存储读写相关权限", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sunmi_box_scan)
        isImmersive = false
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.title = ""
        ScaleUpDowm(findViewById(R.id.capture_scan_line))
        (findViewById<View>(R.id.tv_title) as TextView).text = getString(R.string.box_label_scan)
        init()
    }

    fun openCamera() {
        try {
            mCamera = Camera.open()
            if (mCamera != null) {
                val parameters = mCamera!!.parameters
                val supportedPreviewSizes = parameters.supportedPreviewSizes
                for (size in supportedPreviewSizes) {
                    if (size.width == 1280 && size.height == 720) {
                        previewSize_width = 1280
                        previewSize_height = 720
                        break
                    } else if (size.width == 800 && size.height == 480) {
                        previewSize_width = 800
                        previewSize_height = 480
                        break
                    }
                }
                Log.d(
                    "DBG",
                    "previewSize_width=" + previewSize_width + ",previewSize_height=" + previewSize_height
                )
                if (!data_init) {
                    imgae_data = Image(previewSize_width, previewSize_height, "Y800")
                    data_init = true
                }
                parameters.setPreviewSize(previewSize_width, previewSize_height)
                val focusModes = parameters.supportedFocusModes
                Log.d("DBG", "Supported Focus Modes: $focusModes")
                use_auto_focus =
                    if ((focusModes as List<*>).contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) true else false
                Log.d("DBG", "use_auto_focus: $use_auto_focus")
                if (use_auto_focus) parameters.focusMode =
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                mCamera!!.parameters = parameters
                mCamera!!.setDisplayOrientation(90) //手持机使用，竖屏显示,T1/T2 mini需要屏蔽掉
                mCamera!!.setPreviewDisplay(mHolder)
                mCamera!!.setPreviewCallback(previewCallback)
                mCamera!!.startPreview()
            }
        } catch (e: Exception) {
            Log.d("DBG", "open camera failed!")
            e.printStackTrace()
        }
    }

    private val doAutoFocus = Runnable {
        if (null == mCamera || null == autoFocusCallback) {
            return@Runnable
        }
        mCamera!!.autoFocus(autoFocusCallback)
    }
    var autoFocusCallback: Camera.AutoFocusCallback? =
        Camera.AutoFocusCallback { success, camera -> //Log.d("AutoFocusCallback", "success value: "+success);
            autoFocusHandler!!.postDelayed(doAutoFocus, 100)
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            verifyStoragePermissions(this@SunMiBoxInOutScanActivity)
        } catch (e: Exception) {
            mCamera = null
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mHolder!!.surface == null) {
            return
        }
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(previewCallback)
            mCamera!!.startPreview()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (soundUtils != null) {
            soundUtils!!.release()
        }
        if (mCamera != null) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    private fun initBeepSound() {
        if (soundUtils == null) {
            soundUtils = SoundUtils(this, SoundUtils.RING_SOUND)
            soundUtils!!.putSound(0, R.raw.beep)
        }
    }

    override fun onResume() {
        // TODO Auto-generated method stub
        super.onResume()
        initBeepSound()
        openCamera()
    }

    private fun playBeepSoundAndVibrate() {
        if (soundUtils != null) {
            soundUtils!!.playSound(0, SoundUtils.SINGLE_PLAY)
        }
    }

    private fun init() {
        surface_view = findViewById<View>(R.id.surface_view) as SurfaceView
        mHolder = surface_view!!.holder
        mHolder!!.addCallback(this)
        scanner = ImageScanner() //创建扫描器
        scanner!!.setConfig(Symbol.QRCODE, Config.ENABLE, 1) //允许识读QR码，默认1:允许
        scanner!!.setConfig(Symbol.PDF417, Config.ENABLE, 1) //允许识读PDF417码，默认0：禁止
        scanner!!.setConfig(Symbol.DataMatrix, Config.ENABLE, 1) //允许识读DataMatrix码，默认0：禁止
        scanner!!.setConfig(Symbol.AZTEC, Config.ENABLE, 1) //允许识读AZTEC码，默认0：禁止
        //		scanner.setConfig(Symbol.I25, Config.ENABLE, 1);
        //scanner.setConfig(Symbol.NONE, Config.ENABLE_ECI_MODE, 0);
        if (use_auto_focus) autoFocusHandler = Handler()
        decode_count = 0
        val instData = intent.getStringExtra("instData")?.split(",")!!
        findViewById<View>(R.id.btn_start).setOnClickListener {
            if (instData[0].startsWith("O")) {
                val result = LitePal.where("INST_OUT_KEY = ?", instData[0]).find<InstTrxOut>()
                if (result.isNotEmpty()) {
                    if (result[0].SCAN_QTY >= result[0].PKG_QTY) {
                        RxDialogSure(this@SunMiBoxInOutScanActivity).apply {
                            setContent(getString(R.string.dialog_complete))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                                openActivity<InOutActivity>()
                            }
                        }.show()
                    } else {

                    }
                }
            } else if (instData[0].startsWith("I")) {
                val result = LitePal.where("INST_IN_KEY = ?", instData[0]).find<InstTrxIn>()
                if (result.isNotEmpty()) {
                    if (result[0].SCAN_QTY >= result[0].PKG_QTY) {
                        RxDialogSure(this@SunMiBoxInOutScanActivity).apply {
                            setContent(getString(R.string.dialog_complete))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                                openActivity<InOutActivity>()
                            }
                        }.show()
                    } else {
                    }
                }
            }
        }
        findViewById<View>(R.id.btn_exit).setOnClickListener {
            finish()
        }
    }

    var previewCallback = Camera.PreviewCallback { data, camera ->
        imgae_data!!.setData(data)
        //解码，返回值为0代表失败，>0表示成功
        val nsyms = scanner!!.scanImage(imgae_data)
        if (nsyms != 0) {
            mCamera?.stopPreview()
            playBeepSoundAndVibrate() //解码成功播放提示音
            val syms = scanner!!.results //获取解码结果
            for (sym in syms)  //如果允许识读多个条码，则解码结果可能不止一个
            {
                val res = sym.result
                Log.e("123", res)
                val instData = intent.getStringExtra("instData")?.split(",")!!
                if (instData[0].startsWith("O")) {
                    val boxOutRes = LitePal.where("BOX_LABEL1 = ?", res).find<BoxOut>()
                    if (boxOutRes.isNotEmpty()) {
                        RxDialogSure(this@SunMiBoxInOutScanActivity).apply {
                            setContent(getString(R.string.dialog_scanned))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                                openCamera()
                            }
                        }.show()
                    } else {
                        val boxInRes = LitePal.where("BOX_LABEL1 = ?", res).find<InstItemOut>()

                        if (boxInRes.isNotEmpty()) {
                            if (boxInRes[0].ITEM_1 != instData[5]) {
                                toast(getString(R.string.error_item))
                                return@PreviewCallback
                            }
                            openActivity<BoxInOutPreviewActivity>(
                                "boxData" to boxInRes[0].BOX_LABEL1,
                                "scanKey" to boxInRes[0].SCAN_KEY,
                                "instData" to intent.getStringExtra("instData")
                            )
                        } else {
                            RxDialogSure(this).apply {
                                setContent(getString(R.string.dialog_not_find))
                                setSure(getString(R.string.dialog_ok))
                                setSureListener {
                                    cancel()
                                    openCamera()
                                }
                            }.show()
                        }
                    }
                } else if (instData[0].startsWith("I")) {
                    val instItemOut = LitePal.where("BOX_LABEL1 = ?", res).find<InstItemOut>()
                    if (instItemOut.isNotEmpty()) {
                        RxDialogSure(this@SunMiBoxInOutScanActivity).apply {
                            setContent(getString(R.string.dialog_exist))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                                openCamera()
                            }
                        }.show()
                        break
                    }
                    val find = LitePal.where("BOX_LABEL1 = ?", res).find<BoxIn>()
                    if (find.isNotEmpty()) {
                        RxDialogSure(this@SunMiBoxInOutScanActivity).apply {
                            setContent(getString(R.string.dialog_scanned))
                            setSure(getString(R.string.dialog_ok))
                            setSureListener {
                                cancel()
                                openCamera()
                            }
                        }.show()
                    } else {
                        openActivity<BoxInOutPreviewActivity>(
                            "boxData" to res,
                            "instData" to intent.getStringExtra("instData")
                        )
                    }
                }
            }
        }
        sb.delete(0, sb.length)
    }

    companion object {
        var previewSize_width = 640
        var previewSize_height = 480
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }
}