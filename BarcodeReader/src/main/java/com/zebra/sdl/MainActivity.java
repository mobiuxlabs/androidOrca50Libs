//-----------------------------------------------------------
// Android SDL Sample App
//
// Copyright (c) 2015 Zebra Technologies
//-----------------------------------------------------------

package com.zebra.sdl;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.adapter.BarcodeAdapter;
import com.zebra.decoder.BarCodeReader;
import com.zebra.model.Barcode;
import com.zebra.util.AppUtils;
import com.zebra.util.BeeperHelper;

import org.mozilla.universalchardet.CharsetListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.mobiux.android.commonlibs.utils.AppLogger;
import in.mobiux.android.commonlibs.utils.pdf.PdfUtils;

import static com.zebra.sdl.BaseActivity.CAMERA_PERMISSION_CODE;
import static com.zebra.sdl.BaseActivity.STORAGE_PERMISSION_CODE;


public class MainActivity extends Activity implements
        BarCodeReader.DecodeCallback, BarCodeReader.ErrorCallback {


    private static final String TAG = MainActivity.class.getCanonicalName();

    private Button btnSave, btnClear, btnPrint;
    private TextView tvCount, txtIndicator;
    private RecyclerView recyclerView;


    private List<Barcode> barcodes = new ArrayList<>();
    private Map<String, Barcode> map = new HashMap<>();
    private BarcodeAdapter adapter;
    private Barcode barcode;
    private AppLogger logger;


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


    // ------------------------------------------------------
    // Called with the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = AppLogger.getInstance();
        logger.i(TAG, "Activity Created");
        mainScreen();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        logger = AppLogger.getInstance();

        BeeperHelper.init(this);

        checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
    }

    // ------------------------------------------------------
    // Called when the activity is about to start interacting with the user.
    @Override
    protected void onResume() {
        super.onResume();
        logger.i(TAG, "onResume");
        initScanner();
    }

    //-----------------------------------------------------
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
            dspStat(getResources().getString(R.string.app_name) + " v" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
            if (android.os.Build.VERSION.SDK_INT >= 18) {
                bcr = BarCodeReader.open(num, getApplicationContext()); // Android 4.3 and above
                logger.i(TAG, "BarCodeReader.open Android 4.3 and above");
                Toast.makeText(this, "Barcode device is Connected", Toast.LENGTH_SHORT).show();
            } else {
                bcr = BarCodeReader.open(num); // Android 2.3
                logger.i(TAG, "BarCodeReader.open Android 2.3");
                Toast.makeText(this, "Barcode device is Connected 2.3", Toast.LENGTH_SHORT).show();
            }

            if (bcr == null) {

                logger.e(TAG, "BarcodeReader not connected");
                dspErr("open failed");
                Toast.makeText(this, "Failed to Connect Barcode ", Toast.LENGTH_SHORT).show();
                return;
            }

            logger.i(TAG, "Barcode Reader connected");
            logger.i(TAG, "Barcode reader " + bcr.toString());
            bcr.setDecodeCallback(this);

            bcr.setErrorCallback(this);

            // Set parameter - Uncomment for QC/MTK platforms
            bcr.setParameter(765, 0); // For QC/MTK platforms
            bcr.setParameter(764, 3);
            //bcr.setParameter(137, 0);
            //bcr.setParameter(8610, 1);
//			 Parameters  param = bcr.getParameters();
//			 Log.d("012", "scan_h:"+bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES)+", cam_h:"+param.getPreviewSize().height);
//			 if(bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES) != param.getPreviewSize().height){
//				 param.setPreviewSize(1360, bcr.getNumProperty(BarCodeReader.PropertyNum.VERTICAL_RES));
//				 param.setPreviewSize(1360,960);
//				 bcr.setParameters(param);
//			 }

            // Sample of how to setup OCR Related String Parameters
            // OCR Parameters
            // Enable OCR-B
            //bcr.setParameter(681, 1);

            // Set OCR templates
            //String OCRSubSetString = "01234567890"; // Only numeric characters
            //String OCRSubSetString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!%"; // Only numeric characters
            // Parameter # 686 - OCR Subset
            //bcr.setParameter(686, OCRSubSetString);

            //String OCRTemplate = "54R"; // The D ignores all characters after the template
            // Parameter # 547 - OCR Template
            //bcr.setParameter(547, OCRTemplate);
            // Parameter # 689 - OCR Minimum characters
            //bcr.setParameter(689, 13);
            // Parameter # 690 - OCR Maximum characters
            //bcr.setParameter(690, 13);

            // Set Orientation
            bcr.setParameter(687, 4); // 4 - omnidirectional

            // Sets OCR lines to decide
            //bcr.setParameter(691, 2); // 2 - OCR 2 lines

            // End of OCR Parameter Sample
//			BarCodeReader.ReaderInfo readinfo = new BarCodeReader.ReaderInfo();
//			BarCodeReader.getReaderInfo(0, readinfo);
//			Log.e("012", "face:"+readinfo.facing);
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

    // === Android UI methods =======================================
    //-----------------------------------------------------
    // create main screen
    private void mainScreen() {
        if (atMain)
            return;

        atMain = true;

        setContentView(R.layout.activity_main);        // Inflate our UI from its XML layout description.


        tvCount = findViewById(R.id.tvCount);
        txtIndicator = findViewById(R.id.txtIndicator);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnPrint = findViewById(R.id.btnPrint);
        recyclerView = findViewById(R.id.recyclerView);
        tvCount.setText("");
        txtIndicator.setTag(false);
        txtIndicator.setText("");

        adapter = new BarcodeAdapter(MainActivity.this, barcodes);
        recyclerView.setAdapter(adapter);
        tvCount.setText(adapter.getItemCount() + " Pcs");


        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "Clear");
                barcodes.clear();
                map.clear();
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");
            }
        });

        btnSave.setOnClickListener(view -> {
            logger.i(TAG, "Save");
//            SettingsActivity.launchActivity(getApplicationContext());
        });

        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.i(TAG, "print");

                checkPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

                Toast.makeText(MainActivity.this, "Not Implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //-----------------------------------------------------
    // create snapshot image screen
    private void snapScreen(Bitmap bmSnap) {
        logger.i(TAG, "snapScreen");
        atMain = false;
        setContentView(R.layout.image);

//        image = (ImageView) findViewById(R.id.snap_image);
//        image.setOnClickListener(mImageClickListener);

//        if (bmSnap != null)
//            image.setImageBitmap(bmSnap);
    }

    // ------------------------------------------------------
    // Called when your activity's options menu needs to be created.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    // ------------------------------------------------------
    // Called right before your activity's option menu is displayed.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    // ------------------------------------------------------
    // Called when a menu item is selected.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // ----------------------------------------
    // display status string
    private void dspStat(String s) {
//        tvStat.setText(s);
        logger.i(TAG, "dspStat " + s);
    }

    // ----------------------------------------
    // display status resource id
    private void dspStat(int id) {
//        tvStat.setText(id);
        logger.i(TAG, "dspStat " + id);
    }

    // ----------------------------------------
    // display error msg
    private void dspErr(String s) {
        logger.i(TAG, "dspErr " + s);
//        tvStat.setText("ERROR" + s);
        txtIndicator.setText("Error");
    }

    // ----------------------------------------
    // display status string
    private void dspData(String s) {
//        tvData.setText(s);
        logger.i(TAG, "dspData " + s);
    }

    // -----------------------------------------
    private void beep() {
        logger.i(TAG, "beep()");
        BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
//		if (tg != null)
//			tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
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

        dspStat("Set #" + num + " to " + val + " " + s);
        return ret;
    }

    // ----------------------------------------
    // start a decode session
    private void doDecode() {
        logger.i(TAG, "doDecode()");
        if (setIdle() != STATE_IDLE) {
            return;
        }
        state = STATE_DECODE;
        decCount = 0;
        decodeDataString = new String("");
        decodeStatString = new String("");
        dspData("");
        dspStat(R.string.decoding);
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
                if (!map.containsKey(barcode.getName())) {
                    barcodes.add(barcode);
                }
                map.put(barcode.getName(), barcode);
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");

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
                if (!map.containsKey(barcode.getName())) {
                    barcodes.add(barcode);
                }
                map.put(barcode.getName(), barcode);
                adapter.notifyDataSetChanged();
                tvCount.setText(adapter.getItemCount() + " Pcs");

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
                dspStat(decodeStatString);
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
                    dspStat("decode timed out");
                    break;

                case BarCodeReader.DECODE_STATUS_CANCELED:
                    dspStat("decode cancelled");
                    break;

                case BarCodeReader.DECODE_STATUS_ERROR:
                default:
                    dspStat("decode failed");
//      		Log.d("012", "decode failed length= " + length);
                    break;
            }
        }
        mKeyF4Down = false;

        //}
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
                dspStat("decode stopped");
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
                dspStat("Scan Mode Changed Event (#" + modechgEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_MOTION_DETECTED:
                ++motionEvents;
                dspStat("Motion Detect Event (#" + motionEvents + ")");
                break;

            case BarCodeReader.BCRDR_EVENT_SCANNER_RESET:
                dspStat("Reset Event");
                break;

            default:
                // process any other events here
                break;
        }
