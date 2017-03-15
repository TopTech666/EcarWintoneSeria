package com.ecaray.wintonlib;/*
 *===============================================
 *
 * 文件名:${type_name}
 *
 * 描述: 序列号验证     x
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
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.ecaray.wintonlib.util.SPKeyUtils;
import com.ecaray.wintonlib.util.SPUtils;
import com.wintone.lisence.WintoneLSCOperateTools;
import com.wintone.plateid.AuthService;
import com.wintone.plateid.PlateAuthParameter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AuthHelper {
    private static AuthHelper authHelper;
    private Activity context;
    private String seriaNumber = "";


    public ServiceConnection authConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            authConn = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AuthService.MyBinder authBinder = (AuthService.MyBinder) service;
            PlateAuthParameter pap = new PlateAuthParameter();
            int nNet = 1;
            try {
                pap.sn = seriaNumber;
                pap.authFile = "";
                pap.devCode = "";
                nNet = authBinder.getAuth(pap);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (authBinder != null) {
                    context.unbindService(authConn);
                    if (nNet == 0) {     //0代表授权成功
                        SPKeyUtils.saveSeriaNum(context, seriaNumber);
                    } else {
                        SPUtils.clear(context);
                    }
                }
            }

        }
    };

    private AuthHelper() {
    }


    static public AuthHelper getInstance() {
        if (authHelper == null) {
            authHelper = new AuthHelper();
        }
        return authHelper;
    }


    /****************************************
     方法描述：授权
     @param    seriaNumber
     @return
     ****************************************/
    public void bindAuthService(Activity context, String seriaNumber) {
        //设置sd卡路径
        if (TextUtils.isEmpty(WintoneLSCOperateTools.PATH)) {
            WintoneLSCOperateTools.PATH = getSdPatch(context);
        }
        this.seriaNumber = seriaNumber;
        this.context = context;
        if (!TextUtils.isEmpty(seriaNumber)) {
            Intent authIntent = new Intent(context, AuthService.class);
            context.bindService(authIntent, authConn, Service.BIND_AUTO_CREATE);
        }
    }

    public static String getSdPatch(Activity activity) {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        } else {
            String patch = null;
            return TextUtils.isEmpty(patch = getCanUsePatch(activity)) ? activity.getCacheDir().getAbsolutePath() : patch;
        }
    }

    private static String getCanUsePatch(Activity activity) {
        StorageManager mStorageManager = (StorageManager) activity.getSystemService("storage");
        Method method = null;

        try {
            method = mStorageManager.getClass().getMethod("getVolumePaths", new Class[0]);
        } catch (NoSuchMethodException var8) {
            var8.printStackTrace();
        }

        String[] paths = null;

        try {
            paths = (String[]) ((String[]) method.invoke(mStorageManager, new Object[0]));
        } catch (IllegalArgumentException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        }

        if (paths != null && paths.length != 0) {
            for (int i = 0; i < paths.length; ++i) {
                if ((new File(paths[i])).canRead()) {
                    return paths[i];
                }
            }
            return "";
        } else {
            return "";
        }
    }

}
