package com.ctftek.player;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;

public class StorageService extends Service {

    private final static String TAG = StorageService.class.getName();

    private static final String FILE_NAME = "mediaResource";
    private String mediaPath = "";
    private Messenger mMessenger;
    private Handler mHandler = new Handler();
    private ServiceCallBack serviceCallBack;


    public class StorageServiceBinder extends Binder {
        public StorageService getService() {
            return StorageService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 000000");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        registerBroadCast();
        return new StorageServiceBinder();
    }


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ctftek service start: ");
        flags = START_STICKY;
        registerBroadCast();
//        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
//        boolean active = sp.getBoolean("active", true);
//        Log.d(TAG, "onStartCommand: " + active);
//        if (!active) {
//            Intent i = new Intent(getApplicationContext(), MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(i);
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void setCallback(ServiceCallBack callback) {
        serviceCallBack = callback;
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction("android.intent.action.MEDIA_REMOVED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intentFilter.addDataScheme("file");
        registerReceiver(stroageBroadcastReceiver, intentFilter);
    }

    BroadcastReceiver stroageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                if (intent.getData() == null) {
                    return;
                }
                Log.e(TAG, "插入存储设备：:" + intent.getData().getPath());
                String path = intent.getData().getPath();
                if (isContainResource(path)) {
                    long sdMediaSize = Utils.getFolderSize(new File(mediaPath));
                    long deviceStorageSize = Utils.getInternalMemorySize(context);
                    Log.d(TAG, "拷贝插入的容量媒体文件所需的容量为" + Formatter.formatFileSize(context, sdMediaSize) +
                            ", 而设备存储容量只有：" + Formatter.formatFileSize(context, deviceStorageSize));
                    if (sdMediaSize > deviceStorageSize) {
                        String info = "拷贝插入的容量媒体文件所需的容量为" + Formatter.formatFileSize(context, sdMediaSize) +
                                ", 而设备存储容量只有：" + Formatter.formatFileSize(context, deviceStorageSize) + "!";
                        serviceCallBack.infoCallBack(info);
                        return;
                    }
                    serviceCallBack.updateMediaFile(mediaPath);
                    Log.d(TAG, "onCreate size: " + Utils.getFolderSize(new File(mediaPath)));
                    Utils.deleteFiles(Utils.filePath);
                    Log.d(TAG, "onReceive: 文件复制完成！");
                }
            } else  {
                Log.e(TAG, "移除存储设备");
            }
        }
    };

    private boolean isContainResource(String path) {
        File file = new File(path);
        Log.d(TAG, "isContainResource: " + file.getAbsolutePath());
        File[] files = file.listFiles();
        if (files == null) {
            Log.e(TAG, "空目录");
            return false;
        } else {
            for (int i = 0; i < files.length; i++) {
                Log.e(TAG, "存储目录有：" + files[i].getAbsolutePath());
                if (files[i].getAbsolutePath().contains(FILE_NAME)) {
                    mediaPath = files[i].getAbsolutePath();
                    Log.d(TAG, "mediaPath: " + mediaPath);
                    if (!isEmptyFolder(mediaPath)) {
                        return true;
                    }
                }else if(files[i].getAbsolutePath().contains("USB_DISK")){
                    Log.d(TAG, "isContainResource: A usb disk");
                    mediaPath = files[i].getAbsolutePath();
                    File[] fs = new File(mediaPath).listFiles();
                    for(int j = 0; j < fs.length; j++){
                        if(fs[j].getAbsolutePath().contains(FILE_NAME)){
                            mediaPath = fs[j].getAbsolutePath();
                            if (!isEmptyFolder(mediaPath)) {
                                return true;
                            }
                        }
                    }
                    Log.d(TAG, "mediaPath: " + mediaPath);
                    if (!isEmptyFolder(mediaPath)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isEmptyFolder(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return true;
        } else {
            for (int i = 0; i < files.length; i++) {
                String filePath = files[i].getAbsolutePath();
                if (filePath.endsWith("jpg") || filePath.endsWith("png") || filePath.endsWith("gif")
                        || filePath.endsWith("mp4") || filePath.endsWith("mkv") || filePath.endsWith("avi")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendBroadCast2Activity() {
        Log.d(TAG, "sendBroadCast2Activity: 55555555555555");
        Intent intent = new Intent();
        intent.setAction("com.ctftek.storagestate.change");
//        intent.setComponent(new ComponentName("com.ctftek.player", "com.ctftek.player.MainActivity.MyBroadcastReceiver"));
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stroageBroadcastReceiver);
    }
}
