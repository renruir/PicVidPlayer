package com.ctftek.player;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;

import com.ctftek.player.banner.Banner;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //view
    private Banner banner;

    //data
    private List<String> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
        Intent intent = new Intent(this, StorageService.class);
        startService(intent);

        setContentView(R.layout.activity_main);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
        initFile();
        initDate();
        initView();
        File[] files = getExternalFilesDirs(null);
        for (File file : files) {
            Log.e(TAG, "file: " + file.getAbsolutePath());
        }

//        getPhysicalExternalFilePathAboveM();
//        isContainResource("/mnt/usb_storage/USB_DISK2");//for test
//        updateFileData();
    }

    // 获取次存储卡路径,一般就是外置 TF 卡了. 不过也有可能是 USB OTG 设备...
    // 其实只要判断第二章卡在挂载状态,就可以用了.
    public String getSecondaryStoragePath() {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            Log.d(TAG, "getSecondaryStoragePath: " + paths.length);
            for (int i = 0; i < paths.length; i++) {
                Log.d(TAG, "getSecondaryStoragePath: " + paths[i]);
            }
            // second element in paths[] is secondary storage path
            return paths.length <= 1 ? null : paths[1];
        } catch (Exception e) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e);
        }
        return null;
    }

    private void initView() {
        banner = (Banner) findViewById(R.id.banner);
        banner.setDataList(fileList);
        banner.setImgDelyed(2000);
        banner.startBanner();
        banner.startAutoPlay();
    }

    private void initDate() {
        fileList = new ArrayList<>();
        File file = new File(Utils.filePath);
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "data: " + files[i].getAbsolutePath());
                fileList.add(files[i].getAbsolutePath());
            }
        }
    }

    private void initFile() {
        Utils.isExist(Utils.filePath);
    }

    private void initPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent.getData());
        initDate();
        initView();
    }

    private void updateFileData() {
        File file = new File(Utils.filePath);
        File[] files = file.listFiles();
//        List<SimpleBannerInfo> simpleBannerInfo = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String filePath = files[i].getAbsolutePath();
            if (filePath.endsWith("jpg") || filePath.endsWith("png") || filePath.endsWith("gif")) {
//                simpleBannerInfo.add(new MyImageInfo(filePath));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(MainActivity.this);
            normalDialog.setTitle("提示");
            normalDialog.setMessage("确定要退出播放吗？");
            normalDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            banner.stopPlay();
                            MainActivity.this.finish();
                        }
                    });
            normalDialog.setNegativeButton("关闭",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            normalDialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }


    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String getPhysicalExternalFilePathAboveM() {
        try {
            //===========================获取UserEnvironment================
            Class<?> userEnvironment = Class.forName("android.os.Environment$UserEnvironment");
            Method getExternalDirs = userEnvironment.getDeclaredMethod("getExternalDirs");
            getExternalDirs.setAccessible(true);
            //========获取构造UserEnvironment的必要参数UserId================
            Class<?> userHandle = Class.forName("android.os.UserHandle");
            Method myUserId = userHandle.getDeclaredMethod("myUserId");
            myUserId.setAccessible(true);
            int mUserId = (int) myUserId.invoke(UserHandle.class);
            Constructor<?> declaredConstructor = userEnvironment.getDeclaredConstructor(Integer.TYPE);
            // 得到UserEnvironment instance
            Object instance = declaredConstructor.newInstance(mUserId);
            File[] files = (File[]) getExternalDirs.invoke(instance);
            for (int i = 0; i < files.length; i++) {
                if (Environment.isExternalStorageRemovable(files[i])) {
                    Log.d(TAG, "getPhysicalExternalFilePathAboveM: " + files[i].getPath());
                    return files[i].getPath();
                }
            }
        } catch (Exception e) {
//            CrashHandler.getInstance().saveExceptionAsCrash(e);
        }
        return "";
    }


}