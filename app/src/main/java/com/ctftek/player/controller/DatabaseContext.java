package com.ctftek.player.controller;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ctftek.player.Utils;

import java.io.File;
import java.io.IOException;

public class DatabaseContext extends ContextWrapper {

    private final static String TAG = DatabaseContext.class.getName();
    private Context mContext;

    public DatabaseContext(Context base) {
        super(base);
        mContext = base;
    }

    /**
     * 获得数据库路径，如果不存在，则自动创建
     */
    @Override
    public File getDatabasePath(String name) {
        File file = new File(Utils.databasePath);

        if (!file.exists()) {//如果不存在,
            return mContext.getApplicationContext().getFilesDir();
        } else {//如果存在
            String dbPath = Utils.databasePath + "/" + name;//数据库路径
            //数据库文件是否创建成功
            boolean isFileCreateSuccess = false;
            //判断文件是否存在，不存在则创建该文件
            File dbFile = new File(dbPath);
            if (!dbFile.exists()) {
                try {
                    isFileCreateSuccess = dbFile.createNewFile();//创建文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                isFileCreateSuccess = true;
            //返回数据库文件对象
            if (isFileCreateSuccess)
                return dbFile;
            else
                return null;
        }
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }
}
