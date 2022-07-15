package in.mobiux.android.orca50libs.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import in.mobiux.android.orca50libs.R;


/**
 * @author naz
 * Email 961057759@qq.com
 * Date 2020/4/13
 */
public class BeeperHelper {
    /**
     * 声音资源类型
     */
    public final static int SOUND_FILE_TYPE_NORMAL = 1;
    public final static int SOUND_FILE_TYPE_SHORT = 2;

    @IntDef({SOUND_FILE_TYPE_NORMAL, SOUND_FILE_TYPE_SHORT})
    @Retention(RetentionPolicy.SOURCE)
    @interface SOUND_FILE_TYPE {
    }

    /**
     * 发音类型
     */
    public final static int BEEPER_TYPE_QUIET = 1;
    public final static int BEEPER_TYPE_INVENTORIED = 2;
    public final static int BEEPER_TYPE_PER_TAG = 3;

    @IntDef({BEEPER_TYPE_QUIET, BEEPER_TYPE_INVENTORIED, BEEPER_TYPE_PER_TAG})
    @Retention(RetentionPolicy.SOURCE)
    @interface BEEPER_TYPE {
    }

    private static SoundPool mSoundPool;
    private static int mBeeperType;

    /**
     * 初始化
     *
     * @param context Context
     */
    public static void init(Context context) {
        mBeeperType = BEEPER_TYPE_INVENTORIED;
        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build())
                    .setMaxStreams(1)
                    .build();
            mSoundPool.load(context, R.raw.beeper, SOUND_FILE_TYPE_NORMAL);
            mSoundPool.load(context, R.raw.beeper_short, SOUND_FILE_TYPE_SHORT);
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 1);
            mSoundPool.load(context, R.raw.beeper, SOUND_FILE_TYPE_NORMAL);
            mSoundPool.load(context, R.raw.beeper_short, SOUND_FILE_TYPE_SHORT);
        }
    }


    /**
     * 发声
     *
     * @param soundFileType 类型{@link SOUND_FILE_TYPE}
     */
    public static void beep(@SOUND_FILE_TYPE int soundFileType) {
        if (mSoundPool == null) {
            return;
        }
        if (mBeeperType == BEEPER_TYPE_PER_TAG) {
            if (soundFileType == SOUND_FILE_TYPE_NORMAL) {
//                mSoundPool.play(SOUND_FILE_TYPE_NORMAL, 1, 1, 0, 0, 1);
            } else {
                mSoundPool.play(SOUND_FILE_TYPE_SHORT, 1, 1, 0, 0, 1);
            }
        } else if (mBeeperType == BEEPER_TYPE_INVENTORIED) {
            if (soundFileType == SOUND_FILE_TYPE_NORMAL) {
                mSoundPool.play(SOUND_FILE_TYPE_NORMAL, 1, 1, 0, 0, 1);
            }
        }
    }

    public static void beep() {
        beep(SOUND_FILE_TYPE_SHORT);
    }

    /**
     * 设置蜂鸣类型
     *
     * @param beeperType {@link BEEPER_TYPE}
     */
    public static void setBeeperType(@BEEPER_TYPE int beeperType) {
        mBeeperType = beeperType;
    }

    /**
     * 获取蜂鸣类型
     *
     * @return Type
     */
    public static int getBeeperType() {
        return mBeeperType;
    }

    /**
     * 释放资源
     */
    public static void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
