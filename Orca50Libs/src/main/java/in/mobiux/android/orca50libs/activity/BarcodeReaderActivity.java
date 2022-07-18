package in.mobiux.android.orca50libs.activity;

import android.os.Bundle;

import com.zebra.sdl.BarcodeReaderBaseActivity;

import in.mobiux.android.orca50libs.R;

public class BarcodeReaderActivity extends BarcodeReaderBaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
