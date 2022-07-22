package in.mobiux.android.orca50libs.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import in.mobiux.android.orca50libs.R;

public class BeeperHelper {

    public final static int SOUND_FILE_TYPE_NORMAL = 1;
    public final static int BEEPER_TYPE_PER_TAG = 1;

    private static SoundPool mSoundPool;
    private static int beepShort;

    public static void init(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        mSoundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        beepShort = mSoundPool.load(context, R.raw.beeper, 1);
    }


    public static void beep() {
        mSoundPool.play(beepShort, 1, 1, 0, 0, 1);
    }

    public static void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
