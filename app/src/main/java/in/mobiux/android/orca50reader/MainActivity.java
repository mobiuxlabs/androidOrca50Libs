package in.mobiux.android.orca50reader;

import android.os.Bundle;
import android.widget.TextView;

import in.mobiux.android.orca50libs.model.RFIDTag;
import in.mobiux.android.orca50libs.rfid.RFIDReaderBaseActivity;

public class MainActivity extends RFIDReaderBaseActivity {

    private static final String TAG = "MainActivity";
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
    }

    @Override
    public void onRfidScan(RFIDTag rfidTag) {
//        super.onRfidScan(rfidTag);

        text.setText("" + rfidTag.getEpc());


    }
}