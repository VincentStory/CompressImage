package com.netease.image.compress.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.netease.image.compress.MainActivity;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author wangwenbo
 * @date 2019/10/25.
 * GitHub：
 * description：
 */
public class ImagUtil {
    public static Uri getImageStreamFromExternal(String imagePath) {


        File picPath = new File(imagePath);
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }

        return uri;
    }

    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                new String[]{path.substring(path.lastIndexOf("/") + 1)},
                null);

        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }

    /**
     * 生成bitmap数据
     *
     * @param path
     * @return
     */
    public static Bitmap getCompressBitmap(String path) {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

}
