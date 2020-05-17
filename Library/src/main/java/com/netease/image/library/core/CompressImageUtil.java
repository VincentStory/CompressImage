package com.netease.image.library.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.netease.image.library.config.CompressConfig;
import com.netease.image.library.listener.CompressResultListener;
import com.netease.image.library.utils.Constants;

/**
 * 压缩照片
 */
public class CompressImageUtil {

    private CompressConfig config;
    private Context context;
    private Handler mhHandler = new Handler();

    public CompressImageUtil(Context context, CompressConfig config) {
        this.context = context;
        this.config = config == null ? CompressConfig.getDefaultConfig() : config;
    }

    public CompressImageUtil(Context context) {
        this.context = context;
    }

    public void setConfig(CompressConfig config) {
        this.config = config == null ? CompressConfig.getDefaultConfig() : config;
    }

    public void compress(String imgPath, CompressResultListener listener) {
        if (config.isEnablePixelCompress()) {
            try {
                compressImageByPixel(imgPath, listener);
            } catch (FileNotFoundException e) {
                listener.onCompressFailed(imgPath, String.format("图片压缩失败,%s", e.toString()));
                e.printStackTrace();
            }
        } else {
            compressImageByQuality(BitmapFactory.decodeFile(imgPath), imgPath, listener);
        }
    }

    /**
     * 多线程压缩图片的质量
     */
    private void compressImageByQuality(final Bitmap bitmap, final String imgPath, final CompressResultListener listener) {
        if (bitmap == null) {
            sendMsg(false, imgPath, "像素压缩失败，bitmap为空", listener);
            return;
        }
//开启多线程进行压缩处理
        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos); // 质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)
            while (baos.toByteArray().length > config.getMaxSize()) { // 循环判断如果压缩后图片是否大于指定大小,大于继续压缩
                baos.reset(); // 重置baos即让下一次的写入覆盖之前的内容
                options -= 5; // 图片质量每次减少5
                if (options <= 5) options = 5; // 如果图片质量小于5，为保证压缩后的图片质量，图片最底压缩质量为5
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos); // 将压缩后的图片保存到baos中
                if (options == 5) break; // 如果图片的质量已降到最低则，不再进行压缩
            }
            try {
                File thumbnailFile = getThumbnailFile(new File(imgPath));
                FileOutputStream fos = new FileOutputStream(thumbnailFile);//将压缩后的图片保存的本地上指定路径中
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                baos.flush();
                baos.close();
                bitmap.recycle();
                sendMsg(true, thumbnailFile.getPath(), null, listener);

            } catch (Exception e) {
                sendMsg(false, imgPath, "质量压缩失败", listener);
                e.printStackTrace();
            }
        });

    }

    /**
     * 按比例缩小图片的像素以达到压缩的目的
     */
    private void compressImageByPixel(String imgPath, CompressResultListener listener) throws FileNotFoundException {
        if (imgPath == null) {
            sendMsg(false, null, "要压缩的文件不存在", listener);
            return;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true; // 只读边,不读内容
        BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int width = newOpts.outWidth;
        int height = newOpts.outHeight;
        float maxSize = config.getMaxPixel();
        int be = 1;
        if (width >= height && width > maxSize) { // 缩放比,用高或者宽其中较大的一个数据进行计算
            be = (int) (newOpts.outWidth / maxSize);
            be++;
        } else if (width < height && height > maxSize) {
            be = (int) (newOpts.outHeight / maxSize);
            be++;
        }
        if (width <= config.getUnCompressNormalPixel() || height <= config.getUnCompressNormalPixel()) {
            be = 2;
            if (width <= config.getUnCompressMinPixel() || height <= config.getUnCompressMinPixel()) be = 1;
        }
        newOpts.inSampleSize = be; // 设置采样率
        newOpts.inPreferredConfig = Config.ARGB_8888; // 该模式是默认的,可不设
        newOpts.inPurgeable = true; // 同时设置才会有效
        newOpts.inInputShareable = true; // 当系统内存不够时候图片自动被回收
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        if (config.isEnableQualityCompress()) {
            compressImageByQuality(bitmap, imgPath, listener); // 压缩好比例大小后再进行质量压缩
        } else {
            File thumbnailFile = getThumbnailFile(new File(imgPath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(thumbnailFile));

            listener.onCompressSuccess(thumbnailFile.getPath());
        }
    }

    private File getThumbnailFile(File file) {
        if (file == null || !file.exists()) return file;
        return getPhotoCacheDir(file);
    }

    private File getPhotoCacheDir(File file) {
        if (TextUtils.isEmpty(config.getCacheDir())) config.setCacheDir(Constants.COMPRESS_CACHE);
        File mCacheDir = new File(Constants.BASE_CACHE_PATH
                + context.getPackageName() + "/cache", config.getCacheDir());
        Log.e("netease >>> ", mCacheDir.getAbsolutePath());
        if (!mCacheDir.mkdirs() && (!mCacheDir.exists() || !mCacheDir.isDirectory())) {
            return file;
        } else {
            return new File(mCacheDir, "compress_" + file.getName());
        }
    }

    /**
     * 发送压缩结果的消息
     *
     * @param isSuccess 压缩是否成功
     */
    private void sendMsg(final boolean isSuccess, final String imagePath, final String message, final CompressResultListener listener) {
        mhHandler.post(() -> {
            if (isSuccess) {
                listener.onCompressSuccess(imagePath);
            } else {
                listener.onCompressFailed(imagePath, message);
            }
        });
    }

}
