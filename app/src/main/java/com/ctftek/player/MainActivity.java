package com.ctftek.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ctftek.player.banner.MixBanner;
import com.ctftek.player.banner.VideoBanner;
import com.ctftek.player.sax.ParseXml;
import com.ctftek.player.video.CustomManager;
import com.ctftek.player.video.MultiSampleVideo;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements ServiceCallBack {
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //view
//    private MixBanner mixBanner1;
//    private MixBanner mixBanner2;
    private VideoBanner videoBanner;
    private TextView mText;
    private ImageView exitArea;
    private ImageView inputArea;
    private List<Banner> banners = new ArrayList<>();

    private ViewGroup mRootView;

    private StorageService.StorageServiceBinder serviceBinder;

    //data
    private List<String> fileList;
    private List<ParseXml.VideoInfo> videoInfoList;
    private List<String> videoFilelist = new ArrayList<>();
    private List<List<ParseXml.ImageInfo>> imageInfoList;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: from dialog");
//            initDate();
            try {
                initXmlData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!initLegalDevice()){
            finish();
            Toast.makeText(this, "不合法设备", Toast.LENGTH_SHORT).show();
            return;
        }
        initPermissions();
        Intent intent = new Intent(this, StorageService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);

//        Intent i = new Intent(this, TestActivity.class);
//        startActivity(i);

        initView();
        initFile();
//        initDate();
        try {
            initXmlData();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        initPlayer();
        getSecondaryStoragePath();
        Log.d(TAG, "onCreate size: " + Utils.getInternalMemorySize(this));
    }

    private boolean initLegalDevice() {
        File f = new File(Utils.legalPath);
        if (!f.exists()) {
            return false;
        } else {
            return true;
        }
    }

    private void initView() {
        mRootView = findViewById(android.R.id.content);
        mText = (TextView) findViewById(R.id.msg_text);
//        banner = (Banner) findViewById(R.id.banner);
        exitArea = (ImageView) findViewById(R.id.exit_area);
        inputArea = (ImageView) findViewById(R.id.input_password);
//        mixBanner1 = new MixBanner(this);

//        imageBanner = new Banner(this);
//        mRootView.addView(imageBanner);
//
//        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        params1.width = 960;
//        params1.height = 1080;
//        imageBanner.setLayoutParams(params1);
//        imageBanner.setX(0);
//        imageBanner.setY(0);
    }

    private void initFile() {
        Utils.isExist(Utils.filePath);
    }

    private void initXmlData() throws Exception {
        String xmlPath = Utils.filePath + "/playerlist.xml";
        if (!new File(xmlPath).exists()) {
            return;
        }
        ParseXml parseXml = new ParseXml();
        parseXml.parseXml(xmlPath);
        videoInfoList = parseXml.getVideoInfoList();
        imageInfoList = parseXml.getImagesInfo();

        videoBanner = new VideoBanner(this);
//        mixBanner2 = new MixBanner(this);
        mRootView.addView(videoBanner);
//        mRootView.addView(mixBanner2);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.width = videoInfoList.get(0).getRect()[2];
        params1.height = videoInfoList.get(0).getRect()[3];
        Log.d(TAG, "w: " + videoInfoList.get(0).getRect()[2]);
        Log.d(TAG, "h: " + videoInfoList.get(0).getRect()[3]);
        Log.d(TAG, "x: " + videoInfoList.get(0).getRect()[0]);
        Log.d(TAG, "y: " + videoInfoList.get(0).getRect()[1]);
        videoBanner.setLayoutParams(params1);
        videoBanner.setX(videoInfoList.get(0).getRect()[0]);
        videoBanner.setY(videoInfoList.get(0).getRect()[1]);

        for (ParseXml.VideoInfo info : videoInfoList) {
            videoFilelist.add(info.getName());
        }
//        mixBanner2.setDataList(videoFilelist);
//        mixBanner2.setImgDelyed(2000);
//        mixBanner2.startBanner();
//        mixBanner2.update();
//        mixBanner2.startAutoPlay();
        videoBanner.setVideoList(videoFilelist);

        for (List<ParseXml.ImageInfo> infosList : imageInfoList) {
            for (ParseXml.ImageInfo info : infosList) {
                Banner imageBanner = new Banner(this);
                mRootView.addView(imageBanner);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.width = info.getRect()[2];
                params.height = info.getRect()[3];
                imageBanner.setLayoutParams(params);
                imageBanner.setX(info.getRect()[0]);
                imageBanner.setY(info.getRect()[1]);
                Log.d(TAG, "w: " + info.getRect()[2]);
                Log.d(TAG, "h: " + info.getRect()[3]);
                Log.d(TAG, "x: " + info.getRect()[0]);
                Log.d(TAG, "y: " + info.getRect()[1]);
                imageBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                imageBanner.setImageLoader(new MyLoader());
                imageBanner.setImages(info.getNames());
                List list_title = new ArrayList<>();
                for (String title : info.getNames()) {
                    list_title.add("");
                }
                imageBanner.setBannerTitles(list_title);
                imageBanner.setBannerAnimation(Transformer.Default);
                imageBanner.setDelayTime(info.getDelay() * 1000);
                imageBanner.isAutoPlay(true);
                imageBanner.setIndicatorGravity(BannerConfig.CENTER);
                imageBanner.start();
                banners.add(imageBanner);
            }
        }


        if (videoInfoList.isEmpty() && imageInfoList.isEmpty()) {
            for (Banner b : banners) {
                b.setVisibility(View.GONE);
            }
            videoBanner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        } else {
            for (Banner b : banners) {
                b.setVisibility(View.VISIBLE);
            }
            videoBanner.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
        }
    }

    private void initDate() {
        fileList = new ArrayList<>();
        File file = new File(Utils.filePath);
        File[] files = file.listFiles();
        Log.d(TAG, "initDate: " + files.length);
        if (files.length != 0) {
//            imageBanner.setVisibility(View.VISIBLE);
            videoBanner.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "data: " + files[i].getAbsolutePath());
                String url = files[i].getAbsolutePath();
                if (Utils.getFileExtend(url).equals("mp4") || Utils.getFileExtend(url).equals("mkv") || Utils.getFileExtend(url).equals("wmv") ||
                        Utils.getFileExtend(url).equals("avi") || Utils.getFileExtend(url).equals("ts") || Utils.getFileExtend(url).equals("mpg") ||
                        Utils.getFileExtend(url).equals("jpg") || Utils.getFileExtend(url).equals("png") || Utils.getFileExtend(url).equals("jpeg")) {
                    fileList.add(files[i].getAbsolutePath());
                }
            }
            try {
//                mixBanner2.setDataList(fileList);
//                mixBanner2.setImgDelyed(2000);
//                mixBanner2.startBanner();
//                mixBanner2.update();
//                mixBanner2.startAutoPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
//            imageBanner.setVisibility(View.GONE);
            videoBanner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        }
    }

    private class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context).asBitmap().load(new File((String) path)).into(imageView);
        }
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
//        initDate();
        try {
            initXmlData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitApp(View view) {
        Log.d(TAG, "exitApp: 1111");
        SharedPreferences sharedPreferences = getSharedPreferences("password", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (view.getId() == R.id.exit_area) {
            final AlertDialog.Builder exitDialog =
                    new AlertDialog.Builder(MainActivity.this);
            exitDialog.setTitle("请输入密码退出应用");
            final EditText editPassword = new EditText(MainActivity.this);
            editPassword.setInputType(129);
            exitDialog.setView(editPassword);

            exitDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = editPassword.getText().toString();
                            String storagePassword = sharedPreferences.getString("password", "123456");
                            if (storagePassword.equals(password)) {
//                                imageBanner.stopPlay();
//                                mixBanner2.stopPlay();

                                videoBanner.releasePlayer();
                                MainActivity.this.finish();
                            } else {
                                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            exitDialog.setNegativeButton("关闭",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            exitDialog.show();
        } else if (view.getId() == R.id.input_password) {
            final AlertDialog.Builder newpasswordDialog =
                    new AlertDialog.Builder(MainActivity.this);
            newpasswordDialog.setTitle("请输入新的密码");
            LinearLayout passwordLayout = new LinearLayout(MainActivity.this);
            final EditText editPassword = new EditText(MainActivity.this);
            editPassword.setHint("输入密码");
            editPassword.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            editPassword.setHintTextColor(getResources().getColor(R.color.gray));
            final EditText editPassword1 = new EditText(MainActivity.this);
            editPassword1.setHint("请再次输入密码");
            editPassword1.setHintTextColor(getResources().getColor(R.color.gray));
            editPassword1.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            passwordLayout.setOrientation(LinearLayout.VERTICAL);
            passwordLayout.addView(editPassword);
            passwordLayout.addView(editPassword1);
            newpasswordDialog.setView(passwordLayout);

            newpasswordDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = editPassword.getText().toString();
                            String password1 = editPassword1.getText().toString();
                            String regExp = "^[\\w_]{6,20}$";
                            if (password.matches(regExp) && password1.matches(regExp) && password.equals(password1)) {
                                editor.putString("password", password);
                                editor.commit();
                            } else {
                                Toast.makeText(MainActivity.this, "密码应为6到20位字母或数字，或者两次密码输入不一致", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            newpasswordDialog.setNegativeButton("关闭",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            newpasswordDialog.show();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
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
//                                    Log.d(TAG, "onProgress: " + dirFileCount + "-" + hasReadCount + "==" + dirSize + "-" + hasReadSize);
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

//        imageBanner.setVisibility(View.GONE);
        if (banners != null && videoBanner != null) {
            for (Banner b : banners) {
                b.setVisibility(View.GONE);
            }
            videoBanner.setVisibility(View.GONE);
            videoBanner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        }
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