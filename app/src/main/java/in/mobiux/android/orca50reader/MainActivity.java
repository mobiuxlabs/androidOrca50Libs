package in.mobiux.android.orca50reader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import in.mobiux.android.orca50libs.activity.RFIDReaderBaseActivity;
import in.mobiux.android.orca50libs.model.RFIDTag;


public class MainActivity extends RFIDReaderBaseActivity {

    private static final String TAG = "MainActivity";
    private TextView text;
    private Button btnBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        btnBarcode = findViewById(R.id.btnBarcode);

        btnBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRfidScan(RFIDTag rfidTag) {
//        super.onRfidScan(rfidTag);

        text.setText("" + rfidTag.getEpc());
        Toast.makeText(this, "" + rfidTag.getEpc(), Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onRFIDScanEnd(RFIDTag.InventoryTagEnd tagEnd) {
        super.onRFIDScanEnd(tagEnd);
        Toast.makeText(this, "RFID Scan End", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "On Scan End : totat tag count is : " + tagEnd.mTagCount, Toast.LENGTH_SHORT).show();
    }
}