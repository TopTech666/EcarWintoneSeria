package com.ecaray.wintonlib.net;

import android.text.TextUtils;
import android.util.Log;

import com.ecar.encryption.Epark.EparkEncrypUtil;
import com.ecar.factory.EncryptionUtilFactory;
import com.ecar.util.CastStringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeMap;

/*************************************
 功能：
 创建者： kim_tony
 创建日期：2017/5/16
 版权所有：深圳市亿车科技有限公司
 *************************************/

public class NetUtil {
    public final static String PARAMS_CLIENT_TYPE = "clientType";//

    public final static String PARAMS_ANDROID = "android";//

    public final static String PARAMS_COMID = "comid";//
    //后台解析版本
    public final static String VERSION_INFO = "ve";//

    public final static String VERSION_NAME = "2";//

    protected static final String comid = "000000001";
    public static final String REQUEST_KEY = "1000100 110011 110000 110010 111001 1000011 110111 110011 110100 110000 110110 110010 110010 110001 1000010 110000 110010 110000 110010 110110 1000010 110110 111000 110100 1000010 1000010 110000 110000 110101 110111 111001 1000011";


    public static EparkEncrypUtil eUtil = EncryptionUtilFactory.getDefault(true).createEpark();

    protected static String getSecurityMapKeys(TreeMap tMap, boolean encode, boolean isSign, boolean isNeedVe) {
        return eUtil.getSecurityMapKeys(tMap, encode, isSign, isNeedVe, "", eUtil.binstrToStr(REQUEST_KEY));
    }

    public static void getSerialNum(String imei, Request request) {
        TreeMap<String, String> tMap = new TreeMap<String, String>();
        tMap.put(PARAMS_CLIENT_TYPE, PARAMS_ANDROID);
        tMap.put(VERSION_INFO, VERSION_NAME);
        tMap.put(PARAMS_COMID, comid);
        tMap.put("devicecode", imei);
        tMap.put("method", "getSerialCode");
        tMap.put("module", "plo");
        tMap.put("service", "SerialCode");
        String urlStr = getSecurityMapKeys(tMap, false, true, true).toString();
        urlStr = urlStr.replaceAll("\\{", "");
        urlStr = urlStr.replaceAll("\\}", "");
        urlStr = urlStr.replaceAll(",", "&");
        urlStr = urlStr.replaceAll(" ", "");

        urlStr = "http://tra.parkbees.com/system/data?&".concat(urlStr);
        Log.d("tagutil", "urlStr: " + urlStr);
//        String urlStr = "http://tra.parkbees.com/system/data?&devicecode=" + imei +
//                "&method=getSerialCode&module=plo&service=SerialCode&sign=" +
//                "2b0411949511e2378f91c20bef33314c&ve=2";

        String result = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                byte[] data = new byte[1024];
                int len = is.read(data);
                result = new String(data, 0, len);
                Log.d("recogProxy", result);
                is.close();
                conn.disconnect();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.onResult(paserJson(result, "serialcode"));
    }

    public static String paserJson(String jsonStr, String key) {
        JSONObject jsonobj = null;
        try {
            jsonobj = new JSONObject(jsonStr);
            String serial = jsonobj.getString(key);
            return serial;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";

    }

    public interface Request {
        void onResult(String result);
    }
}
