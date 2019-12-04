package com.ctftek.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.ctftek.player.banner.Banner;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.io.File;
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
//    private NewBanner banner;
    private TextView mText;

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
        initView();
        initFile();
        initDate();

//        getPhysicalExternalFilePathAboveM();
//        isContainResource("/mnt/usb_storage/USB_DISK2");//for test
//        updateFileData();
        getSecondaryStoragePath();
    }

    public String getSecondaryStoragePath() {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            Log.d(TAG, "SecondaryStoragePathSize: " + paths.length);
            if(paths.length == 2){
                return paths[1];
            } else if(paths.length == 3){
                return paths[2];
            } else {
                return paths.length <= 1 ? null : paths[1];
            }

        } catch (Exception e) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e);
            return null;
        }
    }

    private void initView() {
        mText = (TextView)findViewById(R.id.msg_text);
        banner = (Banner) findViewById(R.id.banner);
    }

    private void initDate() {
        fileList = new ArrayList<>();
        File file = new File(Utils.filePath);
        File[] files = file.listFiles();
        Log.d(TAG, "initDate: " + files.length);
        if (files.length != 0) {
            banner.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "data: " + files[i].getAbsolutePath());
                fileList.add(files[i].getAbsolutePath());
            }
            banner.setDataList(fileList);
            banner.setImgDelyed(2000);
            banner.startBanner();
            banner.update();
            banner.startAutoPlay();
        } else {
            banner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
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