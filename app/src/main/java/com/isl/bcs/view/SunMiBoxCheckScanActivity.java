package com.isl.bcs.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drake.tooltip.ToastKt;
import com.isl.bcs.R;
import com.isl.bcs.base.BaseActivity;
import com.isl.bcs.model.InstItemCheck;
import com.isl.bcs.model.Staff;
import com.isl.bcs.utils.Constants;
import com.isl.bcs.utils.SoundUtils;
import com.sunmi.scan.Config;
import com.sunmi.scan.Image;
import com.sunmi.scan.ImageScanner;
import com.sunmi.scan.Symbol;
import com.sunmi.scan.SymbolSet;
import com.tamsiree.rxkit.RxAnimationTool;
import com.tamsiree.rxui.view.dialog.RxDialogSure;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class SunMiBoxCheckScanActivity extends BaseActivity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private SurfaceView surface_view;
    private ImageScanner scanner;
    private Handler autoFocusHandler;
    SoundUtils soundUtils;
    public boolean use_auto_focus = true;
    public int decode_count = 0;
    public static int previewSize_width = 640;
    public static int previewSize_height = 480;
    public boolean data_init = false;
    Image imgae_data;// = new Image(previewSize_width,previewSize_height, "Y800");
    StringBuilder sb = new StringBuilder();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(AppCompatActivity activity) {
        List<String> mPremissionList = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            int isGranted = ActivityCompat.checkSelfPermission(activity, permission);
            if (isGranted != PackageManager.PERMISSION_GRANTED) {
                mPremissionList.add(permission);
            }
        }
        if (mPremissionList.size() > 0) {
            String[] strings = new String[]{mPremissionList.get(0)};
            ActivityCompat.requestPermissions(activity, strings, REQUEST_EXTERNAL_STORAGE);
            return;
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(this);
        } else {
            Toast.makeText(getApplicationContext(), "需开启相机、存储读写相关权限", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunmi_box_scan);
        setImmersive(false);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.tv_title)).setText(getString(R.string.box_label_scan));
        RxAnimationTool.ScaleUpDowm(findViewById(R.id.capture_scan_line));
        ((ImageView) findViewById(R.id.imageView5)).setImageResource(R.mipmap.home_icon_3);
        init();
    }

    public void openCamera() {
        try {
            mCamera = Camera.open();
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                for (Camera.Size size : supportedPreviewSizes) {
                    if (size.width == 1280 && size.height == 720) {
                        previewSize_width = 1280;
                        previewSize_height = 720;
                        break;
                    } else if (size.width == 800 && size.height == 480) {
                        previewSize_width = 800;
                        previewSize_height = 480;
                        break;
                    }
                }
                Log.d("DBG", "previewSize_width=" + previewSize_width + ",previewSize_height=" + previewSize_height);

                if (!data_init) {
                    imgae_data = new Image(previewSize_width, previewSize_height, "Y800");
                    data_init = true;
                }
                parameters.setPreviewSize(previewSize_width, previewSize_height);

                List<String> focusModes = parameters.getSupportedFocusModes();
                Log.d("DBG", "Supported Focus Modes: " + focusModes);
                if (((List) focusModes).contains(parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                    use_auto_focus = true;
                else
                    use_auto_focus = false;
                Log.d("DBG", "use_auto_focus: " + use_auto_focus);
                if (use_auto_focus)
                    parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);//手持机使用，竖屏显示,T1/T2 mini需要屏蔽掉
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            Log.d("DBG", "open camera failed!");
            e.printStackTrace();
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };
    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            //Log.d("AutoFocusCallback", "success value: "+success);
            autoFocusHandler.postDelayed(doAutoFocus, 100);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            verifyStoragePermissions(SunMiBoxCheckScanActivity.this);
        } catch (Exception e) {
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (soundUtils != null) {
            soundUtils.release();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initBeepSound() {
        if (soundUtils == null) {
            soundUtils = new SoundUtils(this, SoundUtils.RING_SOUND);
            soundUtils.putSound(0, R.raw.beep);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initBeepSound();
        openCamera();
    }

    private void playBeepSoundAndVibrate() {
        if (soundUtils != null) {
            soundUtils.playSound(0, SoundUtils.SINGLE_PLAY);
        }
    }

    private void init() {
        surface_view = (SurfaceView) findViewById(R.id.surface_view);
        mHolder = surface_view.getHolder();
        mHolder.addCallback(this);
        scanner = new ImageScanner();//创建扫描器
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);//允许识读QR码，默认1:允许
        scanner.setConfig(Symbol.PDF417, Config.ENABLE, 1);//允许识读PDF417码，默认0：禁止
        scanner.setConfig(Symbol.DataMatrix, Config.ENABLE, 1);//允许识读DataMatrix码，默认0：禁止
        scanner.setConfig(Symbol.AZTEC, Config.ENABLE, 1);//允许识读AZTEC码，默认0：禁止
//		scanner.setConfig(Symbol.I25, Config.ENABLE, 1);
        //scanner.setConfig(Symbol.NONE, Config.ENABLE_ECI_MODE, 0);
        if (use_auto_focus)
            autoFocusHandler = new Handler();
        decode_count = 0;
        findViewById(R.id.btn_exit).setOnClickListener(view -> finish());
    }


    PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            imgae_data.setData(data);
            //解码，返回值为0代表失败，>0表示成功
            int nsyms = scanner.scanImage(imgae_data);
            if (nsyms != 0) {
                mCamera.stopPreview();
                playBeepSoundAndVibrate();//解码成功播放提示音
                SymbolSet syms = scanner.getResults();//获取解码结果
                for (Symbol sym : syms)//如果允许识读多个条码，则解码结果可能不止一个
                {
                    Log.e("123", sym.getResult());
                    List<InstItemCheck> staff = LitePal.where("BOX_LABEL1 = ? ", sym.getResult()).find(InstItemCheck.class);
                    if (staff.size() > 0) {
                        Intent intent = new Intent(SunMiBoxCheckScanActivity.this, BoxCheckPreviewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("boxData", staff.get(0));
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        RxDialogSure rxDialogSure = new RxDialogSure(SunMiBoxCheckScanActivity.this);
                        rxDialogSure.setContent(getString(R.string.dialog_content));
                        rxDialogSure.setSure(getString(R.string.dialog_ok));
                        rxDialogSure.setSureListener(view -> {
                            rxDialogSure.cancel();
                            openCamera();
                        });
                        rxDialogSure.show();
                    }
                }
            }
        }
    };
}