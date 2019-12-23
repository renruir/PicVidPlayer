package com.ctftek.player;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ctftek.player.sax.SaxService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = TestActivity.class.getName();
    Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        btn = (Button) findViewById(R.id.sax_xml);
        //点击按钮，开启线程访问网络
        btn.setOnClickListener(v -> {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // 设置XML文档的路径
                    String xmlPath = "/mnt/sdcard/mediaResource/playerlist.xml";
                    try {
                        File path = new File(xmlPath);
                        InputStream inputStream = new FileInputStream(path);
                        //调用类SaxService：解析流，同时设定需要解析的节点
                        List<HashMap<String, String>> list = SaxService.readXML(inputStream, "SUBW1");
                        Log.d(TAG, "run: " + list.size());
                        for (HashMap<String, String> map : list) {
                            Log.d(TAG, "xml: " + map.toString());
                            Log.d(TAG, "value : " + map.get("rect"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        });
    }
}
