package com.ctftek.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ctftek.player.banner.Banner;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String FILE_NAME = "mediaResource";
    private String mediaPath = "";
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
        setContentView(R.layout.activity_main);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
        initFile();
        initReceiver();
        initDate();
        initView();
//        isContainResource("/mnt/usb_storage/USB_DISK2");//for test
//        updateFileData();
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
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "data: " + files[i].getAbsolutePath());
            fileList.add(files[i].getAbsolutePath());
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

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);  //接受外媒挂载过滤器
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);  //接受外媒挂载过滤器    
        filter.addDataScheme("file");
        registerReceiver(mSdcardReceiver, filter, "android.permission.READ_EXTERNAL_STORAGE", null);
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
                File[] f = files[i].listFiles();
                for (int j = 0; j < f.length; j++) {
                    Log.d(TAG, "son files : " + f[j].getAbsolutePath());
                    if (f[j].getAbsolutePath().contains(FILE_NAME)) {
                        mediaPath = f[j].getAbsolutePath();
                        Log.d(TAG, "mediaPath: " + mediaPath);
                        if (!isEmptyFolder(mediaPath)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    BroadcastReceiver mSdcardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                String path = intent.getDataString().substring(8);
                Log.e(TAG, "onReceive: 插入优盘:" + path);
                if (isContainResource(path)) {
                    Utils.deleteFiles(Utils.filePath);
                    Utils.copyFolder(mediaPath, Utils.filePath);
                    Toast.makeText(MainActivity.this, "文件复制完成！", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
                Log.e(TAG, "onReceive: 移除优盘");
            }
        }
    };

}