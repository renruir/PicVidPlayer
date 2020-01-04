package com.ctftek.player.banner;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctftek.player.Utils;
import com.ctftek.player.ui.GalleryTransformer;
import com.ctftek.player.ui.NoAnimationViewPager;
import com.ctftek.player.video.CustomManager;
//import com.ctftek.player.video.EmptyControlVideo;
import com.ctftek.player.video.EmptyControlVideo;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by steven on 2018/5/14.
 */

public class MixBanner extends RelativeLayout implements View.OnTouchListener {

    private static final String TAG = MixBanner.class.getName();
    private NoAnimationViewPager viewPager;
    private final int UPTATE_VIEWPAGER = 100;
    //图片默认时间间隔
    private int imgDelyed = 2000;
    //每个位置默认时间间隔，因为有视频的原因
    private int delyedTime = 2000;
    //默认显示位置
    private int autoCurrIndex = 0;
    //是否自动播放
    private boolean isAutoPlay = false;
    private Time time;
    private List<String> list;
    private List<View> views;
    private BannerViewAdapter mAdapter;
    private Context mContext;

    public MixBanner(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MixBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public MixBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MixBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
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
        GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        GSYVideoType.enableMediaCodecTexture();
        list.add(videoOptionModel);
        CustomManager.getCustomManager(EmptyControlVideo.TAG).setOptionModelList(list);
//        CustomManager.getCustomManager(EmptyControlVideo.TAG).setOptionModelList(list);
    }

    private void init() {
        time = new Time();
        viewPager = new NoAnimationViewPager(getContext());
        LinearLayout.LayoutParams vp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(vp_param);
        viewPager.setPageTransformer(true, new GalleryTransformer() {
            @Override
            public void transformPage(View page, float position) {
//                Log.d(TAG,"page："+page+"，position："+position);
            }
        });
        this.addView(viewPager, -1);
    }

