package com.ecaray.wintonlib.helper;/*
 *===============================================
 *
 * 文件名:${type_name}
 *
 * 描述: 识别服务
 *
 * 作者:
 *
 * 版权所有:深圳市亿车科技有限公司
 *
 * 创建日期: ${date} ${time}
 *
 * 修改人:   金征
 *
 * 修改时间:  ${date} ${time} 
 *
 * 修改备注: 
 *
 * 版本:      v1.0 
 *
 *===============================================
 */

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ecaray.wintonlib.R;
import com.wintone.plateid.PlateCfgParameter;
import com.wintone.plateid.RecogService;


public class RecogniteHelper4WT {
    private static RecogniteHelper4WT recogHelper;
    public RecogService.MyBinder recogBinder;
    public boolean mServiceIsConnected;
    private int iInitPlateIDSDK = -1;
    private int nRet = -1;
    public boolean isBinded;
    public long TIME_SPACING = 200;//时间间隔

    int[] fieldname = {R.string.plate_number, R.string.plate_color,
            R.string.plate_color_code, R.string.plate_type_code,
            R.string.plate_reliability, R.string.plate_brightness_reviews,
            R.string.plate_move_orientation, R.string.plate_leftupper_pointX,
            R.string.plate_leftupper_pointY, R.string.plate_rightdown_pointX,
            R.string.plate_rightdown_pointY, R.string.plate_elapsed_time,
            R.string.plate_light, R.string.plate_car_color};

    public ServiceConnection recogConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            recogConn = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceIsConnected = true;
            recogBinder = (RecogService.MyBinder) service;
            iInitPlateIDSDK = recogBinder.getInitPlateIDSDK();
            if (iInitPlateIDSDK != 0) {
                nRet = iInitPlateIDSDK;
                String[] str = {"" + iInitPlateIDSDK};
                getResult(null, str, null, null);
            }
            PlateCfgParameter cfgparameter = new PlateCfgParameter();
            cfgparameter.armpolice = 4;
            cfgparameter.armpolice2 = 16;
            cfgparameter.embassy = 12;
            cfgparameter.individual = 0;
            // cfgparameter.nContrast = 9;
            cfgparameter.nOCR_Th = 0;
            cfgparameter.nPlateLocate_Th = 5;
            cfgparameter.onlylocation = 15;
            cfgparameter.tworowyellow = 2;
            cfgparameter.tworowarmy = 6;
            cfgparameter.szProvince = "";
            cfgparameter.onlytworowyellow = 11;
            cfgparameter.tractor = 8;
            cfgparameter.bIsNight = 1;
            int lImageformat = 1;
            int lBVertFlip = 0;
            int lBDwordAligned = 1;
            recogBinder.setRecogArgu(cfgparameter, lImageformat, lBVertFlip, lBDwordAligned);
        }
    };

    private RecogniteHelper4WT() {
        mServiceIsConnected = false;

    }

    public boolean isBinded() {
        return isBinded;
    }

    /****************************************
     方法描述：
     @param  timeSpacing  获取车牌的时间间隔，默认200ms
     @return
     ****************************************/
    static public RecogniteHelper4WT getInstance(long timeSpacing) {
        if (recogHelper == null) {
            recogHelper = new RecogniteHelper4WT();
        }
        return recogHelper;
    }

    static public RecogniteHelper4WT getInstance() {
        if (recogHelper == null) {
            recogHelper = new RecogniteHelper4WT();
        }
        return recogHelper;
    }


    /**
     * 绑定服务，相机开启时绑定
     */
    public RecogniteHelper4WT bindRecogService(Activity activity) {
        if (!isBinded) {
            Intent authIntent = new Intent(activity, RecogService.class);
            activity.bindService(authIntent, recogConn, Service.BIND_AUTO_CREATE);
            isBinded = true;
        }
        return this;
    }

    /**
     * 解绑服务  必须退出页面的时候调用
     */
    public void unbindService(Activity activity) {
        if (recogBinder != null) {
            try {
                activity.unbindService(recogConn);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isBinded = false;

    }

    public int getInitPlateIDSDK() {
        return iInitPlateIDSDK;
    }

    /**
     * 判断服务是否已连接，返回true时才说明能够识别
     */
    public boolean isServiceIsConnected() {
        return mServiceIsConnected;
    }

    public RecogService.MyBinder getRecogBinder() {
        return recogBinder;
    }

    /**
     * 回调数据
     */
    long time;

    public synchronized void getResult(Activity activity, String[] fieldValue, byte[] data, OnResult onResult) {
        synchronized (this) {
            if (System.currentTimeMillis() - time < TIME_SPACING) {
                time = System.currentTimeMillis();
                return;
            }
            nRet = recogBinder.getnRet();
            if (nRet != 0) {
                feedbackWrongCode(activity);
            } else {

                String[] resultString;
                String boolString;
                boolString = fieldValue[0];

                String fileName = null;
                Log.i("carPlate", "识别到的车牌号：" + boolString);

                if (!TextUtils.isEmpty(boolString)) {
                    resultString = boolString.split(";");
                    int length = resultString.length;
                    if (length > 0) {
                        String[] strArray = fieldValue[4].split(";");
                        if (Integer.valueOf(strArray[0]) > 75) {
                            if (data != null) {
                                fileName = onResult.saveImage(data);
                            }
                            if (length == 1) {
                                if (null != fieldname) {
                                    String number = fieldValue[0];
                                    onResult.onGeted(fileName, number);
                                }
                            }
                        } else {
                            onResult.recogFail();
                        }
                    }
                } else {
                    onResult.recogFail();
                }
            }
            nRet = -1;
        }
    }

    private void feedbackWrongCode(Activity context) {
        if (context == null) {
            return;
        }
        String nretString = String.valueOf(nRet);
        switch (nretString) {
            case "-1001":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_readJPG_error),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10001":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_noInit_function),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10003":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_validation_faile),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10004":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_serial_number_null),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10005":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result)
                                + nRet
                                + "\n"
                                + context.getString(R.string.failed_disconnected_server),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10006":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result)
                                + nRet
                                + "\n"
                                + context.getString(R.string.failed_obtain_activation_code),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10007":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result)
                                + nRet
                                + "\n"
                                + context.getString(R.string.failed_noexist_serial_number),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10008":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_serial_number_used),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10009":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result)
                                + nRet
                                + "\n"
                                + context.getString(R.string.failed_unable_create_authfile),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10010":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result)
                                + nRet
                                + "\n"
                                + context.getString(R.string.failed_check_activation_code),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10011":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_other_errors),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10012":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_not_active),
                        Toast.LENGTH_SHORT).show();

                break;
            case "-10015":
                Toast.makeText(
                        context,
                        context.getString(R.string.recognize_result) + nRet + "\n"
                                + context.getString(R.string.failed_check_failure),
                        Toast.LENGTH_SHORT).show();

                break;
            default:
                showShort(context, context.getString(R.string.recognize_result) + nRet + "\n");
                break;
        }
    }

    public static boolean isShowMessage = true;

    /**
     * 短时间显示Toast
     */
    public static void showShort(Context context, String message) {
        if (isShowMessage)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public interface OnResult {
        void onGeted(String fileName, String number);  //获取了结果

        void recogFail();  //识别失败

        String saveImage(byte[] data);
    }
}
