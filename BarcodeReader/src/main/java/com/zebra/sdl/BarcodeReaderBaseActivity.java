//-----------------------------------------------------------
// Android SDL Sample App
//
// Copyright (c) 2015 Zebra Technologies
//-----------------------------------------------------------

package com.zebra.sdl;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zebra.decoder.BarCodeReader;
import com.zebra.model.Barcode;
import com.zebra.util.BarcodeListener;
import com.zebra.util.BeeperHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import in.mobiux.android.commonlibs.activity.AppActivity;
import in.mobiux.android.commonlibs.utils.AppLogger;

import static com.zebra.sdl.BaseActivity.CAMERA_PERMISSION_CODE;
import static com.zebra.sdl.BaseActivity.STORAGE_PERMISSION_CODE;

public class BarcodeReaderBaseActivity extends AppActivity implements
        BarCodeReader.DecodeCallback, BarCodeReader.ErrorCallback, BarcodeListener {

    private static final String TAG = BarcodeReaderBaseActivity.class.getCanonicalName();

    protected Map<String, Barcode> barcodes = new HashMap<>();
    private Barcode barcode;
    private int MESSAGE_TYPE_ERROR = -1;
    private AppLogger logger;
    protected BarcodeListener barcodeListener;

    static private boolean sigcapImage = true; // true = display signature capture

    //states
    static final int STATE_IDLE = 0;
    static final int STATE_DECODE = 1;
    static final int STATE_HANDSFREE = 2;
    static final int STATE_SNAPSHOT = 4;
    static final int STATE_VIDEO = 5;


    // BarCodeReader specifics
    private BarCodeReader bcr = null;

    private boolean beepMode = true;        // decode beep enable
    private boolean snapPreview = false;        // snapshot preview mode enabled - true - calls viewfinder which gets handled by
    private int trigMode = BarCodeReader.ParamVal.LEVEL;
    private boolean atMain = false;
    private int state = STATE_IDLE;
    private int decodes = 0;

    private int motionEvents = 0;
    private int modechgEvents = 0;

    private int snapNum = 0;        //saved snapshot #
    private String decodeDataString;
    private String decodeStatString;
    private static int decCount = 0;

    //add for test
    private long mStartTime;
    private long mBarcodeCount = 0;
    private long mConsumTime;

    private boolean mKeyF4Down = false;

    static {
        try {
            System.loadLibrary("IAL");
            System.loadLibrary("SDL");

            if (android.os.Build.VERSION.SDK_INT >= 19)
                System.loadLibrary("barcodereader44"); // Android 4.4
            else if (android.os.Build.VERSION.SDK_INT >= 18)
                System.loadLibrary("barcodereader43"); // Android 4.3
            else
                System.loadLibrary("barcodereader");   // Android 2.3 - Android 4.2
            Log.i(TAG, "static initializer: Barcode library loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "static initializer: Barcode library failed or Device not Supported");
        }
    }

    // Called with the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BeeperHelper.init(this);
        barcodeListener = this;

        checkPermission(BarcodeReaderBaseActivity.this, Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.i(TAG, "onResume");
        initScanner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.i(TAG, "onPause");
        if (bcr != null) {
            logger.i(TAG, "releasing barcodeReader");
            setIdle();
            bcr.release();
            bcr = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.i(TAG, "onDestroy");
        BeeperHelper.release();
    }

    private void initScanner() {
        logger.i(TAG, "initScanner");
        state = STATE_IDLE;
        mKeyF4Down = false;
        try {

            int num = BarCodeReader.getNumberOfReaders();
            logger.i(TAG, "Numbers of reader " + num);
            dspStatus(getResources().getString(R.string.app_name) + " v" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
            if (android.os.Build.VERSION.SDK_INT >= 18) {
                bcr = BarCodeReader.open(num, getApplicationContext()); // Android 4.3 and above
                logger.i(TAG, "BarCodeReader.open Android 4.3 and above");
//                Toast.makeText(this, "Barcode device is Connected", Toast.LENGTH_SHORT).show();
            } else {
                bcr = BarCodeReader.open(num); // Android 2.3
                logger.i(TAG, "BarCodeReader.open Android 2.3");
//                Toast.makeText(this, "Barcode device is Connected 2.3", Toast.LENGTH_SHORT).show();
            }

            if (bcr == null) {
                logger.e(TAG, "BarcodeReader not connected");
                dspErr("open failed");
                barcodeListener.onConnection(false);
//                Toast.makeText(this, "Failed to Connect Barcode ", Toast.LENGTH_SHORT).show();
                return;
            }

            logger.i(TAG, "Barcode Reader connected");
            logger.i(TAG, "Barcode reader " + bcr.toString());
            bcr.setDecodeCallback(this);

            bcr.setErrorCallback(this);

            // Set parameter - Uncomment for QC/MTK platforms
            bcr.setParameter(765, 0); // For QC/MTK platforms
            bcr.setParameter(764, 3);

            bcr.setParameter(687, 4); // 4 - omnidirectional
        } catch (Exception e) {
            dspErr("open excp:" + e);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        logger.i(TAG, "onKeyDown " + keyCode + " " + event.toString());

        if (keyCode == KeyEvent.KEYCODE_F4 ||
                keyCode == KeyEvent.KEYCODE_F1 ||
                keyCode == KeyEvent.KEYCODE_F2 ||
                keyCode == KeyEvent.KEYCODE_F3) {
            if (!mKeyF4Down) {
                doDecode();
            }
            mKeyF4Down = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        logger.i(TAG, "onKeyUp " + keyCode + " " + event.toString());
        mKeyF4Down = false;
        return super.onKeyUp(keyCode, event);
    }

    //-----------------------------------------------------
    // create snapshot image screen
    private void snapScreen(Bitmap bmSnap) {
        logger.i(TAG, "snapScreen");
        atMain = false;
        setContentView(R.layout.image);
    }


    private void dspStatus(String s) {
        logger.i(TAG, "display Status Message " + s);
        barcodeListener.message(0, s);
    }

    // ----------------------------------------
    // display error msg
    private void dspErr(String s) {
        logger.i(TAG, "dspErr " + s);
//        tvStat.setText("ERROR" + s);
        barcodeListener.message(MESSAGE_TYPE_ERROR, s);
    }

    // ----------------------------------------
    // display status string
    private void dspData(String s) {
//        tvData.setText(s);
        logger.i(TAG, "dspData " + s);
        barcodeListener.message(0, s);
    }

    // -----------------------------------------
    private void beep() {
        logger.i(TAG, "beep()");
//        if (session.getBooleanValue(session.KEY_BEEP)) {
        BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
//        }
    }

// ==== SDL methods =====================

    // ----------------------------------------
    private boolean isHandsFree() {
        return (trigMode == BarCodeReader.ParamVal.HANDSFREE);
    }

    // ----------------------------------------
    private boolean isAutoAim() {
        return (trigMode == BarCodeReader.ParamVal.AUTO_AIM);
    }

    // ----------------------------------------
    // reset Level trigger mode
    void resetTrigger() {
        logger.i(TAG, "resetTrigger");
        doSetParam(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
        trigMode = BarCodeReader.ParamVal.LEVEL;
    }


    // ----------------------------------------
    // set param
    private int doSetParam(int num, int val) {
        logger.i(TAG, "doSetParam " + num);
        String s = "";
        int ret = bcr.setParameter(num, val);
        if (ret != BarCodeReader.BCR_ERROR) {
            if (num == BarCodeReader.ParamNum.PRIM_TRIG_MODE) {
                trigMode = val;
                if (val == BarCodeReader.ParamVal.HANDSFREE) {
                    s = "HandsFree";
                } else if (val == BarCodeReader.ParamVal.AUTO_AIM) {
                    s = "AutoAim";
                    ret = bcr.startHandsFreeDecode(BarCodeReader.ParamVal.AUTO_AIM);
                    if (ret != BarCodeReader.BCR_SUCCESS) {
                        dspErr("AUtoAIm start FAILED");
                    }
                } else if (val == BarCodeReader.ParamVal.LEVEL) {
                    s = "Level";
                }
            } else if (num == BarCodeReader.ParamNum.IMG_VIDEOVF) {
                if (snapPreview = (val == 1))
                    s = "SnapPreview";
            }
        } else
            s = " FAILED (" + ret + ")";

        dspStatus("Set #" + num + " to " + val + " " + s);
        return ret;
    }

    // ----------------------------------------
    // start a decode session
    private void doDecode() {
        barcodeListener.onScanStatus(true);
        logger.i(TAG, "doDecode()");
        if (setIdle() != STATE_IDLE) {
            barcodeListener.onScanStatus(false);
            return;
        }
        state = STATE_DECODE;
        decCount = 0;
        decodeDataString = new String("");
        decodeStatString = new String("");
        dspData("");
        dspStatus(getResources().getString(R.string.decoding));
        try {
            mStartTime = System.currentTimeMillis();
            bcr.startDecode(); // start decode (callback gets results)
        } catch (Exception e) {
            dspErr("open excp:" + e);
        }

    }

    // ----------------------------------------
    // BarCodeReader.DecodeCallback override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        logger.i(TAG, "onDecodeComplete() " + symbology + " " + length + " " + Arrays.toString(data) + " " + reader.toString());
        if (state == STATE_DECODE)
            state = STATE_IDLE;

        // Get the decode count
        if (length == BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT)
            decCount = symbology;

        if (length > 0) {
            if (isHandsFree() == false && isAutoAim() == false)
                bcr.stopDecode();

            ++decodes;

            // signature capture
            if (symbology == 0x69) {
                logger.i(TAG, "symbology == 0x69");
                if (sigcapImage) {
                    Bitmap bmSig = null;
                    int scHdr = 6;
                    if (length > scHdr)
                        bmSig = BitmapFactory.decodeByteArray(data, scHdr, length - scHdr);

                    if (bmSig != null)
                        snapScreen(bmSig);

                    else
                        dspErr("OnDecodeComplete: SigCap no bitmap");
                }
                decodeStatString += new String("[" + decodes + "] type: " + symbology + " len: " + length);
                decodeDataString += new String(data);

                barcode = new Barcode();
                barcode.setName(decodeDataString);

                barcodeListener.onBarcodeScan(barcode.getName());

                logger.i(TAG, "barcode " + decodeDataString);

                mBarcodeCount++;
                long consum = System.currentTimeMillis() - mStartTime;
                mConsumTime += consum;
                decodeDataString += "\n\r" + "Time spent this time:" + consum + "millisecond" + "\n\r" + "Avg Speed:" + (mConsumTime / mBarcodeCount) + "Milliseconds/each";

                logger.i(TAG, "barcode " + decodeDataString);

				/*try {
					decodeDataString += new String(data,charsetName(data));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
            } else {

                //type 99?
                if (symbology == 0x99) {
                    logger.i(TAG, "symbology == 0x99 ");
                    symbology = data[0];
                    int n = data[1];
                    int s = 2;
                    int d = 0;
                    int len = 0;
                    byte d99[] = new byte[data.length];
                    for (int i = 0; i < n; ++i) {
                        s += 2;
                        len = data[s++];
                        System.arraycopy(data, s, d99, d, len);
                        s += len;
                        d += len;
                    }
                    d99[d] = 0;
                    data = d99;
                }

                Log.d("012", "ret=" + byte2hex(data));
                decodeStatString += new String("[" + decodes + "] type: " + symbology + " len: " + length);
                decodeDataString += new String(data);

                barcode = new Barcode();
                barcode.setName(decodeDataString);
                barcodeListener.onBarcodeScan(barcode.getName());
                logger.i(TAG, "Barcode - " + decodeDataString);
                //add for test speed
                mBarcodeCount++;
                long consum = System.currentTimeMillis() - mStartTime;
                mConsumTime += consum;
                decodeDataString += "\n\r" + "Time spent this time:" + consum + "millisecond" + "\n\r" + "Average speed:" + (mConsumTime / mBarcodeCount) + "Milliseconds/each";
				/*try {
					decodeDataString += new String(data,charsetName(data));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
                dspStatus(decodeStatString);
                dspData(decodeDataString);


                logger.i(TAG, "Barcode - " + decodeDataString);

                if (decCount > 1) // Add the next line only if multiple decode
                {
                    decodeStatString += new String(" ; ");
                    decodeDataString += new String(" ; ");
                } else {
                    decodeDataString = new String("");
                    decodeStatString = new String("");
                }

                logger.i(TAG, "decodeStatString - " + decodeStatString);
                logger.i(TAG, "decodeDataString - " + decodeDataString);
            }

            if (beepMode)
                beep();
        } else {
            // no-decode
            dspData("");
            switch (length) {
                case BarCodeReader.DECODE_STATUS_TIMEOUT:
                    dspStatus("decode timed out");
                    break;

                case BarCodeReader.DECODE_STATUS_CANCELED:
                    dspStatus("decode cancelled");
                    break;

                case BarCodeReader.DECODE_STATUS_ERROR:
                default:
                    dspStatus("decode failed");
                    break;
            }
        }
        mKeyF4Down = false;

        barcodeListener.onScanStatus(false);
    }

    private String byte2hex(byte[] buffer) {
        logger.i(TAG, "byte2hex()");

        String h = "";

        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }

        logger.i(TAG, "Hex is " + h);
        return h;
    }

    //------------------------------------------
    private int setIdle() {
        logger.i(TAG, "setIdle()");
        int prevState = state;
        int ret = prevState;        //for states taking time to chg/end

        state = STATE_IDLE;
        switch (prevState) {
            case STATE_HANDSFREE:
                resetTrigger();
                //fall thru
            case STATE_DECODE:
                dspStatus("decode stopped");
                bcr.stopDecode();
                break;

            case STATE_VIDEO:
                bcr.stopPreview();
                break;

            case STATE_SNAPSHOT:
                ret = STATE_IDLE;
                break;

            default:
                ret = STATE_IDLE;
        }
        return ret;
    }

    // ----------------------------------------
    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {

        logger.i(TAG, "onEvent() " + event + " " + info + " " + Arrays.toString(data) + " " + reader.toString());

        switch (event) {
            case BarCodeReader.BCRDR_EVENT_SCAN_MODE_CHANGED:
                ++modechgEvents;
                dspStatus("Scan Mode Changed Event (#" + modechgEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_MOTION_DETECTED:
                ++motionEvents;
                dspStatus("Motion Detect Event (#" + motionEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_SCANNER_RESET:
                dspStatus("Reset Event");
                break;

            default:
                // process any other events here
                break;
        }

        mKeyF4Down = false;
    }


    public void onError(int error, BarCodeReader reader) {
        // TODO Auto-generated method stub

        logger.e(TAG, "onError() " + error + " " + reader.toString());
        barcodeListener.message(error, reader.toString());
    }

    // Function to check and request permission.
    public void checkPermission(BarcodeReaderBaseActivity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(BarcodeReaderBaseActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(BarcodeReaderBaseActivity.this,
                    new String[]{permission},
                    requestCode);
        } else {
            logger.i(TAG, "Permission already granted");
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        R.string.camera_permission_granted,
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this,
                        R.string.camera_permission_denied,
                        Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logger.i(TAG, getResources().getString(R.string.storage_permission_granted));
            } else {
                logger.e(TAG, getResources().getString(R.string.storage_permission_denied));
            }
        }
    }

    @Override
    public void onBarcodeScan(String barcode) {
        logger.i(TAG, "barcode scanned " + barcode);
        Barcode b = new Barcode();
        b.setName(barcode);
        barcodes.put(barcode, b);
    }

    @Override
    public void onScanStatus(boolean status) {
        logger.i(TAG, "Scanning status " + status);
    }

    @Override
    public void onConnection(boolean status) {
        logger.i(TAG, "Connection Status is " + status);
    }

    @Override
    public void message(int type, String msg) {
        logger.i(TAG, "reader message is " + type + " " + msg);
    }
}//end-class
