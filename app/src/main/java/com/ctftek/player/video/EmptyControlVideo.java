package com.ctftek.player.video;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.ctftek.player.R;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;

/**
 * 无任何控制ui的播放
 * Created by guoshuyu on 2017/8/6.
 */

public class EmptyControlVideo extends StandardGSYVideoPlayer {
    public final static String TAG = "EmptyControlVideo";

    public EmptyControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public EmptyControlVideo(Context context) {
        super(context);
    }

    public EmptyControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        //todo 判断如果不是外界造成的就不处理
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        //todo 判断如果不是外界造成的就不处理
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        break;
                }
            }
        };
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
//        Log.d(TAG, "getGSYVideoManager: " + getKey());
        CustomManager.getCustomManager(getKey()).initContext(getContext().getApplicationContext());
        return CustomManager.getCustomManager(getKey());
    }

    public String getKey() {
        if (mPlayPosition == -22) {
            Debuger.printfError(getClass().getSimpleName() + " used getKey() " + "******* PlayPosition never set. ********");
        }
        if (TextUtils.isEmpty(mPlayTag)) {
            Debuger.printfError(getClass().getSimpleName() + " used getKey() " + "******* PlayTag never set. ********");
        }
        return TAG + mPlayPosition + mPlayTag;
    }

    @Override
    public int getLayoutId() {
        return R.layout.empty_control_video;
    }

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //不给触摸快进，如果需要，屏蔽下方代码即可
        mChangePosition = false;

        //不给触摸音量，如果需要，屏蔽下方代码即可
        mChangeVolume = false;

        //不给触摸亮度，如果需要，屏蔽下方代码即可
        mBrightness = false;
    }

    @Override
    protected void touchDoubleUp() {
        //super.touchDoubleUp();
        //不需要双击暂停
    }
}
