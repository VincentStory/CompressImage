package com.netease.image.compress;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.MessagePattern;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.image.compress.utils.ImagUtil;
import com.netease.image.compress.utils.UriParseUtils;
import com.netease.image.library.CompressImageManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.netease.image.library.bean.Photo;
import com.netease.image.library.config.CompressConfig;
import com.netease.image.library.listener.CompressImage;
import com.netease.image.library.utils.CachePathUtils;
import com.netease.image.library.utils.CommonUtils;
import com.netease.image.library.utils.Constants;

import static com.netease.image.compress.utils.ImagUtil.getImageStreamFromExternal;

public class MainActivity extends AppCompatActivity {

    private CompressConfig compressConfig; // 压缩配置
    private ProgressDialog dialog; // 压缩加载框
    private String cameraCachePath; // 拍照源文件路径

    private ImageView image1;
    private ImageView image2;
    private TextView tv1, tv2, tv3, tv4;
    private String mPath1, mPath2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv4 = findViewById(R.id.tv4);

        image1.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivity.this, ImageActivity.class);
            intent.putExtra("path", mPath1);
            if (mPath1 != null)
                startActivity(intent);

        });
        image2.setOnClickListener(l -> {
            Intent intent = new Intent(MainActivity.this, ImageActivity.class);
            intent.putExtra("path", mPath2);
            if (mPath2 != null)
                startActivity(intent);

        });

        compressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000) // 最小像素不压缩，默认值：1000
                .setUnCompressNormalPixel(2000) // 标准像素不压缩，默认值：2000
                .setMaxPixel(1000) // 长或宽不超过的最大像素 (单位px)，默认值：1200
                .setMaxSize(100 * 1024) // 压缩到的最大大小 (单位B)，默认值：200 * 1024 = 200KB
                .enablePixelCompress(true) // 是否启用像素压缩，默认值：true
                .enableQualityCompress(true) // 是否启用质量压缩，默认值：true
                .enableReserveRaw(true) // 是否保留源文件，默认值：true
                .setCacheDir("") // 压缩后缓存图片路径，默认值：Constants.COMPRESS_CACHE
                .setShowCompressDialog(true) // 是否显示压缩进度条，默认值：false
                .create();
//        compressConfig = CompressConfig.getDefaultConfig();
    }


    // 点击拍照
    public void camera(View view) {
        // FileProvider
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        cameraCachePath = file.getAbsolutePath();
        // 启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    // 点击相册
    public void album(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拍照返回
        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = ImagUtil.getCompressBitmap(cameraCachePath);
            mPath1 = cameraCachePath;
            image1.setImageBitmap(bitmap);

            getString(cameraCachePath, 1);

            // 压缩（集合？单张）
            preCompress(cameraCachePath);
        }

        // 相册返回
        if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                image1.setImageURI(uri);

                mPath1 = UriParseUtils.getPath(this, uri);
                getString(mPath1, 1);

                // 压缩（集合？单张）
                preCompress(mPath1);


            }
        }
    }

    // 准备压缩，封装图片集合
    private void preCompress(String photoPath) {
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo(photoPath));
        if (!photos.isEmpty()) compress(photos);
    }


    // 开始压缩
    private void compress(ArrayList<Photo> photos) {
        if (compressConfig.isShowCompressDialog()) {
            Log.e("netease >>> ", "开启了加载框");
            dialog = CommonUtils.showProgressDialog(this, "压缩中……");
        }


        CompressImageManager.builder(this)
                .Config(compressConfig)
                .loadPhtos(photos)
                .setCompressListener(new CompressImage.CompressListener() {
                    @Override
                    public void onCompressSuccess(ArrayList<Photo> arrayList) {
                        Log.e("netease >>> ", "压缩成功" + arrayList.get(0).getCompressPath());
                        mPath2 = arrayList.get(0).getCompressPath();
                        image2.setImageURI(getImageStreamFromExternal(mPath2));
                        getString(mPath2, 2);
                        if (dialog != null && !isFinishing()) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCompressFailed(ArrayList<Photo> images, String error) {
                        Log.e("netease >>> ", error);
                        if (dialog != null && !isFinishing()) {
                            dialog.dismiss();
                        }
                    }
                }).compress();


    }


    public void getString(String photoPath, int type) {


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        //照片长度
        String photoLength = String.valueOf(options.outHeight);

        //照片宽度
        String photoWidth = String.valueOf(options.outWidth);

        if (type == 1)
            tv1.setText("图片像素:" + photoLength + "*" + photoWidth);
        else
            tv3.setText("图片像素:" + photoLength + "*" + photoWidth);
        File f = new File(photoPath);
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(f);
            //照片大小
            float size = fis.available() / 1000;
            String photoSize = size + "KB";
            if (type == 1)
                tv2.setText("图片大小:" + photoSize);
            else
                tv4.setText("图片大小:" + photoSize);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
