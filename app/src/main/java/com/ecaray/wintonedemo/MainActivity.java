package com.ecaray.wintonedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ecar.ecarnetwork.http.exception.InvalidException;
import com.ecaray.wintonedemo.camera.CameraActivity;
import com.ecaray.wintonedemo.entity.SerialBean;
import com.ecaray.wintonedemo.net.PubDataCenter;
import com.ecaray.wintonlib.AuthHelper;
import com.ecaray.wintonlib.util.SPKeyUtils;
import com.ecaray.wintonlib.util.SPUtils;
import com.wintone.lisence.WintoneLSCOperateTools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.ecaray.wintonlib.AuthHelper.getSdPatch;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.rec_car_plate)
    Button mRecCarPlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        ESubscriber<SerialBean> lESubscriber = new ESubscriber<SerialBean>(this) {
            @Override
            protected void onUserSuccess(SerialBean lSerialBean) {
            }

            @Override
            protected void onCheckNgisFailed(Context context, InvalidException commonException) {
//                super.onCheckNgisFailed(context, commonException);

                //此接口为蜜蜂接口，所以sign值配对不正确，需要重新拿到对象检查是否为空
                if (commonException.getResObj() != null) {
                    SerialBean lSerialBean = ((SerialBean) commonException.getResObj());
                    if (lSerialBean != null) {
                        String lSerialCode = lSerialBean.serialcode;
                        if (lSerialCode != null && !TextUtils.isEmpty(lSerialCode)) {
                            //获取序列号成功则保存
                            AuthHelper.getInstance().bindAuthService(MainActivity.this, lSerialBean.serialcode);
                        }
                    }
                }
            }
        };

        //检查是否有文通序列号,没有则回调请求
        SerialHelper4WT.getInstance(this).getSerialNum(new SerialI() {
            @Override
            public void getSerial() {
                Observable<SerialBean> lObservable = PubDataCenter.getInstance().getSerialNum();
                lObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(lESubscriber);
            }
        });

        mRecCarPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });

    }

}
