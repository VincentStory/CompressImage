package com.netease.image.compress;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.netease.image.library.listener.CompressImage;

import static com.netease.image.compress.utils.ImagUtil.getImageStreamFromExternal;

/**
 * @author wangwenbo
 * @date 2019/10/25.
 * GitHub：
 * description：
 */
public class ImageActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        String path = getIntent().getStringExtra("path");
        ImageView imageView = findViewById(R.id.image);

        imageView.setImageURI(getImageStreamFromExternal(path));

        imageView.setOnClickListener(l -> {
            finish();
        });
    }
}
