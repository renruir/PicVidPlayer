package com.ctftek.player;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.ctftek.player.bean.ScrolltextBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {

    private static final String TAG = Utils.class.getName();
        public static final String filePath = "/mnt/sdcard/mediaResource";
//    public static final String filePath = "/sdcard/mediaResource";//for 小米8\
    public static final String legalPath = "/sys/class/leds/ctf-blue";
//    public static final String databasePath = "/metadata/mConfig";
    public static final String passwordFile = "aplayer.properties";
    public static final String superPassword = "ctf9876543210";
    private static Activity mActivity;
    private static Utils mUtils = null;

    private Utils() {
    }

    public static Utils getInstance(Activity activity) {
        if (mUtils == null) {
            synchronized (Utils.class) {
                if (mUtils == null) {
                    mActivity = activity;
                    mUtils = new Utils();
                }
            }
        }
        return mUtils;
    }

    public static List<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    public static void isExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * 遍历文件夹下的文件
     *
     * @param file 地址
     */
    public static List<File> getFile(File file) {
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null) {
            return null;
        } else {
            for (File f : fileArray) {
                if (f.isFile()) {
                    list.add(0, f);
                } else {
                    getFile(f);
                }
            }
        }
        return list;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件地址
     * @return
     */
    public static boolean deleteFiles(String filePath) {
        List<File> files = getFile(new File(filePath));
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                /**  如果是文件则删除  如果都删除可不必判断  */
                if (file.isFile()) {
                    file.delete();
                }

            }
        }
        return true;
    }

    /**
     * 获取Android内部存储的大小
     */
    public static long getInternalMemorySize(Context context) {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        long size = blockCountLong * blockSizeLong;
        Log.d(TAG, "getInternalMemorySize: " + size);
//        return Formatter.formatFileSize(context, size);
        return size;
    }

    /**
     * 获取指定文件夹的大小
     *
     * @param file
     * @return
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getFolderSize: " + size);
        return size;
    }

//    public static boolean copy(String oldPath, String newPath){
//        CopyPasteUtil.build()
//                .setIsNeesDefaulProgressDialog(true)
//                .initValueAndGetDirSize(mActivity, new File(oldPath), new CopyPasteUtil.InitListener() {
//                    @Override
//                    public void onNext(long dirFileCount, long dirSize, CopyPasteUtil.CopyPasteImp imp) {
//                        int fileVolume = (int) (dirSize / (1024 * 1024));
//                        UiThread.run(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "onNext->dirFileCount:" + dirFileCount + "==onNext->dirSize:" + fileVolume + "M", Toast.LENGTH_LONG).show();
//                            }
//                        });
//
//                        imp.copyDirectiory(mActivity, oldPath, newPath, new CopyPasteUtil.CopyPasteListener() {
//                            @Override
//                            public void onSuccess() {
//                                UiThread.run(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(mActivity, "onSuccess:" + i++, Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onProgress(long dirFileCount, long hasReadCount, long dirSize, long hasReadSize) {
//                                UiThread.run(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mVersion.setText(dirFileCount + "-" + hasReadCount + "==" + dirSize + "-" + hasReadSize);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onFail(String errorMsg) {
//                                UiThread.run(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(mActivity, "onFail", Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onCancle() {
//                                UiThread.run(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(mActivity, "onCancle", Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                            }
//                        });
//                    }
//                });
//        return true;
//    }

    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    Log.e(TAG, "begin copy......");
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    Log.e(TAG, "end copy......");
                }

                /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                 */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getFileExtend(String filePath) {
        String exten = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            exten = filePath.substring(i + 1);
        }
        return exten;
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static ScrolltextBean readScrollTextJson(String filePath) {
        String scrollTextJson = null;
        ScrolltextBean scrolltextBean = null;
        InputStreamReader inputStreamReader;
        try {
            InputStream inputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStreamReader.close();
            bufferedReader.close();
            scrollTextJson = stringBuilder.toString();
            Log.i(TAG, stringBuilder.toString());
            scrolltextBean = JSON.parseObject(scrollTextJson, ScrolltextBean.class);
            Log.d(TAG, "readScrollText: " + scrolltextBean.getText());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scrolltextBean;
    }

    public static File[] orderByName(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        List fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (File file1 : files) {
            System.out.println(file1.getName());

        }
        return files;
    }

}
