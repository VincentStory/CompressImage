package com.netease.image.library.listener;

import java.util.ArrayList;

import com.netease.image.library.bean.Photo;

/**
 * 图片集合的压缩返回监听
 */
public interface CompressImage {

    // 开始压缩
    void compress();

    // 图片集合的压缩结果返回
    interface CompressListener {

        // 成功
        void onCompressSuccess(ArrayList<Photo> images);

        // 失败
        void onCompressFailed(ArrayList<Photo> images, String error);
    }
}
