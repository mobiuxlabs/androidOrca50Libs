package in.mobiux.android.orca50reader;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import in.mobiux.android.orca50libs.activity.RFIDReaderBaseActivity;
//import in.mobiux.android.orca50libs.model.RFIDTag;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView text;
    private Button btnBarcode;
    private SoundPool soundPool;
    int beepShort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        btnBarcode = findViewById(R.id.btnBarcode);

        btnBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                beep();
                beepA();
            }
        });

        init(getApplicationContext());
    }

    public void beep() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
    }

    private void init(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();

        beepShort = soundPool.load(context, R.raw.beeper, 1);
    }

    private void beepA() {
        soundPool.play(beepShort, 1, 1, 0, 0, 1);
    }

//    @Override
//    public void onRfidScan(RFIDTag rfidTag) {
////        super.onRfidScan(rfidTag);
//        text.setText("" + rfidTag.getEpc());
//    }
}