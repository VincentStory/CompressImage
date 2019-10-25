package com.netease.image.library;

import android.content.Context;
import android.text.TextUtils;

import com.netease.image.library.bean.Photo;
import com.netease.image.library.config.CompressConfig;
import com.netease.image.library.core.CompressImageUtil;
import com.netease.image.library.listener.CompressImage;
import com.netease.image.library.listener.CompressResultListener;

import java.io.File;
import java.util.ArrayList;

/**
 * 框架：思路、起稿。千万不要过度封装
 * 压缩图片管理类
 * 1、单例？
 * 2、能否重复压缩？
 */
public class CompressImageManager implements CompressImage {

    private CompressImageUtil compressImageUtil; // 压缩工具类
    private ArrayList<Photo> images; // 要压缩的图片集合
    private CompressImage.CompressListener listener; // 压缩监听，告知MainActivity
    private CompressConfig mConfig; // 压缩配置

    /**
     * 私有实现
     *
     * @param context  上下文
     * @param config   配置
     * @param images   图片集合
     * @param listener 监听
     * @return
     */
    private CompressImageManager(Context context, CompressConfig config,
                                 ArrayList<Photo> images, CompressListener listener) {
        compressImageUtil = new CompressImageUtil(context, config);
        this.mConfig = config;
        this.images = images;
        this.listener = listener;
    }

    public void setImages(ArrayList<Photo> images) {
        this.images = images;
    }

    public void setListener(CompressListener listener) {
        this.listener = listener;
    }

    public void setConfig(CompressConfig config) {
        this.mConfig = config;
    }

    public void setCompressImageUtil(CompressImageUtil compressImageUtil) {
        this.compressImageUtil = compressImageUtil;
    }

    /**
     * 静态方法，new实现
     *
     * @param context  上下文
     * @param config   配置
     * @param images   图片集合
     * @param listener 监听
     * @return
     */
    public static CompressImage build(Context context, CompressConfig config,
                                      ArrayList<Photo> images, CompressImage.CompressListener listener) {
        return new CompressImageManager(context, config, images, listener);
    }

    public CompressImageManager() {
    }


    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {


        private CompressImageManager manager;
        private CompressImageUtil compressImageUtil;

        public Builder(Context context) {
            manager = new CompressImageManager();
            compressImageUtil = new CompressImageUtil(context);
        }

        public Builder config(CompressConfig config) {
            compressImageUtil.setConfig(config);
            manager.setCompressImageUtil(compressImageUtil);
            manager.setConfig(config);
            return this;
        }

        public Builder loadPhtos(ArrayList<Photo> images) {
            manager.setImages(images);
            return this;
        }

        public Builder setCompressListener(CompressListener listener) {
            manager.setListener(listener);
            return this;
        }

        public Builder compress() {
            manager.compress();
            return this;
        }


    }


    @Override
    public void compress() {
        if (images == null || images.isEmpty()) {
            listener.onCompressFailed(images, "集合为空");
            return;
        }

        for (Photo image : images) {
            if (image == null) {
                listener.onCompressFailed(images, "某图片为空");
                return;
            }
        }

        // 开始递归压缩，从第一张开始
        compress(images.get(0));
    }

    // 从第一张开始，index = 0
    private void compress(Photo image) {
        // 路径为空
        if (TextUtils.isEmpty(image.getOriginalPath())) {
            // 继续
            continueCompress(image, false);
            return;
        }

        // 文件不存在
        File file = new File(image.getOriginalPath());
        if (!file.exists() || !file.isFile()) {
            continueCompress(image, false);
            return;
        }

        // < 200KB
        if (file.length() < mConfig.getMaxSize()) {
            continueCompress(image, true);
            return;
        }

        // 单张压缩
        compressImageUtil.compress(image.getOriginalPath(), new CompressResultListener() {
            @Override
            public void onCompressSuccess(String imgPath) {
                image.setCompressPath(imgPath);
                continueCompress(image, true);
            }

            @Override
            public void onCompressFailed(String imgPath, String error) {
                continueCompress(image, false, error);
            }
        });
    }

    private void continueCompress(Photo image, boolean bool, String... error) {
        image.setCompressed(bool);
        // 当前图片的索引
        int index = images.indexOf(image);
        if (index == images.size() - 1) { // 最后一张
            handlerCallback(error);
        } else {
            compress(images.get(index + 1));
        }
    }

    private void handlerCallback(String... error) {
        if (error.length > 0) {
            listener.onCompressFailed(images, error[0]);
            return;
        }

        for (Photo image : images) {
            // 如果存在没有压缩的图片，或者压缩失败的
            if (!image.isCompressed()) {
                listener.onCompressFailed(images, image.getOriginalPath() + "压缩失败");
                return;
            }
        }

        listener.onCompressSuccess(images);
    }
}