//        mKeyF4Down = false;
    }

    //-------------------------------------------------------
    private Bitmap rotated(Bitmap bmSnap) {
        Matrix matrix = new Matrix();
        if (matrix != null) {
            matrix.postRotate(90);
            // create new bitmap from orig tranformed by matrix
            Bitmap bmr = Bitmap.createBitmap(bmSnap, 0, 0, bmSnap.getWidth(), bmSnap.getHeight(), matrix, true);
            if (bmr != null)
                return bmr;
        }

        return bmSnap;        //when all else fails
    }

    public void onError(int error, BarCodeReader reader) {
        // TODO Auto-generated method stub

        logger.e(TAG, "onError() " + error + " " + reader.toString());

    }

    //add by myself

    /**
     * The encode formate of data scanned by scnner;
     *
     * @param date the source data
     * @return the encode formate;
     * @throws IOException decode exception;
     */
    private String charsetName(byte[] date) {
        return guessEncoding(date);
		/*Charset charset = null;
		InputStream is = new ByteArrayInputStream(date);
		CodepageDetectorProxy detectorProxy = CodepageDetectorProxy.getInstance();
		detectorProxy.add(new ParsingDetector(false));
		detectorProxy.add(JChardetFacade.getInstance());
		detectorProxy.add(ASCIIDetector.getInstance());
		detectorProxy.add(UnicodeDetector.getInstance());
		try {
			charset = detectorProxy.detectCodepage(is,date.length);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (charset != null) {
			return charset.name();
		} else {
			return guessEncoding(date);
		}*/
    }

    /**
     * juniversalchardet guess the encode type
     *
     * @param bytes
     * @return
     */
    private String guessEncoding(byte[] bytes) {
        logger.i(TAG, "guessEncoding() " + Arrays.toString(bytes));
        String DEFAULT_ENCODING = "UTF-8";
        org.mozilla.universalchardet.UniversalDetector detector =
                new org.mozilla.universalchardet.UniversalDetector(new CharsetListener() {
                    @Override
                    public void report(String s) {
                        Log.d("SDLguiActivity", "Charset Name:" + s);
                    }
                });
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }

        logger.i(TAG, "encoding " + encoding);
        return encoding;
    }

    // Function to check and request permission.
    public void checkPermission(MainActivity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
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
}//end-class
