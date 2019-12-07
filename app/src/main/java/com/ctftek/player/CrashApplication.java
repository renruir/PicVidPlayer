package com.ctftek.player;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
//        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
//        SharedPreferences.Editor ed = sp.edit();
//        ed.putBoolean("active", false);
//        ed.commit();
//        try{
//            Thread.sleep(1000);
//        }catch (InterruptedException e){
//            Log.e(TAG, "error : ", e);
//        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        finishActivity();
    }
    public void finishActivity(){
        for (Activity activity : mActivityList) {
            if (null != activity) {
                activity.finish();
            }
        }
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

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