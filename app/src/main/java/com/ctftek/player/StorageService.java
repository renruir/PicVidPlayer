package com.ctftek.player;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class StorageService extends Service {

    private final static String TAG = StorageService.class.getName();

    private static final String FILE_NAME = "mediaResource";
    private String mediaPath = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        flags = START_STICKY;
        registerBroadCast();
        return super.onStartCommand(intent, flags, startId);
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
                    Utils.deleteFiles(Utils.filePath);
                    Utils.copyFolder(mediaPath, Utils.filePath);
                    Toast.makeText(context, "文件复制完成！", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) ||
                    intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
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

    /**
     * 6.0获取外置sdcard和U盘路径，并区分
     *
     * @param mContext
     * @param keyword  SD = "内部存储"; EXT = "SD卡"; USB = "U盘"
     * @return
     */
    public static String getStoragePath(Context mContext, String keyword) {
        String targetpath = "";
        StorageManager mStorageManager = (StorageManager) mContext
                .getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");

            Method getPath = storageVolumeClazz.getMethod("getPath");

            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);

            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");


            for (int i = 0; i < length; i++) {

                Object storageVolumeElement = Array.get(result, i);

                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);

                String path = (String) getPath.invoke(storageVolumeElement);

                if (userLabel.contains(keyword)) {
                    targetpath = path;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetpath;
    }

}
