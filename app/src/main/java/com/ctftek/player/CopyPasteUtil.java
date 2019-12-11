package com.ctftek.player;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.Formatter;
import android.util.Log;

import com.ctftek.player.ui.CommonProgressDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyPasteUtil {
    private static final String TAG = CopyPasteUtil.class.getName();
    private static Context mContext;
    /**
     * copy过程的监听接口
     */
    public interface CopyPasteListener {
        void onSuccess();

        void onProgress(long dirFileCount, long hasReadCount, long dirSize, long hasReadSize);

        void onFail(String errorMsg);

        void onCancle();
    }

    /**
     * 初始化的监听接口
     */
    public interface InitListener {
        void onNext(long dirFileCount, long dirSize, CopyPasteImp imp);
    }

    public static CopyPasteImp build() {
        return new CopyPasteImp();
    }

    public static class CopyPasteImp {
        private long dirSize = 0;// 文件夹总体积
        private long hasReadSize = 0;// 已复制的部分，体积
        private long dirFileCount = 0;// 文件总个数
        private long hasReadCount = 0;// 已复制的文件个数
        private CommonProgressDialog progressDialog;// 进度提示框
        private boolean isNeesDefaulProgressDialog = true;
        private Thread copyFileThread;
        private FileInputStream fileInputStream = null;
        private FileOutputStream fileOutputStream = null;
        private FileChannel fileChannelOutput = null;
        private FileChannel fileChannelInput = null;
        private BufferedInputStream inbuff = null; //todo 屏蔽之后，代码照跑无误，严重怀疑buff是否还有作用，未针对调试。
        private BufferedOutputStream outbuff = null;

        public CommonProgressDialog getProgressDialog() {
            return progressDialog;
        }

        /**
         * 复制单个文件
         */
        public boolean copyFile(final String oldPathName, final String newPathName, Context context) {
            //大于50M时，才显示进度框
            final File oldFile = new File(oldPathName);
            if (oldFile.length() > 50 * 1024 * 1024) {
                if (isNeesDefaulProgressDialog && null == progressDialog) {
                    progressDialog = new CommonProgressDialog(context);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            copyFileThread.interrupt();
                            copyFileThread = null;
                            try {
                                fileInputStream.close();
                                fileOutputStream.close();
                                fileChannelOutput.close();
                                fileChannelInput.close();
                            } catch (IOException e) {
                                Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
                            }
                        }
                    });
                    progressDialog.show();
                }
            }
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        File fromFile = new File(oldPathName);
                        File targetFile = new File(newPathName);
                        fileInputStream = new FileInputStream(fromFile);
                        fileOutputStream = new FileOutputStream(targetFile);
                        fileChannelOutput = fileOutputStream.getChannel();
                        fileChannelInput = fileInputStream.getChannel();
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        long transferSize = 0;
                        long size = new File(oldPathName).length();
                        Log.d(TAG, "old file size: " + size);
                        int fileVolume = (int) (size / 1024 / 1024);//单位为M
                        int tempP = 0;
                        int progress = 0;
                        if (null != progressDialog) {
//                            progressDialog.setMax(fileVolume * 1024 * 1024);
                            progressDialog.setMax(fileVolume);
                        }

                        while (fileChannelInput.read(buffer) != -1) {
                            buffer.flip();
                            transferSize += fileChannelOutput.write(buffer);
                            progress = (int) transferSize / (1024 * 1024);
                            if (progress > tempP) {
                                tempP = progress;
                                if (null != progressDialog) {
//                                    progressDialog.setProgress(progress * 1024 * 1024);
                                    progressDialog.setProgress(progress);
                                }
                            }
                            buffer.clear();
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        fileInputStream.close();
                        fileChannelOutput.close();
                        fileChannelInput.close();
                        if (null != progressDialog && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        Log.e("CopyPasteUtil", "CopyPasteUtil copyFile error:" + e.getMessage());
                    }
                }
            };
            copyFileThread = new Thread(run);
            copyFileThread.start();
            return true;
        }

        /**
         * 复制文件夹
         */
        public void copyDirectiory(Context context, String sourceDir, String targetDir, CopyPasteListener call) {
            if (context != null) {
                mContext = context;
                if (isNeesDefaulProgressDialog && null == progressDialog) {
                    progressDialog = new CommonProgressDialog(context);
                }
                if (null != progressDialog) {
                    progressDialog.setMessage("文件迁移正在进行中...");
                    Log.d(TAG, "copyDirectiory size: " + Utils.getFolderSize(new File(sourceDir)));
//                    dirSize = Utils.getFolderSize(new File(sourceDir));
//                    progressDialog.setMax((int) (Utils.getFolderSize(new File(sourceDir)) / (1024 * 1024)));

                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            copyFileThread.interrupt();
                            copyFileThread = null;
                            try {
                                if (null != fileInputStream) fileInputStream.close();
                                if (null != fileOutputStream) fileOutputStream.close();
                                if (null != inbuff) inbuff.close();
                                if (null != outbuff) outbuff.close();
                                if (null != fileChannelOutput) fileChannelOutput.close();
                                if (null != fileChannelInput) fileChannelInput.close();
                                if (null != call) {
                                    call.onCancle();
                                }
                            } catch (IOException e) {
                                Log.e("CopyPasteUtil", "CopyPasteUtil copyDirectiory error:" + e.getMessage());
                            }
                        }
                    });
                    progressDialog.show();
                }
            }
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    copyDirMethod(0, sourceDir, targetDir, call);
                }
            };
            copyFileThread = new Thread(run);
            copyFileThread.start();
        }

        /**
         * 复制文件夹,真正的执行动作
         */
        private void copyDirMethod(int dirLevel, String sourceDir, String targetDir, CopyPasteListener call) {
            (new File(targetDir)).mkdirs();
            dirLevel++; //进入下一层 层级+1
            File[] file = (new File(sourceDir)).listFiles();// 获取源文件夹当下的文件或目录
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    File sourceFile = file[i];
                    progressDialog.setMessage("文件迁移正在进行中..." + file[i].getName());
                    File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());// 目标文件
                    copyFileMethod(sourceFile, targetFile, call);
                } else if (file[i].isDirectory()) {
                    String dir1 = sourceDir + "/" + file[i].getName();
                    String dir2 = targetDir + "/" + file[i].getName();
                    copyDirMethod(dirLevel, dir1, dir2, call);
                }
            }
            dirLevel--;//该层已经循环遍历完毕，返回上一层 层级-1
            //层级小于等于0，说明已经计算完毕，递归回到最顶层
            if (dirLevel <= 0 && null != call) {
                if (null != progressDialog) {
                    Log.d(TAG, "5555555555: ");
                    progressDialog.setMessage("文件迁移完成");
                    call.onSuccess();
                }
            }
        }

        /**
         * 复制单个文件，用于上面的复制文件夹方法
         * @param sourcefile 源文件路径
         * @param targetFile 目标路径
         */
        private synchronized void copyFileMethod(final File sourcefile, final File targetFile, CopyPasteListener call) {
            try {
                fileInputStream = new FileInputStream(sourcefile);
                inbuff = new BufferedInputStream(fileInputStream);
                fileOutputStream = new FileOutputStream(targetFile);// 新建文件输出流并对它进行缓冲
                outbuff = new BufferedOutputStream(fileOutputStream);
                int fileVolume = (int) (dirSize / (1024 * 1024));//单位为M
                fileChannelOutput = fileOutputStream.getChannel();
                fileChannelInput = fileInputStream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                long transferSize = 0;
                int tempP = 0;
                int progress = 0;
                if (null != progressDialog) {
//                    progressDialog.setMax(fileVolume * 1024 * 1024);
                    progressDialog.setMax(fileVolume);
                }
                while (fileChannelInput.read(buffer) != -1) {
                    buffer.flip();
                    transferSize += fileChannelOutput.write(buffer);
                    progress = (int) ((transferSize + hasReadSize) / (1024 * 1024));
                    if (progress > tempP) {
                        tempP = progress;
                        if (null != progressDialog) {
//                            progressDialog.setProgress(progress * 1024 * 1024);//M
                            progressDialog.setProgress(progress);
                        }
                        if (null != call) { //此处重点在于传递大小
                            call.onProgress(dirFileCount, hasReadCount, dirSize, transferSize + hasReadSize);
                        }
                    }
                    buffer.clear();
                }
                hasReadCount++;
                hasReadSize += sourcefile.length();
                if (null != call) { //此处重点在于传递文件个数
                    call.onProgress(dirFileCount, hasReadCount, dirSize, hasReadSize);
                }
                outbuff.flush();
                fileOutputStream.flush();
                inbuff.close();
                outbuff.close();
                fileOutputStream.close();
                fileInputStream.close();
                fileChannelOutput.close();
                fileChannelInput.close();
//                if (hasReadSize >= dirSize && null != call) {
//                    call.onSuccess();
//                }
            } catch (FileNotFoundException e) {
                if (null != call) {
                    call.onFail(e.getMessage());
                }
            } catch (IOException e) {
                if (null != call) {
                    call.onFail(e.getMessage());
                }
            } catch (Exception e){
                if (null != call) {
                    call.onFail(e.getMessage());
                }
            }
        }

        /**
         * 删除整个文件夹
         * FileCopeTool.deleteFolder(URL.HtmlPath + "/" + identify);
         *
         * @param path 路径，无需文件名
         */
        public void deleteFolder(String path) {
            File f = new File(path);
            if (f.exists()) {
                // 在判断它是不是一个目录
                if (f.isDirectory()) {
                    // 列出该文件夹下的所有内容
                    String[] fileList = f.list();
                    if (fileList == null) {
                        return;
                    }
                    for (int i = 0; i < fileList.length; i++) {
                        // 对每个文件名进行判断
                        // 如果是文件夹 那么就循环deleteFolder
                        // 如果不是，直接删除文件
                        String name = path + File.separator + fileList[i];
                        File ff = new File(name);
                        if (ff.isDirectory()) {
                            deleteFolder(name);
                        } else {
                            ff.delete();
                        }
                    }
                    // 最后删除文件夹
                    f.delete();

                } else {
                    // 该文件夹不是一个目录
                }
            } else {
                //不存在该文件夹
            }
        }

        /**
         * 获取文件夹大小
         *
         * @param dirLevel 计算文件夹的层级，用于判断递归遍历是否完成,初始调用应该设置为0
         * @param file
         * @param call     完成回调
         */
        private void getDirSize(int dirLevel, File file, InitListener call) {
            if (file.isFile()) {
                // 如果是文件，获取文件大小累加
                dirSize += file.length();
                Log.d(TAG, "dirSize: " + dirSize);
                dirFileCount++;
            } else if (file.isDirectory()) {
                dirLevel++; //进入下一层 层级+1
                File[] f1 = file.listFiles();
                for (int i = 0; i < f1.length; i++) {
                    // 调用递归遍历f1数组中的每一个对象
                    getDirSize(dirLevel, f1[i], call);
                }
                dirLevel--;//该层已经循环遍历完毕，返回上一层 层级-1
            }
            //层级小于等于0，说明已经计算完毕，递归回到最顶层
            if (dirLevel <= 0 && null != call) {
                Log.d(TAG, "old file dirSize: " + dirSize);
                call.onNext(dirFileCount, dirSize, this);
            }
        }

        /**
         * 初始化全局变量
         */
        private void initDirSize() {
            dirSize = 0;
            hasReadSize = 0;
            dirFileCount = 0;
            hasReadCount = 0;
        }

        /**
         * 复制文件夹前，初始化四个个变量
         */
        public void initValueAndGetDirSize(Context context, File file, InitListener call) {
            if (isNeesDefaulProgressDialog) {
                progressDialog = new CommonProgressDialog(context);
                progressDialog.setMessage("准备...");
                progressDialog.show();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initDirSize();
                    getDirSize(0, file, call);
                }
            }).start();
        }

        /**
         * 默认为true
         */
        public CopyPasteImp setIsNeesDefaulProgressDialog(boolean isNeed) {
            isNeesDefaulProgressDialog = isNeed;
            return this;
        }
    }
}