package com.netease.image.library.utils;

import android.os.Environment;

public class Constants {

    // 相机
    public final static int CAMERA_CODE = 1001;
    // 相册
    public final static int ALBUM_CODE = 1002;
    // 缓存根目录
    public final static String BASE_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/";
    // 压缩后缓存路径
    public final static String COMPRESS_CACHE = "compress_cache";
}
