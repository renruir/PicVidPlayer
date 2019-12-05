package com.ctftek.player;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CrashApplication extends Application {
    private static final String TAG = CrashApplication.class.getName();
    private List<Activity> mActivityList;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CrashApplication onCreate: ");
        mActivityList = new ArrayList<>();
        Thread.setDefaultUncaughtExceptionHandler(restartHandler);
    }

    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            restartApp();
        }
    };

    public void restartApp() {
        Log.d(TAG, "restartApp: " + 1111111111);
        // fix our issues for sharedpreferences
        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        RxBus.getDefault().post(RxBusConstant.FINISH);//把你退出APP的代码放在这，我这里使用了rxbus关闭所有Activity
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    /**
     * 添加单个Activity
     */
    public void addActivity(Activity activity) {
        // 为了避免重复添加，需要判断当前集合是否满足不存在该Activity
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity); // 把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity(Activity activity) {
        // 判断当前集合是否存在该Activity
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity); // 从集合中移除
            if (activity != null){
                activity.finish(); // 销毁当前Activity
            }
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeAllActivity() {
        // 通过循环，把集合中的所有Activity销毁
        for (Activity activity : mActivityList) {
            if (activity != null){
                activity.finish();
            }
        }
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}