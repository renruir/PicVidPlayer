package com.ctftek.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class StorageReceiver extends BroadcastReceiver {

    private static final String TAG = StorageReceiver.class.getName();
    private static final String FILE_NAME = "mediaResource";
    private String mediaPath = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String path = intent.getDataString().substring(8);
            Log.e(TAG, "onReceive: 插入优盘:" + path);
            if (isContainResource(path)) {
                Utils.deleteFiles(Utils.filePath);
                Utils.copyFolder(mediaPath, Utils.filePath);
                Toast.makeText(context, "文件复制完成！", Toast.LENGTH_SHORT).show();
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            Log.e(TAG, "onReceive: 移除优盘");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startIntent = new Intent(context, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }

    }

    private boolean isContainResource(String path) {
        File file = new File(path);
        Log.d(TAG, "isContainResource: " + file.getAbsolutePath());
        File[] files = file.listFiles();
        if (files == null) {
            Log.e(TAG, "空目录");
            return false;
        } else {
            for (int i = 0; i < files.length; i++) {
                Log.e(TAG, "存储目录有：" + files[i].getAbsolutePath());
                File[] f = files[i].listFiles();
                for (int j = 0; j < f.length; j++) {
                    Log.d(TAG, "son files : " + f[j].getAbsolutePath());
                    if (f[j].getAbsolutePath().contains(FILE_NAME)) {
                        mediaPath = f[j].getAbsolutePath();
                        Log.d(TAG, "mediaPath: " + mediaPath);
                        if (!isEmptyFolder(mediaPath)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isEmptyFolder(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return true;
        } else {
            for (int i = 0; i < files.length; i++) {
                String filePath = files[i].getAbsolutePath();
                if (filePath.endsWith("jpg") || filePath.endsWith("png") || filePath.endsWith("gif")
                        || filePath.endsWith("mp4") || filePath.endsWith("mkv") || filePath.endsWith("avi")) {
                    return false;
                }
            }
        }
        return true;
    }
}
