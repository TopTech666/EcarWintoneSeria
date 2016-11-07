package com.ecaray.wintonedemo.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ecaray.wintonedemo.App;


/**
 * ===============================================
 * <p/>
 * 类描述:
 * <p/>
 * 创建人: Eric_Huang
 * <p/>
 * 创建时间: 2016/5/4 10:30
 * <p/>
 * 修改人:Eric_Huang
 * <p/>
 * 修改时间: 2016/5/4 10:30
 * <p/>
 * 修改备注:
 * <p/>
 * ===============================================
 */
public class SysServiceUtils {

    public static String getIMEI(){

        String IMEI = ((TelephonyManager) App.getInstance().getSystemService(
                Context.TELEPHONY_SERVICE)).getDeviceId();
        if(IMEI != null && !TextUtils.isEmpty(IMEI)){
            return IMEI;
        }else{
            return "";
        }
    }

    public static String getDeviceName(){
        String model= android.os.Build.MODEL;
        return model.toUpperCase();
    }

}
