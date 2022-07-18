package in.mobiux.android.orca50libs.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import in.mobiux.android.orca50libs.model.OperationTag;
import in.mobiux.android.orca50libs.model.RFIDTag;
import in.mobiux.android.orca50libs.rfid.core.RFIDReader;
import in.mobiux.android.orca50libs.rfid.core.RFIDReaderListener;
import in.mobiux.android.orca50libs.rfid.core.Reader;

public class RFIDReaderBaseActivity extends BaseActivity implements RFIDReaderListener {

    private static final String TAG = "RFIDReaderBaseActivity";

    private RFIDReader rfidReader;
    protected Map<String, RFIDTag> tags = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.i(TAG, "onResume");

        rfidReader = new RFIDReader(getApplicationContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        rfidReader.setOnRFIDReaderListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.i(TAG, "onPause");

        rfidReader.releaseResources();
        rfidReader.unregisterListener(this);
    }


    @Override
    public void onScanningStatus(boolean status) {
        logger.i(TAG, "Scanning Status " + status);
    }

    @Override
    public void onRfidScan(RFIDTag rfidTag) {
        logger.i(TAG, "");
        tags.put(rfidTag.getEpc(), rfidTag);
    }

    @Override
    public void onOperationTag(OperationTag operationTag) {
        logger.i(TAG, "onOperationTag " + operationTag.strEPC);
    }

    @Override
    public void onRFIDScanEnd(RFIDTag.InventoryTagEnd tagEnd) {
        logger.i(TAG, "Scan End " + tagEnd.mTagCount);
    }

    @Override
    public void onConnection(boolean status) {
        logger.i(TAG, "Connection Status " + status);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            startScan();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4) {
            startScan();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public void startScan() {
        rfidReader.startScan();
    }


    private Timer timer = new Timer();
    private final long DELAY = 1000;

    public void setRFOutputPower(int rssi) {

        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rfidReader.setRFOutputPower(rssi);
            }
        }, DELAY);
    }
}
