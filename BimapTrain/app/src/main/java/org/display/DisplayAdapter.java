package org.display;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

/**
 * Author: yuzzha
 * Date: 2020-11-17 14:39
 * Description:
 * Remark:
 */
public class DisplayAdapter {

    private static final int WIDTH = 1;
    private static final int HEIGHT = 2;
    private static final float DEFAULT_WIDTH = 768f; //默认宽度
    private static final float DEFAULT_HEIGHT = 1024f; //默认高度
    private static float appDensity;
    /**
     * 字体的缩放因子，正常情况下和density相等，但是调节系统字体大小后会改变这个值
     */
    private static float appScaledDensity;
    /**
     * 状态栏高度
     */
    private static int barHeight;
    private static DisplayMetrics appDisplayMetrics;
    private static float densityScale = 1.0f;

    /**
     * application 层调用，存储默认屏幕密度
     *
     * @param application application
     */
    public static void initAppDensity(@NonNull final Application application) {
        //获取application的DisplayMetrics
        appDisplayMetrics = application.getResources().getDisplayMetrics();
        //获取状态栏高度
        barHeight = getStatusBarHeight(application);
        if (appDensity == 0) {
            //初始化的时候赋值
            appDensity = appDisplayMetrics.density;
            appScaledDensity = appDisplayMetrics.scaledDensity;

            //添加字体变化的监听
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    //字体改变后,将appScaledDensity重新赋值
                    if (newConfig != null && newConfig.fontScale > 0) {
                        appScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                }
            });
        }
    }

    /**
     * 此方法在BaseActivity中做初始化(如果不封装BaseActivity的话,直接用下面那个方法就好了)
     *
     * @param activity activity
     */
    public static void setDefault(Activity activity) {
        setAppOrientation(activity, WIDTH);
    }

    /**
     * 比如页面是上下滑动的，只需要保证在所有设备中宽的维度上显示一致即可，
     * 再比如一个不支持上下滑动的页面，那么需要保证在高这个维度上都显示一致
     *
     * @param activity    activity
     * @param orientation WIDTH HEIGHT
     */
    public static void setOrientation(Activity activity, int orientation) {
        setAppOrientation(activity, orientation);
    }

    /**
     * 重设屏幕密度
     *
     * @param activity    activity
     * @param orientation WIDTH 宽，HEIGHT 高
     */
    private static void setAppOrientation(@NonNull Activity activity, int orientation) {

        float targetDensity;

        if (orientation == HEIGHT) {
            targetDensity = (appDisplayMetrics.heightPixels - barHeight) / DEFAULT_HEIGHT;
        } else {
            targetDensity = appDisplayMetrics.widthPixels / DEFAULT_WIDTH;
        }

        float targetScaledDensity = targetDensity * (appScaledDensity / appDensity);
        int targetDensityDpi = (int) (160 * targetDensity);
        // 最后在这里将修改过后的值赋给系统参数,只修改Activity的density值
        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;

        densityScale = appDensity / targetDensity;
        setBitmapDefaultDensity(activityDisplayMetrics.densityDpi);
    }


    /**
     * 重置屏幕密度
     *
     * @param activity activity
     */
    public static void resetAppOrientation(@NonNull Activity activity) {
        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = appDensity;
        activityDisplayMetrics.scaledDensity = appScaledDensity;
        activityDisplayMetrics.densityDpi = (int) (appDensity * 160);

        densityScale = 1.0f;
        setBitmapDefaultDensity(activityDisplayMetrics.densityDpi);
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 设置 Bitmap 的默认屏幕密度
     * 由于 Bitmap 的屏幕密度是读取配置的，导致修改未被启用
     * 所有，放射方式强行修改
     * @param defaultDensity 屏幕密度
     */
    private static void setBitmapDefaultDensity(int defaultDensity) {
        //获取单个变量的值
        Class clazz;
        try {
            clazz = Class.forName("android.graphics.Bitmap");
            Field field = clazz.getDeclaredField("sDefaultDensity");
            field.setAccessible(true);
            field.set(null, defaultDensity);
            field.setAccessible(false);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 屏幕密度缩放系数
     *
     * @return 屏幕密度缩放系数
     */
    public static float getDensityScale() {
        return densityScale;
    }

}
