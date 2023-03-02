package com.isl.bcs.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.drake.serialize.intent.openActivity
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.isl.bcs.R
import com.isl.bcs.base.BaseActivity
import com.isl.bcs.databinding.ActivityStaffScanBinding
import com.isl.bcs.model.Staff
import com.isl.bcs.utils.Constants
import com.tamsiree.rxfeature.scaner.CameraManager
import com.tamsiree.rxkit.*
import org.litepal.LitePal
import org.litepal.extension.find
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class StaffScanActivity : BaseActivity() {

    private lateinit var mBinding: ActivityStaffScanBinding

    /**
     * 扫描处理
     */
    private var handler: StaffScanActivity.CaptureActivityHandler? = null


    /**
     * 扫描边界的宽度
     */
    private var mCropWidth = 0

    /**
     * 扫描边界的高度
     */
    private var mCropHeight = 0

    /**
     * 是否有预览
     */
    private var hasSurface = false

    /**
     * 扫描成功后是否震动
     */
    private val vibrate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityStaffScanBinding.inflate(layoutInflater)
        with(mBinding) {
            setContentView(this.root)
            immersive(toolbar.toolbar, false)
            setSupportActionBar(toolbar.toolbar)
            supportActionBar?.title = ""
            toolbar.tvTitle.text = getString(R.string.staff_scan)
            //界面控件初始化
            initDecode()
            //权限初始化
            initPermission()
            //扫描动画初始化
            initScanerAnimation()
            //初始化 CameraManager
            CameraManager.init(this@StaffScanActivity)
            hasSurface = false
        }
    }

    private fun initDecode() {
        multiFormatReader = MultiFormatReader()
        // 解码的参数
        val hints = Hashtable<DecodeHintType, Any?>(2)
        // 可以解析的编码类型
        var decodeFormats = Vector<BarcodeFormat?>()
        if (decodeFormats.isEmpty()) {
            decodeFormats = Vector()
            val PRODUCT_FORMATS = Vector<BarcodeFormat?>(5)
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_A)
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_E)
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_13)
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_8)
            // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
            val ONE_D_FORMATS = Vector<BarcodeFormat?>(PRODUCT_FORMATS.size + 4)
            ONE_D_FORMATS.addAll(PRODUCT_FORMATS)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_39)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_93)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_128)
            ONE_D_FORMATS.add(BarcodeFormat.ITF)
            val QR_CODE_FORMATS = Vector<BarcodeFormat?>(1)
            QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE)
            val DATA_MATRIX_FORMATS = Vector<BarcodeFormat?>(1)
            DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX)

            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(ONE_D_FORMATS)
            decodeFormats.addAll(QR_CODE_FORMATS)
            decodeFormats.addAll(DATA_MATRIX_FORMATS)
        }
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        multiFormatReader?.setHints(hints)
    }

    override fun onResume() {
        super.onResume()
        val surfaceHolder = mBinding.capturePreview.holder
        if (hasSurface) {
            //Camera初始化
            initCamera(surfaceHolder)
        } else {
            surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    if (!hasSurface) {
                        hasSurface = true
                        initCamera(holder)
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    hasSurface = false
                }
            })
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
    }

    override fun onPause() {
        super.onPause()
        if (handler != null) {
            handler?.quitSynchronously()
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
        CameraManager.get().closeDriver()
    }

    private fun initPermission() {
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }

    private fun initScanerAnimation() {
        RxAnimationTool.ScaleUpDowm(mBinding.captureScanLine)
    }

    private var cropWidth: Int
        get() = mCropWidth
        set(cropWidth) {
            mCropWidth = cropWidth
            CameraManager.FRAME_WIDTH = mCropWidth
        }

    private var cropHeight: Int
        get() = mCropHeight
        set(cropHeight) {
            mCropHeight = cropHeight
            CameraManager.FRAME_HEIGHT = mCropHeight
        }

    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder)
            val point = CameraManager.get().cameraResolution
            val width = AtomicInteger(point.y)
            val height = AtomicInteger(point.x)
            val cropWidth1 = mBinding.imageView.width * width.get() / mBinding.container.width
            val cropHeight1 = mBinding.imageView.height * height.get() / mBinding.container.height
            cropWidth = cropWidth1
            cropHeight = cropHeight1
        } catch (ioe: IOException) {
            return
        } catch (ioe: RuntimeException) {
            return
        }
        if (handler == null) {
            handler = CaptureActivityHandler()
        }
    }

    fun handleDecode(result: Result) {
        //扫描成功之后的振动与声音提示
        RxBeepTool.playBeep(this, vibrate)
        val result1 = result.text
//        val result1 = "A-001"
        Log.e("二维码/条形码 扫描结果", result1)
        val result = LitePal.where("id_no = ? ", result1).find<Staff>()
        if (result.isNotEmpty()) {
            Constants.currentStaff = result[0]
            when (intent.getStringExtra(Constants.INTENT_TYPE)) {
                Constants.TYPE_INOUT -> openActivity<InOutActivity>()
                Constants.TYPE_TRANSFER -> openActivity<TransferActivity>()
                Constants.TYPE_CHECK -> openActivity<CheckActivity>()
            }
        } else {
            toast(getString(R.string.staff_code_error))
        }
    }

    val autoFocus = 1
    val restartPreview = 2
    val decodeSucceeded = 3
    val decodeFailed = 4
    val decode = 5
    val quit = 6

    //==============================================================================================解析结果 及 后续处理 end
    @SuppressLint("HandlerLeak")
    internal inner class CaptureActivityHandler : Handler() {
        var decodeThread: DecodeThread? = null
        private var state: State
        override fun handleMessage(message: Message) {
            if (message.what == autoFocus) {
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, autoFocus)
                }
            } else if (message.what == restartPreview) {
                restartPreviewAndDecode()
            } else if (message.what == decodeSucceeded) {
                state = State.SUCCESS
                handleDecode(message.obj as Result) // 解析成功，回调
                handler!!.sendEmptyMessage(restartPreview)
            } else if (message.what == decodeFailed) {
                state = State.PREVIEW
                CameraManager.get().requestPreviewFrame(decodeThread!!.getHandler(), decode)
            }
        }

        fun quitSynchronously() {
            state = State.DONE
            decodeThread!!.interrupt()
            CameraManager.get().stopPreview()
            val quit = Message.obtain(decodeThread!!.getHandler(), quit)
            quit.sendToTarget()
            try {
                decodeThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                removeMessages(decodeSucceeded)
                removeMessages(decodeFailed)
                removeMessages(decode)
                removeMessages(autoFocus)
            }
        }

        private fun restartPreviewAndDecode() {
            if (state == State.SUCCESS) {
                state = State.PREVIEW
                CameraManager.get().requestPreviewFrame(decodeThread?.getHandler(), decode)
                CameraManager.get().requestAutoFocus(this, autoFocus)
            }
        }

        init {
            decodeThread = DecodeThread()
            decodeThread?.start()
            state = State.SUCCESS
            CameraManager.get().startPreview()
            restartPreviewAndDecode()
        }
    }

    internal inner class DecodeThread : Thread() {
        private val handlerInitLatch: CountDownLatch = CountDownLatch(1)
        private var handler: Handler? = null
        fun getHandler(): Handler? {
            try {
                handlerInitLatch.await()
            } catch (ie: InterruptedException) {
                // continue?
            }
            return handler
        }

        override fun run() {
            Looper.prepare()
            handler = DecodeHandler()
            handlerInitLatch.countDown()
            Looper.loop()
        }
    }

    @SuppressLint("HandlerLeak")
    internal inner class DecodeHandler : Handler() {
        override fun handleMessage(message: Message) {
            if (message.what == decode) {
                decode(message.obj as ByteArray, message.arg1, message.arg2)
            } else if (message.what == quit) {
                Looper.myLooper()?.quit()
            }
        }
    }

    private var multiFormatReader: MultiFormatReader? = null
    private fun decode(data: ByteArray, width: Int, height: Int) {
        var width1 = width
        var height1 = height
        var rawResult: Result? = null

        //modify here
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height1) {
            for (x in 0 until width1) {
                rotatedData[x * height1 + height1 - y - 1] = data[x + y * width1]
            }
        }
        // Here we are swapping, that's the difference to #11
        val tmp = width1
        width1 = height1
        height1 = tmp
        val source = CameraManager.get().buildLuminanceSource(rotatedData, width1, height1)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            if (bitmap.width > 0 && bitmap.height > 0) {
                rawResult = multiFormatReader?.decodeWithState(bitmap)
            } else {
                multiFormatReader?.reset()
            }
        } catch (e: ReaderException) {
            // continue
        } finally {
            multiFormatReader?.reset()
        }
        if (rawResult != null) {
            val message = Message.obtain(handler, decodeSucceeded, rawResult)
            val bundle = Bundle()
            bundle.putParcelable("barcode_bitmap", source.renderCroppedGreyscaleBitmap())
            message.data = bundle
            message.sendToTarget()
        } else {
            val message = Message.obtain(handler, decodeFailed)
            message.sendToTarget()
        }
    }

    private enum class State {
        //预览
        PREVIEW,  //成功
        SUCCESS,  //完成
        DONE
    }
}