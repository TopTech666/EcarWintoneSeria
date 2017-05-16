package com.ecaray.wintonlib.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


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

    public static String getIMEI(Context context) {

        String IMEI = ((TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE)).getDeviceId();
        if (!TextUtils.isEmpty(IMEI)) {
            if (IMEI.length() < 15) {
                int length = 15 - IMEI.length();
                for (int i = 0; i < length; i++) {
                    IMEI = IMEI.concat("0");
                }
            }
            return IMEI;
        } else {
            return "";
        }
    }

    public static String getDeviceName() {
        String model = android.os.Build.MODEL;
        return model.toUpperCase();
    }

}
