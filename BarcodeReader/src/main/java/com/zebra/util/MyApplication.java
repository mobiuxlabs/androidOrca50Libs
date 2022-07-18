package com.zebra.util;

import android.media.MediaPlayer;
import android.os.Handler;

import com.zebra.sdl.R;

import in.mobiux.android.commonlibs.utils.App;
import in.mobiux.android.commonlibs.utils.AppLogger;
import in.mobiux.android.commonlibs.utils.SessionManager;


/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
public class MyApplication extends App {


    private String TAG = MyApplication.class.getName();

    private MediaPlayer mediaPlayer;
    private AppLogger logger;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        logger.i(TAG, "### App Terminated ###");
        System.exit(0);
    }
}
