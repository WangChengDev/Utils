package com.yuantuan.ytwebview.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.yuantuan.ytwebview.file.Question;
import com.yuantuan.ytwebview.params.YtfConstant;
import com.yuantuan.ytwebview.safety.EncryptionHandle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * [YTF] (C)2015-2099 Yuantuan Inc.
 * Link        http://www.ytframework.cn
 * =============================================================================
 *
 * @author Like<572919350@qq.com>
 * @created 2016/7/7.
 * @description 一些对数据存储，数据操作，SD卡操作有关的
 * =============================================================================
 */
public class YtfFileUtil {


    private static Context mContext;
    /**
     * 包名
     */
    private static String packageName;
    /**
     * 获取资源
     */
    private static Resources resources;

    /**
     * 单例
     */
    private static YtfFileUtil ytfFileUtil;

    private YtfFileUtil() {

    }

    public static void init(Context context) {
        mContext = context;
        packageName = mContext.getPackageName();
        resources = mContext.getResources();
        ytfFileUtil = new YtfFileUtil();

    }

    /**
     * 单例模式
     *
     * @return
     */
    public static YtfFileUtil getInstance() {
        if (ytfFileUtil == null) {
            throw new NullPointerException("请先初始化YtfFileUtil类");
        } else {
            return ytfFileUtil;
        }
    }

