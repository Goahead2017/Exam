package com.bignerdranch.android.ourcqupt.ImageToLoad;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * 实现图片压缩
 */

public class ImageResizer {

    private static final String TAG = "ImageResizer";

    private static class SingletonHolder{
        private static final ImageResizer INSTANCE = new ImageResizer();
    }

    private ImageResizer(){
    }

    public static ImageResizer getInstance(){
        return SingletonHolder.INSTANCE;
    }

    //获取采样率
    public Bitmap decodeSampledBitmapFromResource(Resources res,int resId,int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //轻量级操作，当设置为true是，BitmapFactory只会解析图片的原始宽/高信息，并不会去真正加载图片
        options.inJustDecodeBounds = true;
        //加载图片
        BitmapFactory.decodeResource(res,resId,options);
        //根据采样率的规则并结合目标View的所需大小计算出采样率inSampleSize
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);

        options.inJustDecodeBounds = false;
        //重新加载图片
        return BitmapFactory.decodeResource(res,resId,options);
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd,int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);

        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(reqWidth == 0 || reqHeight == 0){
            return 1;
        }

        //获取原始图片的宽/高
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG,"origin,w=" + width + "h=" + height);
        int inSampleSize =  1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height / 2;
            final int halfWidth =  width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }
        Log.d(TAG,"sampleSize:" + inSampleSize);
        return inSampleSize;
    }

}