    public void setDataList(List<String> dataList) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        //用于显示的数组
        if (views == null) {
            views = new ArrayList<>();
        } else {
            views.clear();
        }
        try {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            RequestOptions options = new RequestOptions();
            options.fitCenter();
            //数据大于一条，才可以循环
            if (dataList.size() > 1) {
                autoCurrIndex = 1;
                //循环数组，将首位各加一条数据
                for (int i = 0; i < dataList.size() + 2; i++) {
                    String url;
                    if (i == 0) {
                        url = dataList.get(dataList.size() - 1);
                    } else if (i == dataList.size() + 1) {
                        url = dataList.get(0);
                    } else {
                        url = dataList.get(i - 1);
                    }

                    if (Utils.getFileExtend(url).equals("mp4") || Utils.getFileExtend(url).equals("mkv") ||
                            Utils.getFileExtend(url).equals("avi") ||Utils.getFileExtend(url).equals("ts") ||
                            Utils.getFileExtend(url).equals("mpg")||Utils.getFileExtend(url).equals("wmv") ) {
                        final EmptyControlVideo videoPlayer = new EmptyControlVideo(getContext());
                        initPlayer();
                        Log.d(TAG, "setDataList: " + videoPlayer.getGSYVideoManager().getClass().getName());
                        videoPlayer.setPlayTag(TAG);
                        videoPlayer.setPlayPosition(i);
                        videoPlayer.setRotateViewAuto(true);
                        videoPlayer.setLockLand(true);
                        videoPlayer.setReleaseWhenLossAudio(false);
                        videoPlayer.setShowFullAnimation(true);
                        videoPlayer.setIsTouchWiget(false);
                        videoPlayer.setNeedLockFull(true);
                        videoPlayer.setLayoutParams(lp);
                        videoPlayer.setUp(url, true, "");
                        videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {

                            @Override
                            public void onPlayError(String url, Object... objects) {
                                Log.d(TAG, "onPlayError: " + "文件格式错误:" + url+", 跳过，播放下一个");
                                videoPlayer.release();
                                viewPager.setCurrentItem(autoCurrIndex + 1);
                                mHandler.removeCallbacks(runnable);
                                mHandler.postDelayed(runnable, 50);
                            }
                        });
                        views.add(videoPlayer);
                    } else {
                        ImageView imageView = new ImageView(getContext());
                        imageView.setLayoutParams(lp);
                        imageView.setBackgroundColor(Color.BLACK);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        Glide.with(getContext()).load(new File(url)).apply(options).into(imageView);
                        views.add(imageView);
                    }
                }
            } else if (dataList.size() == 1) {
                autoCurrIndex = 0;
                String url = dataList.get(0);
                if (Utils.getFileExtend(url).equals("mp4") || Utils.getFileExtend(url).equals("mkv") ||
                        Utils.getFileExtend(url).equals("avi") ||Utils.getFileExtend(url).equals("ts") ||
                        Utils.getFileExtend(url).equals("mpg")||Utils.getFileExtend(url).equals("wmv")) {
                    final EmptyControlVideo videoPlayer = new EmptyControlVideo(getContext());
                    initPlayer();
                    videoPlayer.setPlayTag(TAG);
                    videoPlayer.setPlayPosition(0);
                    videoPlayer.setRotateViewAuto(true);
                    videoPlayer.setLockLand(true);
                    videoPlayer.setReleaseWhenLossAudio(false);
                    videoPlayer.setShowFullAnimation(true);
                    videoPlayer.setIsTouchWiget(false);
                    videoPlayer.setNeedLockFull(true);
                    videoPlayer.setLayoutParams(lp);
                    videoPlayer.setUp(url, true, "");
                    videoPlayer.setLayoutParams(lp);
                    videoPlayer.setUp(url, true, "");
                    videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {

                        @Override
                        public void onPlayError(String url, Object... objects) {
                            Log.d(TAG, "onPlayError: " + "文件格式错误:" + url+", 跳过，播放下一个");
                            videoPlayer.release();
                            mHandler.removeCallbacks(runnable);
                            mHandler.postDelayed(runnable, 100);
                        }

                        @Override
                        public void onAutoComplete(String url, Object... objects) {
                            super.onAutoComplete(url, objects);
                            videoPlayer.startPlayLogic();
                        }
                    });
                    videoPlayer.setUpLazy(url, false, null, null, "这是title");
                    videoPlayer.startPlayLogic();
                    views.add(videoPlayer);
                } else {
                    ImageView imageView = new ImageView(getContext());
                    imageView.setLayoutParams(lp);
                    imageView.setBackgroundColor(Color.BLACK);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    Glide.with(getContext()).load(new File(url)).apply(options).into(imageView);
                    views.add(imageView);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setImgDelyed(int imgDelyed) {
        this.imgDelyed = imgDelyed;
    }

    public void startBanner() {
        Log.d(TAG, "startBanner: 00000000");
        mAdapter = new BannerViewAdapter(views);
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(autoCurrIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("TAG", "position:" + position);
                //当前位置
                autoCurrIndex = position;
                getDelayedTime(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "1111:" + state);
                //移除自动计时
                mHandler.removeCallbacks(runnable);
                //ViewPager跳转
                int pageIndex = autoCurrIndex;
                if (autoCurrIndex == 0) {
                    pageIndex = views.size() - 2;
                } else if (autoCurrIndex == views.size() - 1) {
                    pageIndex = 1;
                }
                if (pageIndex != autoCurrIndex) {
                    //无滑动动画，直接跳转
                    viewPager.setCurrentItem(pageIndex, false);
                }

                //停止滑动时，重新自动倒计时
                if (state == 0 && isAutoPlay && views.size() > 1) {
                    View view1 = views.get(pageIndex);
                    if (view1 instanceof StandardGSYVideoPlayer) {
                        final EmptyControlVideo videoView = (EmptyControlVideo) view1;
                        videoView.setVideoAllCallBack(new GSYSampleCallBack() {
                            @Override
                            public void onAutoComplete(String url, Object... objects) {
                                Log.d(TAG, "AutoComplete: " + url);
                                videoView.release();
                                if(mHandler != null){
                                    mHandler.postDelayed(runnable, 50);
                                }
                            }

                            @Override
                            public void onPlayError(String url, Object... objects) {
                                Log.e(TAG, "onPlayError:" + url);
                                videoView.release();
                                mHandler.removeCallbacks(runnable);
                                mHandler.postDelayed(runnable, 100);
                            }

                        });

                    } else {
                        delyedTime = imgDelyed;
                        mHandler.postDelayed(runnable, delyedTime);
                    }
                    Log.d(TAG, "" + pageIndex + "--" + autoCurrIndex);
                }
            }
        });
    }

    //开启自动循环
    public void startAutoPlay() {
        isAutoPlay = true;
        if (views.size() > 1) {
            getDelayedTime(autoCurrIndex);
            if (delyedTime <= 0) {
                mHandler.postDelayed(time, imgDelyed);
            } else {
                mHandler.postDelayed(runnable, delyedTime);
            }
        }
    }

    /**
     * 发消息，进行循环
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(UPTATE_VIEWPAGER);
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch: 2222");
        return true;
    }

    /**
     * 这个类获取视频长度，以及已经播放的时间
     */
    private class Time implements Runnable {

        private StandardGSYVideoPlayer videoView;
        private Runnable runnable;

        public void getDelyedTime(StandardGSYVideoPlayer videoView, Runnable runnable) {
            this.videoView = videoView;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            int current = videoView.getPlayPosition();
            int duration = videoView.getDuration();
            int delyedTime = duration - current;
            mHandler.postDelayed(runnable, delyedTime);
        }
    }

    //接受消息实现轮播
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPTATE_VIEWPAGER:
                    viewPager.setCurrentItem(autoCurrIndex + 1);
                    break;
            }
        }
    };

    /**
     * 获取delyedTime
     *
     * @param position 当前位置
     */
    private void getDelayedTime(int position) {
        View view1 = views.get(position);
        if (view1 instanceof StandardGSYVideoPlayer) {
            StandardGSYVideoPlayer videoView = (StandardGSYVideoPlayer) view1;
            videoView.seekTo(0);
            delyedTime = videoView.getDuration();
            if (CustomManager.instance().size() >= 0) {
                Map<String, CustomManager> map = CustomManager.instance();
                List<String> removeKey = new ArrayList<>();
                for (Map.Entry<String, CustomManager> customManagerEntry : map.entrySet()) {
                    CustomManager customManager = customManagerEntry.getValue();
                    //当前播放的位置
                    int po = customManager.getPlayPosition();
                    //对应的播放列表TAG
                    Log.d(TAG, "PlayTag: " + customManager.getPlayTag());
                    if (customManager.getPlayTag().equals(EmptyControlVideo.TAG)) {
                        CustomManager.releaseAllVideos(customManagerEntry.getKey());
                        removeKey.add(customManagerEntry.getKey());
                    }
                }
                if(removeKey.size() > 0) {
                    for (String key : removeKey) {
                        map.remove(key);
                    }
//                    listMultiNormalAdapter.notifyDataSetChanged();
                }
            }
            videoView.startPlayLogic();
            time.getDelyedTime(videoView, runnable);
        } else {
            delyedTime = imgDelyed;
        }
    }

    public void update(){
//        mHandler.removeCallbacks(runnable);
        mAdapter.notifyDataSetChanged();
    }

    public void dataChange(List<String> list) {
        if (list != null && list.size() > 0) {
            //改变资源时要重新开启循环，否则会把视频的时长赋给图片，或者相反
            //因为delyedTime也要改变，所以要重新获取delyedTime
            mHandler.removeCallbacks(runnable);
            setDataList(list);
            mAdapter.setDataList(views);
            mAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(autoCurrIndex, false);
            //开启循环
            if (isAutoPlay && views.size() > 1) {
                getDelayedTime(autoCurrIndex);
                if (delyedTime <= 0) {
                    mHandler.postDelayed(time, imgDelyed);
                } else {
                    mHandler.postDelayed(runnable, delyedTime);
                }
            }
        }
    }

    public void stopPlay(){
        this.setVisibility(GONE);
        if(views!= null && views.size() != 0){
            View view1 = views.get(autoCurrIndex);
            Log.d(TAG, "stopPlay: " + autoCurrIndex);
            mHandler.removeCallbacks(runnable);
            if (view1 instanceof StandardGSYVideoPlayer) {
                StandardGSYVideoPlayer videoPlayer = (StandardGSYVideoPlayer) view1;
                videoPlayer.getGSYVideoManager().stop();
                videoPlayer.release();
            } else {
                ImageView imageView = (ImageView)view1;
                imageView.setVisibility(GONE);
                views.clear();
            }
        }
        Log.d(TAG, "getVisibility: " + this.getVisibility());
        removeAllViews();
    }

    public void destroy() {
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        time = null;
        runnable = null;
        if(views != null){
            views.clear();
        }
        views = null;
        viewPager = null;
        mAdapter = null;
    }
}
