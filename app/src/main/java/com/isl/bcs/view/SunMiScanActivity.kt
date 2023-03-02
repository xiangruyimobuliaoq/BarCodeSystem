package com.isl.bcs.view

import android.Manifest
import com.isl.bcs.base.BaseActivity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import com.isl.bcs.utils.SoundUtils
import android.app.Activity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.hardware.Camera
import android.widget.Toast
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.isl.bcs.R
import com.sunmi.scan.*
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList

class SunMiScanActivity : BaseActivity(), SurfaceHolder.Callback {
    private var mCamera: Camera? = null
    private var mHolder: SurfaceHolder? = null
    private var surface_view: SurfaceView? = null
    private var textview: TextView? = null
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
        setContentView(R.layout.activity_sunmiscan)
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
            verifyStoragePermissions(this@SunMiScanActivity)
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

    override fun onDestroy() {
        super.onDestroy()
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
    }

    var hasRead: Boolean = false
    var previewCallback = Camera.PreviewCallback { data, camera ->
        if (hasRead) return@PreviewCallback
        imgae_data!!.setData(data)
        val startTimeMillis = System.currentTimeMillis()
        //解码，返回值为0代表失败，>0表示成功
        val nsyms = scanner!!.scanImage(imgae_data)
        val endTimeMillis = System.currentTimeMillis()
        val cost_time = endTimeMillis - startTimeMillis
        sb.append("计数: " + decode_count++)
        sb.append("\n耗时: $cost_time ms\n")
        if (nsyms != 0) {
            hasRead = true
            playBeepSoundAndVibrate() //解码成功播放提示音
            val syms = scanner!!.results //获取解码结果
            for (sym in syms)  //如果允许识读多个条码，则解码结果可能不止一个
            {

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