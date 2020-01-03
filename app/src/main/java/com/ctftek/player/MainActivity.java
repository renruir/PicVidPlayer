package com.ctftek.player;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ctftek.player.banner.MixBanner;
import com.ctftek.player.banner.VideoBanner;
import com.ctftek.player.bean.DaoMaster;
import com.ctftek.player.bean.DaoSession;
import com.ctftek.player.bean.ScrolltextBean;
import com.ctftek.player.bean.SecurityWord;
import com.ctftek.player.bean.SecurityWordDao;
import com.ctftek.player.controller.DatabaseContext;
import com.ctftek.player.sax.ParseXml;
import com.ctftek.player.ui.ScrollTextView;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity implements ServiceCallBack {
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //view
    private MixBanner mixBanner;
    //    private MixBanner mixBanner2;
    private FrameLayout parentView;
    private FrameLayout mainView;
    private VideoBanner videoBanner;
    private TextView mText;
    private ImageView exitArea;
    private ImageView inputArea;
    private List<Banner> banners = new ArrayList<>();
    private ScrollTextView marqueeView;
    private ViewGroup mRootView;
    private String xmlPath;

    private StorageService.StorageServiceBinder serviceBinder;

    //data
    final static int COUNTS = 5;//点击次数
    final static long DURATION = 3 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];

    //database
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private SecurityWordDao securityWordDao;

    private List<String> fileList;
    private List<ParseXml.VideoInfo> videoInfoList;
    private List<String> videoFilelist = new ArrayList<>();
    private List<List<ParseXml.ImageInfo>> imageInfoList;

    private Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: from dialog");
            if (iSplit()) {
                try {
                    initXmlData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                initDate();
            }


        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
        Intent intent = new Intent(this, StorageService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        if (!initLegalDevice()) {
            finish();
            Toast.makeText(this, "不合法设备", Toast.LENGTH_SHORT).show();
            return;
        }
        setContentView(R.layout.activity_main);
//        Intent i = new Intent(this, TestActivity.class);
//        startActivity(i);
        initDatabase();
        initView();
        initFile();
        if (iSplit()) {
            try {
                initXmlData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initDate();
        }

        getSecondaryStoragePath();
        Log.d(TAG, "onCreate size: " + Utils.getInternalMemorySize(this));
    }

    private void initPassword() {
        try {
            String passPath = Utils.databasePath + "/aplayer.json";
            if (!new File(passPath).exists()) {
                return;
            }
            ScrolltextBean scrolltextBean = Utils.readScrollTextJson(passPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper daoHelper = new DaoMaster.DevOpenHelper(new DatabaseContext(this), "aplayer.db", null);
        db = daoHelper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        securityWordDao = daoSession.getSecurityWordDao();
        if (securityWordDao.queryBuilder().list().size() == 0) {
            securityWordDao.insert(new SecurityWord(1L, "123456"));
        }
    }

    private boolean iSplit() {
        xmlPath = Utils.filePath + "/ADCFG.txt";
        if (new File(xmlPath).exists()) {
            return true;
        } else {
            return false;
        }
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
        parentView = findViewById(R.id.parentView);
        mainView = findViewById(R.id.mainView);
        mRootView = findViewById(android.R.id.content);
        mText = (TextView) findViewById(R.id.msg_text);
        exitArea = (ImageView) findViewById(R.id.exit_area);
        inputArea = (ImageView) findViewById(R.id.input_password);
        exitArea.setZ(2);
        inputArea.setZ(2);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initScrollText() {
        marqueeView = new ScrollTextView(this);
        mainView.addView(marqueeView, -1);
        marqueeView.setHorizontal(true);
        marqueeView.setScrollForever(true);
        marqueeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        try {
            String scrollTextPath = Utils.filePath + "/ROLLTXT.txt";
            if (!new File(scrollTextPath).exists()) {
                return;
            }
            ScrolltextBean scrolltextBean = Utils.readScrollTextJson(scrollTextPath);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(scrolltextBean.getW(), scrolltextBean.getH());
            marqueeView.setLayoutParams(params);
            marqueeView.setX(scrolltextBean.getX());
            marqueeView.setY(scrolltextBean.getY());
            Log.d(TAG, "initScrollText: x=" + scrolltextBean.getX() + ",y=" + scrolltextBean.getY());
            marqueeView.setText(scrolltextBean);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initFile() {
        Utils.isExist(Utils.filePath);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initXmlData() throws Exception {
        initScrollText();

        ParseXml parseXml = new ParseXml();
        parseXml.parseXml(xmlPath);
        if (videoFilelist != null && !videoFilelist.isEmpty()) {
            videoFilelist.clear();
        }
        videoInfoList = parseXml.getVideoInfoList();
        imageInfoList = parseXml.getImagesInfo();
        Log.d(TAG, "video size: " + videoInfoList.size());
        for (ParseXml.VideoInfo info : videoInfoList) {
            Log.d(TAG, "video name: " + info.getName());
        }

        for (List<ParseXml.ImageInfo> imageInfo : imageInfoList) {
            for (ParseXml.ImageInfo info : imageInfo) {
                Log.d(TAG, "image name: " + info.getNames());
            }
        }

        videoBanner = new VideoBanner(this);
//        mixBanner2 = new MixBanner(this);
        mainView.addView(videoBanner);
//        mRootView.addView(mixBanner2);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.width = videoInfoList.get(0).getRect()[2];
        params1.height = videoInfoList.get(0).getRect()[3];
//        Log.d(TAG, "w: " + videoInfoList.get(0).getRect()[2]);
//        Log.d(TAG, "h: " + videoInfoList.get(0).getRect()[3]);
//        Log.d(TAG, "x: " + videoInfoList.get(0).getRect()[0]);
//        Log.d(TAG, "y: " + videoInfoList.get(0).getRect()[1]);
        videoBanner.setLayoutParams(params1);
        videoBanner.setX(videoInfoList.get(0).getRect()[0]);
        videoBanner.setY(videoInfoList.get(0).getRect()[1]);

        for (ParseXml.VideoInfo info : videoInfoList) {
            videoFilelist.add(info.getName());
        }
        videoBanner.setVideoList(videoFilelist);

        for (List<ParseXml.ImageInfo> infosList : imageInfoList) {
            for (ParseXml.ImageInfo info : infosList) {
                Banner imageBanner = new Banner(this);
                mainView.addView(imageBanner);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.width = info.getRect()[2];
                params.height = info.getRect()[3];
                imageBanner.setLayoutParams(params);
                imageBanner.setX(info.getRect()[0]);
                imageBanner.setY(info.getRect()[1]);
//                Log.d(TAG, "w: " + info.getRect()[2]);
//                Log.d(TAG, "h: " + info.getRect()[3]);
//                Log.d(TAG, "x: " + info.getRect()[0]);
//                Log.d(TAG, "y: " + info.getRect()[1]);
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
                imageBanner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });
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
            if (marqueeView != null) {
                marqueeView.setVisibility(View.VISIBLE);
            }
            mText.setVisibility(View.GONE);
        }

        Drawable drawable = null;
        try {
            String path = Utils.filePath + "/" + parseXml.getBackgroundImage();
            Log.d(TAG, "path: " + path);
            drawable = Drawable.createFromStream(new FileInputStream(path), parseXml.getBackgroundImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        parentView.setBackground(drawable);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDate() {
        fileList = new ArrayList<>();
        File file = new File(Utils.filePath);
        File[] files = file.listFiles();
        initScrollText();
        Log.d(TAG, "initDate: " + files.length);
        mixBanner = new MixBanner(this);
//        mRootView.removeAllViews();
//        mRootView.addView(marqueeView);
        mainView.addView(mixBanner);
        exitArea.bringToFront();
        if (files.length != 0) {
            if (videoBanner != null && videoBanner.getVisibility() == View.VISIBLE) {
                videoBanner.setVisibility(View.GONE);
            }
//            imageBanner.setVisibility(View.VISIBLE);
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
                mixBanner.setDataList(fileList);
                mixBanner.setImgDelyed(8000);
                mixBanner.startBanner();
                mixBanner.update();
                mixBanner.startAutoPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
//            imageBanner.setVisibility(View.GONE);
            mixBanner.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        }
    }

    private class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context)
//                    .asBitmap()
                    .load(new File((String) path))
                    .transforms(new CenterCrop(), new RoundedCorners(20))
                    .into(imageView);
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
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent.getData());
        try {
            if (iSplit()) {
                initXmlData();

            } else {
                initDate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("password", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {

        }
    }

    public void exitApp(View view) {
        if (view.getId() == R.id.exit_area) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
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
                                if ("147258369".equals(password)) {//超级密码，提前结束
                                    if (mixBanner != null) {
                                        mixBanner.stopPlay();
                                    }
                                    if (videoBanner != null) {
                                        videoBanner.releasePlayer();
                                    }
                                    MainActivity.this.finish();
                                }
                                String storagePassword = "123456";
                                SecurityWord securityWord = securityWordDao.queryBuilder().list().get(0);
                                if (securityWord != null) {
                                    storagePassword = securityWord.getPassword();
                                }
                                if (storagePassword.equals(password)) {//普通密码
                                    if (mixBanner != null) {
                                        mixBanner.stopPlay();
                                    }
                                    if (videoBanner != null) {
                                        videoBanner.releasePlayer();
                                    }
                                    ;
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
            }

        } else if (view.getId() == R.id.input_password) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                final AlertDialog.Builder newpasswordDialog =
                        new AlertDialog.Builder(MainActivity.this);
                newpasswordDialog.setTitle("请输入新的密码");
                LinearLayout oldPasswordLayout = new LinearLayout(MainActivity.this);
                final EditText oldPassword = new EditText(MainActivity.this);
                oldPassword.setHint("请输入原密码");
                oldPassword.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                oldPassword.setHintTextColor(getResources().getColor(R.color.gray));
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
                passwordLayout.addView(oldPassword);
                passwordLayout.addView(editPassword);
                passwordLayout.addView(editPassword1);
                newpasswordDialog.setView(passwordLayout);
                SecurityWord securityWord = securityWordDao.queryBuilder().list().get(0);
                newpasswordDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String oldStoragePass = "123456";
                                if (securityWord != null) {
                                    oldStoragePass = securityWord.getPassword();
                                }
                                String oldinputPass = oldPassword.getText().toString();
                                String password = editPassword.getText().toString();
                                String password1 = editPassword1.getText().toString();
                                String regExp = "^[\\w_]{6,20}$";
                                if (oldStoragePass.equals(oldinputPass)) {
                                    if (password.matches(regExp) && password1.matches(regExp) && password.equals(password1)) {
                                        securityWordDao.update(new SecurityWord(1L, password));
                                    } else {
                                        Toast.makeText(MainActivity.this, "密码应为6到20位字母或数字，或者两次密码输入不一致", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
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
        if (banners != null && videoBanner != null) {
            for (Banner b : banners) {
                b.setVisibility(View.GONE);
            }
            videoBanner.setVisibility(View.GONE);
            videoBanner.releasePlayer();
            mText.setVisibility(View.VISIBLE);
        }
        if (mixBanner != null) {
            Log.d(TAG, "updateMediaFile: 5555555555555");
            mixBanner.setVisibility(View.GONE);
            mixBanner.stopPlay();
            mixBanner.destroy();
        }
        if (marqueeView != null) {
            marqueeView.setVisibility(View.GONE);
        }

        parentView.setBackgroundResource(0);
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
                                    Log.d(TAG, "onFail:file copy exception");
                                    try {
                                        MainActivity.this.finish();
                                        CrashApplication crashApplication = (CrashApplication) MainActivity.this.getApplication();
                                        crashApplication.restartApp();
//                                        throw new Exception("文件拷贝异常");
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