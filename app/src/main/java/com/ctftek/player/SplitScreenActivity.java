package com.ctftek.player;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

public class SplitScreenActivity extends AppCompatActivity {

    private ViewGroup mRootView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_screen);
        mRootView = findViewById(android.R.id.content);
    }

    public void processBtn(View view) {
        Log.d("renrui", "processBtn: ");
        ImageView imageView = new ImageView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.width = 100;
        params.height = 100;
        imageView.setLayoutParams(params);
        addContentView(imageView, params);
        RequestOptions options = new RequestOptions();
        options.fitCenter();
        Glide.with(this).load(R.drawable.test_image).apply(options).into(imageView);
        imageView.setX(100);
        imageView.setY(500);

    }
}
