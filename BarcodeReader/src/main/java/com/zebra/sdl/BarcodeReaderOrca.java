package com.zebra.sdl;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.zebra.decoder.BarCodeReader;
import com.zebra.util.BeeperHelper;

import in.mobiux.android.commonlibs.utils.AppLogger;


//this is NOT working
//todo
public class BarcodeReaderOrca implements
        BarCodeReader.DecodeCallback, BarCodeReader.ErrorCallback, KeyEvent.Callback {

    private static final String TAG = BarcodeReaderOrca.class.getCanonicalName();
    private Context context;
    private BarCodeReader bcr;

    private AppLogger logger;

    static {
        System.loadLibrary("IAL");
        System.loadLibrary("SDL");

        if (android.os.Build.VERSION.SDK_INT >= 19)
            System.loadLibrary("barcodereader44"); // Android 4.4
        else if (android.os.Build.VERSION.SDK_INT >= 18)
            System.loadLibrary("barcodereader43"); // Android 4.3
        else
            System.loadLibrary("barcodereader");   // Android 2.3 - Android 4.2

        Log.i(TAG, "Library loaded Successfully");
    }

    public BarcodeReaderOrca(Context context) {
        this.context = context;

        logger = AppLogger.getInstance();
        BeeperHelper.init(context);
    }

    private void connect() {
//        initScanner();
    }

//    private void initScanner() {
//        logger.i(TAG, "initScanner");
//        state = STATE_IDLE;
//        mKeyF4Down = false;
//        try {
//
//            int num = BarCodeReader.getNumberOfReaders();
//            logger.i(TAG, "Numbers of reader " + num);
//            dspStat(getResources().getString(R.string.app_name) + " v" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
//            if (android.os.Build.VERSION.SDK_INT >= 18) {
//                bcr = BarCodeReader.open(num, getApplicationContext()); // Android 4.3 and above
//                logger.i(TAG, "BarCodeReader.open Android 4.3 and above");
//            } else {
//                bcr = BarCodeReader.open(num); // Android 2.3
//                logger.i(TAG, "BarCodeReader.open Android 2.3");
//            }
//
//            if (bcr == null) {
//
//                logger.e(TAG, "BarcodeReader not connected");
//                dspErr("open failed");
//                return;
//            }
//
//            logger.i(TAG, "Barcode Reader connected");
//            logger.i(TAG, "Barcode reader " + bcr.toString());
//            bcr.setDecodeCallback(this);
//
//            bcr.setErrorCallback(this);
//
//            // Set parameter - Uncomment for QC/MTK platforms
//            bcr.setParameter(765, 0); // For QC/MTK platforms
//            bcr.setParameter(764, 3);
//            //bcr.setParameter(137, 0);
//            //bcr.setParameter(8610, 1);
////			 Parameters  param = bcr.getParameters();
////			 Log.d("012", "scan_h:"+bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES)+", cam_h:"+param.getPreviewSize().height);
////			 if(bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES) != param.getPreviewSize().height){
////				 param.setPreviewSize(1360, bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES));
////				 param.setPreviewSize(1360,960);
////				 bcr.setParameters(param);
////			 }
//
//            // Sample of how to setup OCR Related String Parameters
//            // OCR Parameters
//            // Enable OCR-B
//            //bcr.setParameter(681, 1);
//
//            // Set OCR templates
//            //String OCRSubSetString = "01234567890"; // Only numeric characters
//            //String OCRSubSetString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!%"; // Only numeric characters
//            // Parameter # 686 - OCR Subset
//            //bcr.setParameter(686, OCRSubSetString);
//
//            //String OCRTemplate = "54R"; // The D ignores all characters after the template
//            // Parameter # 547 - OCR Template
//            //bcr.setParameter(547, OCRTemplate);
//            // Parameter # 689 - OCR Minimum characters
//            //bcr.setParameter(689, 13);
//            // Parameter # 690 - OCR Maximum characters
//            //bcr.setParameter(690, 13);
//
//            // Set Orientation
//            bcr.setParameter(687, 4); // 4 - omnidirectional
//
//            // Sets OCR lines to decide
//            //bcr.setParameter(691, 2); // 2 - OCR 2 lines
//
//            // End of OCR Parameter Sample
////			BarCodeReader.ReaderInfo readinfo = new BarCodeReader.ReaderInfo();
////			BarCodeReader.getReaderInfo(0, readinfo);
////			Log.e("012", "face:"+readinfo.facing);
//        } catch (Exception e) {
//            dspErr("open excp:" + e);
//        }
//    }

    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onError(int error, BarCodeReader reader) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "key is pressed " + keyCode);
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        logger.i(TAG, "onKeyDown " + keyCode + " " + event.toString());
//
//        if (keyCode == KeyEvent.KEYCODE_F4 ||
//                keyCode == KeyEvent.KEYCODE_F1 ||
//                keyCode == KeyEvent.KEYCODE_F2 ||
//                keyCode == KeyEvent.KEYCODE_F3) {
//            if (!mKeyF4Down) {
//                doDecode();
//            }
//            mKeyF4Down = true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        logger.i(TAG, "onKeyUp " + keyCode + " " + event.toString());
//        mKeyF4Down = false;
//        return super.onKeyUp(keyCode, event);
//    }
}
