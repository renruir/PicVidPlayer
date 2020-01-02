package com.ctftek.player.banner;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.ctftek.player.video.CustomManager;
import com.ctftek.player.video.MultiSampleVideo;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE;
import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING;

public class VideoBanner extends LinearLayout {

    private static final String TAG = "VideoBanner";
    private Context mContext;
    private List<String> videoList = new ArrayList<>();
    private int currIndex = 0;
    MultiSampleVideo videoPlayer;

    public VideoBanner(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public VideoBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        videoPlayer = new MultiSampleVideo(mContext);

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
        CustomManager.getCustomManager(MultiSampleVideo.TAG).setOptionModelList(list);
//        CustomManager.getCustomManager(EmptyControlVideo.TAG).setOptionModelList(list);
    }

    public void setVideoList(List<String> data) {
        Log.d(TAG, "setDataList: " + data.size());
        currIndex = 0;
        if (this.videoList.size() != 0) {
            videoList.clear();
        }
        this.videoList = data;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        final MultiSampleVideo videoPlayer = new MultiSampleVideo(getContext());
        videoPlayer.setPlayTag(TAG);
        videoPlayer.setPlayPosition(currIndex);
        videoPlayer.setRotateViewAuto(true);
        videoPlayer.setLockLand(true);
        videoPlayer.setReleaseWhenLossAudio(false);
        videoPlayer.setShowFullAnimation(true);
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.setNeedLockFull(true);
        videoPlayer.setLayoutParams(lp);
        doPlay(videoPlayer, currIndex);

        videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                Log.d(TAG, "onPrepared: 888888888");
//                doPlay(videoPlayer, currIndex);
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                super.onPlayError(url, objects);
                Log.d(TAG, "onPlayError: " + url);
                doPlay(videoPlayer, ++currIndex);
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                doPlay(videoPlayer, ++currIndex);
            }
        });
    }

    private void doPlay(MultiSampleVideo vPlay, int index) {
        Log.d(TAG, "doPlay index :" + index);
        if (index >= videoList.size()) {
            currIndex = 0;
        }
        vPlay.setUpLazy(videoList.get(currIndex), false, null, null, "这是title");
        vPlay.setUp(videoList.get(currIndex), true, "");
        vPlay.setPlayPosition(currIndex);
        vPlay.startPlayLogic();
        removeAllViews();
        addView(videoPlayer);
    }


    public void releasePlayer() {
        if (videoPlayer.isInPlayingState()) {
            videoPlayer.release();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, 975, 669, 20);
            }
        });
    }
}
