package com.ctftek.player;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ctftek.player.banner.Banner;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements ServiceCallBack {
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //view
    private Banner banner;
    //    private NewBanner banner;
    private TextView mText;
    private StorageService.StorageServiceBinder serviceBinder;

    //data
    private List<String> fileList;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: from dialog");
            initDate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
        Intent intent = new Intent(this, StorageService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);

        initView();
        initFile();
        initDate();
        initPlayer();
        getSecondaryStoragePath();
        Log.d(TAG, "onCreate size: " + Utils.getInternalMemorySize(this));
    }

    private void initView() {
        mText = (TextView) findViewById(R.id.msg_text);
        banner = (Banner) findViewById(R.id.banner);
    }

    private void initFile() {
        Utils.isExist(Utils.filePath);
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
                String url = files[i].getAbsolutePath();
                if (Utils.getFileExtend(url).equals("mp4") || Utils.getFileExtend(url).equals("mkv") ||
                        Utils.getFileExtend(url).equals("avi") || Utils.getFileExtend(url).equals("ts") || Utils.getFileExtend(url).equals("mpg") ||
                        Utils.getFileExtend(url).equals("jpg") || Utils.getFileExtend(url).equals("png") || Utils.getFileExtend(url).equals("jpeg")) {
                    fileList.add(files[i].getAbsolutePath());
                }
            }
            try {
                banner.setDataList(fileList);
                banner.setImgDelyed(2000);
                banner.startBanner();
                banner.update();
                banner.startAutoPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            banner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        }
    }

    private void initPlayer() {
//        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
//        PlayerFactory.setPlayManager(SystemPlayerManager.class);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        CacheFactory.setCacheManager(ProxyCacheManager.class);
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
//        CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
        List<VideoOptionModel> list = new ArrayList<>();

        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 1);
        list.add(videoOptionModel);
        GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        GSYVideoType.enableMediaCodecTexture();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
    }

    public String getSecondaryStoragePath() {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            Log.d(TAG, "SecondaryStoragePathSize: " + paths.length);
            if (paths.length == 2) {
                return paths[1];
            } else if (paths.length == 3) {
                return paths[2];
            } else {
                return paths.length <= 1 ? null : paths[1];
            }

        } catch (Exception e) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e);
            return null;
        }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        Log.d(TAG, "Program exception, activity destroy ");
    }

    @Override
    public void updateMediaFile(String storagePath) {
        Log.d(TAG, "updateMediaFile:00000000000 ");
        try {
            CopyPasteUtil.build()
                    .setIsNeesDefaulProgressDialog(true)
                    .initValueAndGetDirSize(MainActivity.this, new File(storagePath), new CopyPasteUtil.InitListener() {
                        @Override
                        public void onNext(long dirFileCount, long dirSize, CopyPasteUtil.CopyPasteImp imp) {
                            int fileVolume = (int) (dirSize / (1024 * 1024));
                           Log.d(TAG, "onNext->dirFileCount:" + dirFileCount + "==onNext->dirSize:" + fileVolume + "M");

                            imp.copyDirectiory(MainActivity.this, storagePath, Utils.filePath, new CopyPasteUtil.CopyPasteListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "onSuccess: ");
                                    Message msg = Message.obtain();
                                    mHandler.sendMessage(msg);
                                    imp.getProgressDialog().dismiss();
                                }

                                @Override
                                public void onProgress(long dirFileCount, long hasReadCount, long dirSize, long hasReadSize) {
                                    Log.d(TAG, "onProgress: " + dirFileCount + "-" + hasReadCount + "==" + dirSize + "-" + hasReadSize);
                                }

                                @Override
                                public void onFail(String errorMsg) {
                                    Log.d(TAG, "onFail: ");
                                    try {
                                        MainActivity.this.finish();
                                        CrashApplication crashApplication = (CrashApplication) MainActivity.this.getApplication();
                                        crashApplication.restartApp();
                                        throw new Exception("文件拷贝异常");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onCancle() {
                                    Log.d(TAG, "onCancle: ");
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        banner.setVisibility(View.GONE);
        mText.setVisibility(View.VISIBLE);
//        banner.stopPlay();
//        banner.destroy();
    }

    @Override
    public void infoCallBack(String info) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("提示");
        normalDialog.setMessage(info);
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: 0000000000");
            serviceBinder = (StorageService.StorageServiceBinder) iBinder;
            serviceBinder.getService().setCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


}