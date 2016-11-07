package com.ecaray.wintonedemo;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;

import com.ecar.ecarnetwork.http.api.ApiBox;
import com.ecaray.wintonedemo.entity.SerialBean;
import com.ecaray.wintonedemo.util.SysServiceUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import rx.Observable;

/**
 * ===============================================
 * <p/>
 * 类描述:
 * <p/>
 * 创建人: Eric_Huang
 * <p/>
 * 创建时间: 2016/8/26 11:41
 * <p/>
 * 修改人:Eric_Huang
 * <p/>
 * 修改时间: 2016/8/26 11:41
 * <p/>
 * 修改备注:
 * <p/>
 * ===============================================
 */
public class App extends Application {

    private static App mApp;
    private List<Activity> mActivityList = new LinkedList<>();
    private List<Activity> mPayActivityList = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        ApiBox.Builder builder = new ApiBox.Builder();
        //网络请求框架，param:1.是否需要答应log，2.传入KEY，3.传入APP_ID
        builder.application(this).debug(BuildConfig.DEBUG).reqKey("").build();
    }

    public static App getInstance() {
        return mApp;
    }
    public static Resources getAppResources() {
        return mApp.getResources();
    }

}
