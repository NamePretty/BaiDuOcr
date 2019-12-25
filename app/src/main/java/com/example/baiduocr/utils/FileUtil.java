package com.example.baiduocr.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtil {

    private static String cameraPath = "/ocr_app/camera";

    public static File getSaveFile(Context context) {
        return new File(context.getFilesDir(), "pic.jpg");
    }

    /**
     * 判断Sd卡是否存在
     *
     * @param context
     * @return
     */
    public static boolean hasSdcard(Context context) {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String getCameraPath(Context context) {
        String path;
        if (hasSdcard(context)) {
            path = Environment.getExternalStorageDirectory() + cameraPath;
        } else {
            path = Environment.getRootDirectory() + cameraPath;
        }

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

}
