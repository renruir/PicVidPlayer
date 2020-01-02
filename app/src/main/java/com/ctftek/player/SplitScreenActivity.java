package com.ctftek.player;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctftek.player.banner.VideoBanner;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SplitScreenActivity extends AppCompatActivity {

    private static final String TAG = SplitScreenActivity.class.getName();
    private ViewGroup mRootView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_screen);
        Log.d(TAG, "onCreate: 777777777777777");
        mRootView = findViewById(android.R.id.content);
        VideoBanner videoBanner = new VideoBanner(this);
        mRootView.addView(videoBanner);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoBanner.setLayoutParams(lp);
        String url1 = "/mnt/sdcard/mediaResource/1.mp4";
        String url2 = "/mnt/sdcard/mediaResource/2.mp4";
        String url3 = "/mnt/sdcard/mediaResource/3.mp4";
        List<String> videoList = new ArrayList<>();
        videoList.add(url1);
        videoList.add(url2);
        videoList.add(url3);
        videoBanner.setVideoList(videoList);
    }


}
