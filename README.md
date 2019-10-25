# 自定义压缩图片框架

引入：
  implementation 'com.github.VincentStory:CompressImage:v1.0'

#设置图片压缩参数

  compressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000) 
                .setUnCompressNormalPixel(2000) 
                .setMaxPixel(1000) 
                .setMaxSize(100 * 1024) 
                .enablePixelCompress(true)
                .enableQualityCompress(true) 
                .enableReserveRaw(true) 
                .setCacheDir("") 
                .setShowCompressDialog(true) 
                .create();
                
                
        CompressImageManager.build(this, compressConfig, photos, new CompressImage.CompressListener() {
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
