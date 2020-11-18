package com.train.bimap;

import android.app.Application;

import org.display.DisplayAdapter;

/**
 * Author: yuzzha
 * Date: 2020-11-17 14:42
 * Description:
 * Remark:
 */
public class BitmapTrianApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayAdapter.initAppDensity(this);
    }
}
