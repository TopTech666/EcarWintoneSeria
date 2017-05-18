package com.ecaray.wintonlib;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.ecaray.wintonlib.helper.AuthHelper;
import com.ecaray.wintonlib.helper.RecogniteHelper4WT;
import com.ecaray.wintonlib.net.NetUtil;
import com.ecaray.wintonlib.util.SPKeyUtils;
import com.ecaray.wintonlib.util.SysServiceUtils;
import com.wintone.plateid.PlateRecognitionParameter;
import com.wintone.plateid.RecogService;


/*************************************
 功能：
 创建者： kim_tony
 创建日期：2017/5/16
 版权所有：深圳市亿车科技有限公司
 *************************************/

public class WintonRecogManager {

    static WintonRecogManager wintonHelper;
    static RecogniteHelper4WT mRecogHelper;

    private static boolean isStop;//是否停止识别  默认识别

    static long time;

    private static long RECOG_SPACING = 200;//识别过滤间隔  单位ms

    public void setRecogSpacing(long RECOG_SPACING) {
        this.RECOG_SPACING = RECOG_SPACING;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }


    //服务是否连接
    public boolean isServiceIsConnected() {
        return mRecogHelper.isServiceIsConnected();
    }

    private WintonRecogManager() {
        if (mRecogHelper == null) {
            mRecogHelper = RecogniteHelper4WT.getInstance();
        }
    }


    public static WintonRecogManager getInstance() {
        if (wintonHelper == null) {
            wintonHelper = new WintonRecogManager();
        }
        isStop = false;
        return wintonHelper;
    }


    public void authInit(final Activity context, boolean isNeedWintone) {
        if (!isNeedWintone) {
            return;
        } else {
            AuthHelper.seriaNumber = SPKeyUtils.getSeriaNum(context);
            if (!TextUtils.isEmpty(AuthHelper.seriaNumber)) {
                AuthHelper.getInstance().bindAuthService(context, AuthHelper.seriaNumber);
                return;
            }
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                NetUtil.getSerialNum(SysServiceUtils.getIMEI(context), new NetUtil.Request() {
                    @Override
                    public void onResult(String serial) {
                        AuthHelper.seriaNumber = serial;
                        if (!TextUtils.isEmpty(AuthHelper.seriaNumber)) {
                            AuthHelper.getInstance().bindAuthService(context, AuthHelper.seriaNumber);
                        } else {
                            Log.d("tagutil", "IMEI号服务器不存在 ");
                        }
                    }
                });

            }
        }).start();

    }


    public void bindRecogService(Activity activity) {
        mRecogHelper.bindDataRecogService(activity);
    }

    public void unBind(Activity activity, boolean isWintone) {
        isStop = true;
        if (isWintone)
            mRecogHelper.unbindService(activity);
    }


    /**
     * 文通识别
     *
     * @param data 字节数组(图片)
     */
    //识别帮助类
    public void useWTRecognitionByData(Activity activity,
                                       byte[] data,
                                       RecogniteHelper4WT.OnResult geted,
                                       int preWidth, int preHeight) {
        if ((System.currentTimeMillis() - time) < RECOG_SPACING || isStop) {
            geted.recogFail();
            return;
        }

        RecogService.initializeType = true;  // 记录进入此界面时是拍照识别还是视频识别   	 true:视频识别 		false:拍照识别
        time = System.currentTimeMillis();

        int nRet = -1;
        if (mRecogHelper.isServiceIsConnected() && data != null) {
            nRet = mRecogHelper.getRecogBinder() != null ? mRecogHelper.getRecogBinder().getnRet() : nRet;

            int initPlateIDSDK = mRecogHelper.getInitPlateIDSDK();
            if (initPlateIDSDK == 0) {
                //识别参数设置
                PlateRecognitionParameter mPlateRecParam = new PlateRecognitionParameter();

                //设置识别区域--三个参数的赋值顺序一定要保证！
                mPlateRecParam.height = preHeight;
                mPlateRecParam.width = preWidth;
                mPlateRecParam.picByte = data;

                //初始化参数
                mPlateRecParam.plateIDCfg.bRotate = 1;
                mPlateRecParam.plateIDCfg.left = 0;
                mPlateRecParam.plateIDCfg.right = 0;
                mPlateRecParam.plateIDCfg.top = 0;
                mPlateRecParam.plateIDCfg.bottom = 0;


                //识别开始
                RecogService.MyBinder lBinder = mRecogHelper.getRecogBinder();
                String[] mFieldValue = lBinder.doRecogDetail(mPlateRecParam);
                if (nRet != 0) {
                    String[] str = {"" + nRet};
                    mRecogHelper.getResult(activity, str, data, geted);
                } else {
                    mRecogHelper.getResult(activity, mFieldValue, data, geted);
                }
            }
        } else {
            geted.recogFail();
        }
    }

    /**
     * 文通识别
     *
     * @param path 图片路径
     */
    //识别帮助类
    public void useWTRecognitionByPic(Activity activity,
                                      String path,
                                      RecogniteHelper4WT.OnResult geted,
                                      int preWidth, int preHeight) {
        if (isStop) {
            return;
        }
        bindRecogService(activity);
        int nRet = -1;
        RecogService.initializeType = false;  // 记录进入此界面时是拍照识别还是视频识别   	 true:视频识别 		false:拍照识别

        if (mRecogHelper.isServiceIsConnected() && !TextUtils.isEmpty(path)) {
            nRet = mRecogHelper.getRecogBinder() != null ? mRecogHelper.getRecogBinder().getnRet() : nRet;

            int initPlateIDSDK = mRecogHelper.getInitPlateIDSDK();
            if (initPlateIDSDK == 0) {


                PlateRecognitionParameter prp = new PlateRecognitionParameter();
                prp.height = preHeight;// 图像高度
                prp.width = preWidth;// 图像宽度
                prp.pic = path;// 图像文件
                prp.devCode = "";


                //识别开始
                RecogService.MyBinder lBinder = mRecogHelper.getRecogBinder();
                String[] mFieldValue = lBinder.doRecogDetail(prp);
                nRet = lBinder.getnRet();
                System.out.println("图像宽高" + preHeight + "    " + preWidth);

                if (nRet != 0) {
                    String[] str = {"" + nRet};
                    mRecogHelper.getResult(activity, str, null, geted);
                } else {
                    mRecogHelper.getResult(activity, mFieldValue, null, geted);
                }
            }
        }
    }

}
