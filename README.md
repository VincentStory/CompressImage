# CompressImage 可自定义压缩大小的图片压缩库

## 压缩效果对比图

![效果图](https://github.com/VincentStory/CompressImage/blob/master/example.png)
![效果图](https://github.com/VincentStory/CompressImage/blob/master/Screenshot_2021-11-24-10-56-22-023_com.net.image..jpg)

## 核心代码
```

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


```


# 使用方法
## 添加依赖
Step 1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

``` 
Step 2. Add the dependency

```
dependencies {
	        implementation 'com.github.VincentStory:CompressImage:v1.0'
	    }

``` 
## 方法名
setUnCompressMinPixel()  设置最小不压缩像素值 默认值：1000 <br>
setUnCompressNormalPixel() 标准像素不压缩，默认值：2000  <br>
setMaxPixel() 长或宽不超过的最大像素 (单位px)，默认值：1200 <br>
setMaxSize()   压缩到的最大大小 (单位B)，默认值：200 * 1024 = 200KB <br>
enablePixelCompress()   是否启用像素压缩，默认值：true <br>
enableQualityCompress()   是否启用质量压缩，默认值：true <br>
enableReserveRaw()  是否保留源文件，默认值：true <br>
setCacheDir() 压缩后缓存图片路径，默认值：Constants.COMPRESS_CACHE <br>
setShowCompressDialog()  是否显示压缩进度条，默认值：false <br>
loadPhtos() 放入要压缩的图片集合  <br>
setCompressListener() 设置压缩图片回调监听方法 <br>
compress()  开始压缩 <br>


 ## 压缩图片方法及回调
 ```
        CompressImageManager.builder(this)
                .setUnCompressMinPixel(1000) 
                .setUnCompressNormalPixel(2000) 
                .setMaxPixel(1000) 
                .setMaxSize(100 * 1024) 
                .enablePixelCompress(true) 
                .enableQualityCompress(true) 
                .enableReserveRaw(true) 
                .setCacheDir("") 
                .setShowCompressDialog(true) 
                .loadPhtos(photos)
                .setCompressListener(new CompressImage.CompressListener() {
                    @Override
                    public void onCompressSuccess(ArrayList<Photo> arrayList) {
                        Log.e("netease >>> ", "压缩成功" + arrayList.get(0).getCompressPath());
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
```

-----------------------完成--------------------

如果对你有所帮助欢迎Star或者Fork,有遇到其他问题可以加我VX进行沟通 VX：459005147 备注：android交流

