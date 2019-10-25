#可自定义压缩大小的图片压缩库

#压缩对比图

![效果图](https://github.com/VincentStory/CompressImage/blob/master/example.png)


引入：

  implementation 'com.github.VincentStory:CompressImage:v1.0'


#设置图片压缩参数

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
                
  #压缩图片方法及回调
                
      CompressImageManager.builder(this)
                .config(compressConfig)
                .loadPhtos(photos)
                .setCompressListener(new CompressImage.CompressListener() {
                    @Override
                    public void onCompressSuccess(ArrayList<Photo> arrayList) {
                       //成功后回调 ，返回压缩后的图片对象集合
                        Log.e("netease >>> ", "压缩成功" + arrayList.get(0).getCompressPath());
                    }

                    @Override
                    public void onCompressFailed(ArrayList<Photo> images, String error) {
                        //当压缩过程出现问题时调用
                        Log.e("netease >>> ", error);
                        if (dialog != null && !isFinishing()) {
                            dialog.dismiss();
                        }
                    }
                }).compress();
