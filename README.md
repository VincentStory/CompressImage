# CompressImage 可自定义压缩大小的图片压缩库

## 压缩效果对比图

![效果图](https://github.com/VincentStory/CompressImage/blob/master/example.png)


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

