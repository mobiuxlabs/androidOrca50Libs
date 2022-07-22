package in.mobiux.android.orca50reader;

import android.os.Bundle;
import android.widget.TextView;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import in.mobiux.android.commonlibs.activity.AppActivity;
//import in.mobiux.android.orca50libs.activity.BarcodeReaderActivity;


public class BarcodeActivity extends BarcodeReaderBaseActivity {

    private TextView tvBarcode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        tvBarcode = findViewById(R.id.tvBarcode);
    }

    @Override
    public void onBarcodeScan(String barcode) {
//        super.onBarcodeScan(barcode);
        tvBarcode.setText(barcode);
    }
}