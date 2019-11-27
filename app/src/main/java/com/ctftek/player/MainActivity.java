package com.ctftek.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.xdandroid.hellodaemon.DaemonEnv;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
    }
}
