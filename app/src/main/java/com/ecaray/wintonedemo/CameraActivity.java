package com.ecaray.wintonedemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ecaray.wintonlib.WintonRecogManager;
import com.ecaray.wintonlib.helper.RecogniteHelper4WT;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * ===============================================
 * <p>
 * 类描述:
 * <p>
 * 创建人: Eric_Huang
 * <p>
 * 创建时间: 2016/9/23 15:53
 * <p>
 * 修改人:Eric_Huang
 * <p>
 * 修改时间: 2016/9/23 15:53
 * <p>
 * 修改备注:
 * <p>
 * ===============================================
 */
public class CameraActivity extends Activity implements Camera.PreviewCallback {

    @Bind(R.id.sv_camera)
    SurfaceView sv_camera;


    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;

    //0代表前置摄像头，1代表后置摄像头
    private int mCameraPosition = 1;
    //车牌号码
    private String mCarPlate;
    //识别帮助类
    public WintonRecogManager wtManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        ButterKnife.bind(this);
        this.initData();
        this.initView();
    }


    protected void initData() {
        mSurfaceHolder = sv_camera.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(mCallback);
    }

    public void initView() {

        sv_camera.setFocusable(true);
        sv_camera.setBackgroundColor(Color.TRANSPARENT);
    }


    SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            //当surfaceView关闭时，关闭预览并释放资源
            new Thread(new Runnable() {
                @Override
                public void run() {
                    releaseResource();
                }
            }).start();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (null == mCamera) {
                //文通识别服务绑定
                wtManager = WintonRecogManager.getInstance();
                wtManager.bind(CameraActivity.this);
                initCamera(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            initCamera(holder);
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        //判断是否为拍照，IsAction为true则说明是拍照，则停止预览
        wtManager.useWTRecognitionByData(this, data, new Geted(), mPreWidth, mPreHeight);
    }


    /**
     * 相机参数的初始化设置
     */
    private void initCamera(SurfaceHolder mSurfaceHolder) {
        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mParameters = mCamera.getParameters();
        mParameters.setPictureFormat(ImageFormat.JPEG);
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        setCameraSize(mParameters);
//        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//1连续对焦
//        // 设置JPG照片的质量
        mParameters.setJpegQuality(85);

        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(mCamera, 90);
        } else {
            mParameters.setRotation(90);
        }
        mCamera.setParameters(mParameters);
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();

    }


    /**
     * 实现的图像的正确显示
     */
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    public void releaseResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mSurfaceHolder.removeCallback(mCallback);
            mSurfaceHolder = null;
        }
    }


    /**
     * 获取车牌号后的回调类
     */
    class Geted implements RecogniteHelper4WT.OnResult {

        @Override
        public void onGeted(String fileName, String carPlate) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            Log.i("camera", "fileName=" + fileName + "\n" + "number=" + carPlate);
            mCarPlate = carPlate;
            Toast.makeText(CameraActivity.this, mCarPlate, Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void recogFail() {

        }

        @Override
        public String saveImage(byte[] data) {
            return "";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////设置预览区域////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isFatty;
    private int mPreWidth;
    private int mPreHeight;

    /**
     * 设置预览扫描区域尺寸
     */
    private void setCameraSize(Camera.Parameters parameters) {
        //获取手机支持分辨率
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size size;

        DisplayMetrics metric = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metric);

        int width = metric.widthPixels;
        int height = metric.heightPixels;
        if (width * 3 == height * 4) {
            isFatty = true;
        }

        int length = list.size();
        int previewWidth = 480;
        int previewHeight = 640;
        int second_previewWidth;
        int second_previewheight;
        if (length == 1) {
            //如果只有一种预览分辨率，则直接赋值
            size = list.get(0);
            previewWidth = size.width;
            previewHeight = size.height;

        } else {
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (isFatty) {
                    if (size.height <= 960 || size.width <= 1280) {

                        second_previewWidth = size.width;
                        second_previewheight = size.height;

                        previewWidth = second_previewWidth;
                        previewHeight = second_previewheight;
                    }
                } else {
                    if (size.height <= 960 || size.width <= 1280) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                        if (previewWidth <= second_previewWidth) {
                            previewWidth = second_previewWidth;
                            previewHeight = second_previewheight;
                        }
                    }
                }
            }
        }
        mPreWidth = previewWidth;
        mPreHeight = previewHeight;
        parameters.setPreviewSize(mPreWidth, mPreHeight);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wtManager.unBind(CameraActivity.this, true);
    }
}
