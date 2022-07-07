package com.netease.image.compress.app;

import android.app.Application;

import com.previewlibrary.ZoomMediaLoader;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZoomMediaLoader.getInstance().init(new TestImageLoader());
    }
}
