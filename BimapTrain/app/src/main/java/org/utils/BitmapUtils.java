package org.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.IdRes;
import androidx.savedstate.SavedStateRegistryController;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: yuzzha
 * Date: 2020-11-12 13:48
 * Description:
 * Remark:
 */
public class BitmapUtils {


    /**
     * Bitmap所占用的内存 = 图片长度 x 图片宽度 x 一个像素点占用的字节数。
     * <p>
     * Bitmap计算三种情况
     * 1、getRowBytes:Since API Level 1
     * 2、getByteCount:Since API Level 12
     * 3、getAllocationByteCount():Since API Level 19
     * <p>
     * 获取bitmap的大小
     */
    public static int calcBitmapMemorySize(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) { //api 12
            return bitmap.getByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //api 19
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight(); //other version
    }

    /**
     * 读取Assets下的Bitmap
     *
     * @param mContext Context 上下文
     * @param fileName bitmap文件名
     * @return result
     */
    public static Bitmap loadAssetsBitmap(@NotNull Context mContext, @NotNull String fileName) {
        Bitmap result = null;
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open(fileName);
            result = BitmapFactory.decodeStream(inputStream, null, null);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // BitmapRegionDecoder mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        //mDecoder.decodeRegion()
        return result;
    }

    public static Bitmap loadResBitmap(@NotNull Context mContext, @IdRes int bitmapId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;            // 原图的五分之一，设置为2则为二分之一
       // Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), bitmapId, options);
        return loadResBitmap(mContext, bitmapId, options);
    }

    public static Bitmap loadResBitmap(@NotNull Context mContext, @IdRes int bitmapId, BitmapFactory.Options opts) {
        //BitmapFactory.decodeResource()
        return BitmapFactory.decodeResource(mContext.getResources(), bitmapId, opts);
    }

    public static boolean isQualityCompress(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } else {
            return false;
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(@NotNull Context mContext, int resId,int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // 先将inJustDecodeBounds设置为true不会申请内存去创建Bitmap，返回的是一个空的Bitmap，但是可以获取
        //图片的一些属性，例如图片宽高，图片类型等等。
        options.inJustDecodeBounds = true;
        Resources res =mContext.getResources();
        BitmapFactory.decodeResource(res, resId, options);
        // 计算inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 加载压缩版图片
        options.inJustDecodeBounds = false;
        // 根据具体情况选择具体的解码方法
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 压缩图片
     * 质量压缩 内存大小不变，但文件大小发生改变
     * 改变色深（bitdepth）和透明度（per-pixel alpha，而且JPG）来经行压缩，但是像素是没有改变的，不会减少bitmap占用的内存,常用于上传
     *
     * @param bitmap    被压缩的图片
     * @param sizeLimit KB 大小限制
     * @return 压缩后的图片
     */
    public static Bitmap qualityCompressBitmap(Bitmap bitmap, long sizeLimit) {
        return qualityCompressBitmap(bitmap, sizeLimit, Bitmap.CompressFormat.JPEG);
    }

    public static Bitmap qualityCompressBitmap(Bitmap bitmap, long sizeLimit, Bitmap.CompressFormat compressFormat) {
        //进行有损压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        //质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)
        // JPEG only supports opaque pixels ,png 不支持透明
        boolean reconstructed = bitmap.compress(compressFormat, quality, baos);
        if (reconstructed) {
            // 循环判断压缩后图片是否超过限制大小
            //循环判断如果压缩后图片是否大于sizeLimit,大于继续压缩
            while (baos.toByteArray().length / 1024 > sizeLimit) {
                //重置baos即让下一次的写入覆盖之前的内容
                baos.reset();
                bitmap.compress(compressFormat, quality, baos);
                quality -= 10;
            }
            return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);
        } else {
            return bitmap;
        }
    }

    /**
     * 计算inSampleSize值
     * <p>
     * 计算出在新图片需要的采样率
     * <p>
     * options.inJustDecodeBounds = true;
     * 因为我是固定来取样的数据，为什么这个压缩方法叫采样率压缩，
     * 是因为配合inJustDecodeBounds，先获取图片的宽、高【这个过程就是取样】，
     * 然后通过获取的宽高，动态的设置inSampleSize的值。
     *
     * @param options   用于获取原图的长宽
     * @param reqWidth  要求压缩后的图片宽度
     * @param reqHeight 要求压缩后的图片长度
     * @return 返回计算后的inSampleSize值
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 计算inSampleSize值
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            //origin.recycle();
        }
        return newBM;
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        //当 ratio=1，下面的 newBM 将会等价于 origin
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        //origin.recycle();
        return newBM;
    }

    /**
     * 裁剪
     *
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    private Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
        cropWidth /= 2;
        int cropHeight = (int) (cropWidth / 1.2);
        return Bitmap.createBitmap(bitmap, w / 3, 0, cropWidth, cropHeight, null, false);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        //origin.recycle();
        return newBM;
    }

    /**
     * 偏移效果
     * @param origin 原图
     * @return 偏移后的bitmap
     */
    public static Bitmap skewBitmap(Bitmap origin,float kx, float ky) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.postSkew(kx, ky);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
       // origin.recycle();
        return newBM;
    }


}
