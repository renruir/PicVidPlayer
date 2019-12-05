package com.ctftek.player.banner;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctftek.player.MVideoView;
import com.ctftek.player.Utils;
import com.ctftek.player.video.EmptyControlVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.shuyu.gsyvideoplayer.GSYVideoADManager.TAG;

/**
 * Created by steven on 2018/5/14.
 */

public class Banner extends RelativeLayout {

    private static final String TAG = Banner.class.getName();
    private ViewPager viewPager;
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
    private List<BannerModel> bannerModels;
    private List<String> list;
    private List<View> views;
    private BannerViewAdapter mAdapter;
    private Context mContext;

    public Banner(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public Banner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public Banner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Banner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        time = new Time();
        viewPager = new ViewPager(getContext());
        LinearLayout.LayoutParams vp_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(vp_param);
        this.addView(viewPager);
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
                        Utils.getFileExtend(url).equals("avi") ||Utils.getFileExtend(url).equals("ts")) {
                    final EmptyControlVideo videoPlayer = new EmptyControlVideo(getContext());

                    videoPlayer.setLayoutParams(lp);
                    videoPlayer.setUp(url, true, "");
//                    PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
//                    videoPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//                    videoPlayer.startPlayLogic();
                    videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {

//                        @Override
//                        public void onAutoComplete(String url, Object... objects) {
//                            Log.d(TAG, "onAutoComplete: " + url);
//                            videoPlayer.startPlayLogic();
//                            views.add(videoPlayer);
//                        }

                        @Override
                        public void onPlayError(String url, Object... objects) {
                            Log.e(TAG, "onPlayError 22:" + url);
                            mHandler.removeCallbacks(runnable);
                            mHandler.postDelayed(runnable, 100);
                            Toast.makeText(mContext, "文件格式错误:" + url+", 跳过，播放下一个", Toast.LENGTH_SHORT).show();
                        }
                    });
                    views.add(videoPlayer);
                } else {
                    ImageView imageView = new ImageView(getContext());
                    imageView.setLayoutParams(lp);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Glide.with(getContext()).load(new File(url)).apply(options).into(imageView);
                    views.add(imageView);
                }
            }
        } else if (dataList.size() == 1) {
            autoCurrIndex = 0;
            String url = dataList.get(0);
            if (Utils.getFileExtend(url).equals("mp4") || Utils.getFileExtend(url).equals("mkv") || Utils.getFileExtend(url).equals("avi")) {
                final EmptyControlVideo videoPlayer = new EmptyControlVideo(getContext());
                videoPlayer.setLayoutParams(lp);
                PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
                videoPlayer.setUp(url, true, "");
                videoPlayer.startPlayLogic();
                videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {

                    @Override
                    public void onPlayError(String url, Object... objects) {
                        Log.e(TAG, "onPlayError:" + url);
                        mHandler.removeCallbacks(runnable);
                        mHandler.postDelayed(runnable, 100);
                    }


                });

            } else {
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(lp);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Glide.with(getContext()).load(new File(url)).apply(options).into(imageView);
                views.add(imageView);
            }
        }
    }

    public void setImgDelyed(int imgDelyed) {
        this.imgDelyed = imgDelyed;
    }

    public void startBanner() {
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
                Log.d("TAG", "1111:" + state);

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
//                        int current = videoView.getPlayPosition();
                        PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
                        videoView.setVideoAllCallBack(new GSYSampleCallBack() {

                            @Override
                            public void onPrepared(String url, Object... objects) {
                                int current = videoView.getPlayPosition();
                                int duration = videoView.getDuration();
                                delyedTime = duration - current;
                                Log.d(TAG, "duration: " + duration);
                                Log.d(TAG, "current: " + current);
                                //某些时候，某些视频，获取的时间无效，就延时10秒，重新获取
                                if (delyedTime <= 0) {
                                    time.getDelyedTime(videoView, runnable);
                                    mHandler.postDelayed(time, imgDelyed);
                                } else {
                                    mHandler.postDelayed(runnable, delyedTime);
                                }
                            }

                            @Override
                            public void onAutoComplete(String url, Object... objects) {
                                Log.d(TAG, "AutoComplete: " + url);
//                                String text = null;
//                                text.substring(1);
                                viewPager.setCurrentItem(autoCurrIndex + 1);
                            }

                            @Override
                            public void onPlayError(String url, Object... objects) {
                                Log.e(TAG, "onPlayError:" + url);
                                mHandler.removeCallbacks(runnable);
                                mHandler.postDelayed(runnable, 100);
                            }

                        });

                    } else {
                        delyedTime = imgDelyed;
                        mHandler.postDelayed(runnable, delyedTime);
                    }
                    Log.d("TAG", "" + pageIndex + "--" + autoCurrIndex);
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

    private class BannerModel {
        public String url;
        public int playTime;
        public int type = 0;
    }

    /**
     * 获取delyedTime
     *
     * @param position 当前位置
     */
    private void getDelayedTime(int position) {
        View view1 = views.get(position);
        if (view1 instanceof StandardGSYVideoPlayer) {
            StandardGSYVideoPlayer videoView = (StandardGSYVideoPlayer) view1;
//            videoView.
            videoView.seekTo(0);
            delyedTime = videoView.getDuration();
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
        if(views.size() != 0){
            View view1 = views.get(autoCurrIndex);
            Log.d(TAG, "stopPlay: " + autoCurrIndex);
            mHandler.removeCallbacks(runnable);
            if (view1 instanceof StandardGSYVideoPlayer) {
                StandardGSYVideoPlayer videoView = (StandardGSYVideoPlayer) view1;
//            videoView.
                videoView.release();
            } else {
                views.clear();
            }
        }
    }

    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        time = null;
        runnable = null;
        views.clear();
        views = null;
        viewPager = null;
        mAdapter = null;
    }
}