    /**
     * 判断SD卡是否正常挂载
     *
     * @return
     */
    public static boolean SDCardIsWork() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    /**
     * 获取内置SD卡的路径
     *
     * @return
     */
    public static String getExternaStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] list = file.listFiles();
            if (list != null && list.length != 0) {
                File[] file1 = list;
                for (int i = 0; i < list.length; ++i) {
                    File item = file1[i];
                    delete(item);
                }
                file.delete();
            } else {
                file.delete();
            }
        }
    }

    /**
     * 计算文件大小
     *
     * @param file
     * @return
     */
    public static long computeDirOrFileSize(File file) {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                return file.length();
            } else {
                long length = 0L;
                File[] list = file.listFiles();
                if (list != null) {
                    for (int i = 0; i < list.length; ++i) {
                        File item = list[i];
                        if (item.isFile()) {
                            length += item.length();
                        } else {
                            length += computeDirOrFileSize(item);
                        }
                    }
                }
                return length;
            }
        } else {
            return 0L;
        }
    }

    /**
     * 获取原生下面文件的文件流
     *
     * @param path
     * @return
     */
    public String getAssetsFileStream(String path) {
        try {
            InputStreamReader inputReader = new InputStreamReader(resources.getAssets().open(path));
            if (inputReader != null) {
                BufferedReader bufReader = new BufferedReader(inputReader);
                String line = "";
                String result = "";
                while ((line = bufReader.readLine()) != null)
                    result += line;
                return result;
            } else {
                throw new NullPointerException("未找到指定文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取指定文件夹下面的文件内容
     *
     * @param strFilePath
     * @return
     */
    public String getFileString(String strFilePath) {
        /**判断SD是否工作*/
        if (!SDCardIsWork()) {
            return "";
        }
        File file = new File(strFilePath);
        BufferedReader reader;
        String text = "";
        try {
            // FileReader f_reader = new FileReader(file);
            // BufferedReader reader = new BufferedReader(f_reader);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);// 找到文档的前三个字节并自动判断文档类型。
            in.reset();
            L.d("格式：first3bytes[0]="+first3bytes[0]+",first3bytes[1]="+first3bytes[1]+",first3bytes[2]="+first3bytes[2]);
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8
                L.d("格式：utf-8");
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {
                L.d("格式：unicode");
                reader = new BufferedReader(
                        new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {
                L.d("格式：utf-16be");
                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {
                L.d("格式：utf-16le");
                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16le"));
            } else if (first3bytes[0] ==123 &&first3bytes[1]==13&&first3bytes[2]==10) {
                /**UTF-8无BOM*/
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            } else {
                L.d("格式：GBK");
                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();
            int index = 0;
            int line = 0;
            List<Question> questions = new ArrayList<>();
            while (str != null) {
                while (line < 3) {
                    text = text + str + "\r\n";
                    str = reader.readLine();
                    line++;
                }
                line = 0;
                Question question = new Question();
                String[] num = text.split("\r\n");
                for (int i = 0; i < num.length; i++) {

                    if (i % 3 == 0) {
                        question.setId(index);
                    }
                    if (i % 3 == 2) {
                        question.setQuestion(num[i]);
                    }
                    if (i % 3 == 1) {
                        question.setAnwer(num[i]);
                    }

                }
                questions.add(question);
                index++;
            }
            reader.close();


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }


    /**
     * 读取SD卡中文本文件,无编码格式
     *
     * @param fileName
     * @return
     */
    public String readSDFile(String fileName) {
        StringBuffer sb = new StringBuffer();
        File file = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            int c;
            while ((c = fis.read()) != -1) {
                sb.append((char) c);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 写数据到SD
     *
     * @param str      写入字符串
     * @param pathName 路径，路径样式/sdcard/test/
     * @param name     名称
     */
    public static void writeFileToSD(String str, String pathName, String name) {
        /**判断SD是否工作*/
        if (!SDCardIsWork()) {
            return;
        }
        try {
            File path = new File(pathName);
            File file = new File(pathName + name);
            if (!path.exists()) {
                //L.d("TestFile", "Create the path:" + pathName);
                path.mkdir();
            }
            if (!file.exists()) {
                //L.d("TestFile", "Create the file:" + name);
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
            byte[] buf = str.getBytes();
            stream.write(buf);
            stream.close();

        } catch (Exception e) {
            Log.e("TestFile", "Error on writeFilToSD.");
            e.printStackTrace();
        }
    }

    /**
     * 从SD卡读取文件
     *
     * @param path 文件路径+名称，路径样式/sdcard/test/
     * @return
     */
    public static String readFileToSD(String path) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File targetFile = new File(path);
            String readedStr = "";

            try {
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                    return "No File error ";
                } else {
                    InputStream in = new BufferedInputStream(new FileInputStream(targetFile));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String tmp;

                    while ((tmp = br.readLine()) != null) {
                        readedStr += tmp;
                    }
                    br.close();
                    in.close();

                    return readedStr;
                }
            } catch (Exception e) {
                return e.toString();
            }
        } else {
            return "SD Card error";
        }
    }

    /**
     * 复制assets目录下指定所有文件到指定目录下面
     *
     * @param assetDir
     * @param dir
     */
    public static void copyAssets(String assetDir, String dir) {
        String[] files;
        try {
            // 获得Assets一共有几多文件
            files = resources.getAssets().list(assetDir);
            //L.d("<>_<>", "copyAssets: "+files.length);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
            }
        }

        for (int i = 0; i < files.length; i++) {
            try {
                // 获得每个文件的名字
                String fileName = files[i];
                // 根据路径判断是文件夹还是文件
                if (!fileName.contains(".")) {
                    if (0 == assetDir.length()) {
                        copyAssets(fileName, dir + fileName + "/");
                    } else {
                        copyAssets(assetDir + "/" + fileName, dir + "/"
                                + fileName + "/");
                    }
                    continue;
                }
                File outFile = new File(mWorkingPath, fileName);
                if (outFile.exists())
                    outFile.delete();
                InputStream in = null;
                if (0 != assetDir.length())
                    in = mContext.getAssets().open(assetDir + "/" + fileName);
                else
                    in = mContext.getAssets().open(fileName);
                OutputStream out = new FileOutputStream(outFile);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String  原文件路径  如：/aa
     * @param newPath String  复制后路径  如：xx:/bb/cc
     */
    public static void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            //L.d("<>_<>", "copyAssets: "+fileNames.length);
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
        }
    }

    /**
     * 获取包名
     *
     * @return
     */
    public String getPackageName() {
        return mContext.getPackageName();
    }

    /**
     * 获取热修复路径
     *
     * @return
     */
    public String getRepair() {
        return YtfConstant.DATA_HEAD + getPackageName() + "/";
    }

    /**
     * 获取Mime类型
     *
     * @param str
     * @return
     */
    public String getFileMimeType(String str) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(str);

    }


}
