package com.ctftek.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.ctftek.player.banner.Banner;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.io.File;
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
}