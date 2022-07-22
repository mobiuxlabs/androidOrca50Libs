package in.mobiux.android.orca50libs.rfid.core;

import android.content.Context;
import android.os.Handler;

import com.module.interaction.ModuleConnector;
import com.module.interaction.RXTXListener;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.config.CMD;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rfid.rxobserver.bean.RXOperationTag;
import com.util.StringTool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.mobiux.android.commonlibs.utils.App;
import in.mobiux.android.commonlibs.utils.AppLogger;
import in.mobiux.android.commonlibs.utils.SessionManager;
//import in.mobiux.android.orca50libs.model.Barcode;
import in.mobiux.android.orca50libs.model.OperationTag;
import in.mobiux.android.orca50libs.model.RFIDTag;
import in.mobiux.android.orca50libs.utils.BeeperHelper;

public class RFIDReader implements Reader {

    public static final String TAG = RFIDReader.class.getCanonicalName();
    private Context context;
    private final SessionManager session;
    private final AppLogger logger;
    private final Handler mHandler;
    private static RFIDReader INSTANCE = null;

    public static String PORT = "dev/ttyS4";
    public static int BAUD_RATE = 115200;

    public ModuleConnector connector = new ReaderConnector();
    public RFIDReaderHelper rfidReaderHelper;
    public RFIDReaderListener listener;
    private boolean connectionStatus = false;
    private boolean observerRegistrationStatus = false;
    private boolean scanningStatus = false;
    public int rssiValue;

    ReaderSetting mReaderSetting = ReaderSetting.newInstance();

    private List<RFIDReaderListener> listeners = new ArrayList<>();

    private final RXTXListener rxtxListener = new RXTXListener() {
        @Override
        public void reciveData(byte[] bytes) {
            logger.i(TAG, "receiveData " + Arrays.toString(bytes));
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        listener.onScanningStatus(true);
                    }
                });
            }
        }

        @Override
        public void sendData(byte[] bytes) {
//            logger.i(TAG, "send Data " + Arrays.toString(bytes));
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        listener.onScanningStatus(true);
                    }
                });
            }
        }

        @Override
        public void onLostConnect() {
            logger.i(TAG, "onLostConnect");
            if (listener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnection(false);
                        connectionStatus = false;
                    }
                });
            }
        }
    };

    private final RXObserver rxObserver = new RXObserver() {

        @Override
        protected void refreshSetting(ReaderSetting readerSetting) {
            logger.i(TAG, "Setting Refresh output power is : " + Arrays.toString(readerSetting.btAryOutputPower));

            rssiValue = byteArrayToInt(readerSetting.btAryOutputPower);
            logger.i(TAG, "rssiValue " + rssiValue);
//            session.setInt(session.KEY_RF_OUTPUT_POWER, rssiValue);
            mReaderSetting = readerSetting;
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            logger.i(TAG, "Command Executed " + cmd + "\tstatus " + status);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    scanningStatus = true;
                    if (cmd == CMD.SET_ACCESS_EPC_MATCH || cmd == CMD.GET_OUTPUT_POWER || cmd == CMD.WRITE_TAG) {
                        scanningStatus = false;
                    }

                    for (RFIDReaderListener l : listeners) {
                        l.onScanningStatus(scanningStatus);
                    }
                }
            });
        }

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            logger.i(TAG, "onInventoryTag :epc " + tag.strEPC + "\t # crc-" + tag.strCRC + "# rssi-" + tag.strRSSI + "# freq-" + tag.strFreq + "#pc-" + tag.strPC + "#btnID-" + tag.btAntId);

            RFIDTag inventory = new RFIDTag();
            inventory.setEpc(tag.strEPC);
            inventory.setRssi(tag.strRSSI);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (RFIDReaderListener l : listeners) {
                        l.onScanningStatus(true);
                        l.onRfidScan(inventory);
                    }
                }
            });
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            logger.i(TAG, "onInventoryTagEnd " + tagEnd.mTotalRead);
            scanningStatus = false;

            if (tagEnd.mTotalRead > 0) {
                beep();
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (RFIDReaderListener l : listeners) {
                        l.onRFIDScanEnd(new RFIDTag.InventoryTagEnd(tagEnd));
                        l.onScanningStatus(scanningStatus);
                    }
                }
            });
        }

        @Override
        protected void onOperationTag(RXOperationTag tag) {
            logger.i(TAG, "onOperationTag " + tag.strEPC);
            beep();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OperationTag operationTag = new OperationTag(tag);
                    for (RFIDReaderListener lis : listeners) {
                        lis.onOperationTag(operationTag);
                    }
                }
            });
        }
    };


    public RFIDReader(Context context) {
        this.context = context;
        logger = AppLogger.getInstance();
        session = SessionManager.getInstance(context);
        mHandler = new Handler(context.getMainLooper());
        BeeperHelper.init(context);

        INSTANCE = this;
    }

    @Override
    public void connect(ReaderType type) {
        initConnection();
    }

    @Override
    public boolean isConnected() {
        return connectionStatus;
    }


    private void initConnection() {

        logger.i(TAG, "Connecting to RFID");

        try {

            if (connector.isConnected()) {
                connectionStatus = connector.isConnected();
            } else {
                connectionStatus = connector.connectCom(PORT, BAUD_RATE);
            }


            if (isConnected()) {
                logger.i(TAG, "RFID READER CONNECTION SUCCESS");

                connectionStatus = true;

                try {

                    rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();
                    if (!observerRegistrationStatus) {
                        rfidReaderHelper.registerObserver(rxObserver);
                        rfidReaderHelper.setRXTXListener(rxtxListener);
                        observerRegistrationStatus = true;
                    }

                    ModuleManager.newInstance().setUHFStatus(true);

                    int beeperResult = -1;
                    beeperResult = rfidReaderHelper.setBeeperMode(mReaderSetting.btReadId, (byte) 2);
                    logger.i(TAG, "beeper result value " + beeperResult);

                    mReaderSetting.newInstance().btBeeperMode = ((byte) 2);

//                    logger.i(TAG, "beeper result " + beeperResult);
                    rfidReaderHelper.setTrigger(true);

//                    rssiValue = session.getIntValue(session.KEY_RF_OUTPUT_POWER, 30);
//                    rssiValue = session.getIntValue("rf_output_power");
                    setRFOutputPower(rssiValue);

                } catch (Exception e) {
                    logger.i(TAG, "Exception - " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } else {
                logger.e(TAG, "CONNECTION FAILED");
            }
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }

        if (listener != null) {
            listener.onConnection(connector.isConnected());
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (rfidReaderHelper != null && isConnected()) {
                    getRFOutputPower();
                }
            }
        }, 500);

        connectionStatus = connector.isConnected();
    }

    public int selectAccessEpcMatch(String tagEPC) {

        //        setting the selected rfid tag
        String[] tagResult = StringTool.stringToStringArray(tagEPC.replace(" ", "").toUpperCase(), 2);
        logger.i(TAG, "selected epc " + tagEPC);
        logger.i(TAG, "selected tag stringArray " + Arrays.toString(tagResult));
        byte[] btAryEpc = StringTool.stringToByteArray(tagEPC);
        btAryEpc = StringTool.stringArrayToByteArray(tagResult, tagResult.length);
        byte selectedTagLength = (byte) (btAryEpc.length & 0xFF);
        logger.i(TAG, "selected tag length " + selectedTagLength);
        logger.i(TAG, "selected tag byteArray " + Arrays.toString(btAryEpc));


        int selectStatus = rfidReaderHelper.setAccessEpcMatch(ReaderSetting.newInstance().btReadId, selectedTagLength, btAryEpc);

        if (selectStatus == 0) {
            logger.i(TAG, "rfid tag selected");
        } else {
            logger.e(TAG, "rfid tag not selected");
        }

        return selectStatus;
    }

//    public int writeToTag(byte[] btAryPassWord, byte btMemBank, byte btWordAdd, byte btWordCnt, byte[] btAryData) {
//
//        int writeStatus = rfidReaderHelper.writeTag(ReaderSetting.newInstance().btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
//        if (writeStatus == 0) {
//            logger.i(TAG, "write is success");
//
//        } else {
//            logger.e(TAG, "write is failed");
//        }
//
//        return writeStatus;
//    }


    public void releaseResources() {

        logger.i(TAG, "Releasing resources");

        try {
            if (connector != null && !connector.isConnected()) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(TAG, "" + e.getLocalizedMessage());
        } finally {
            if (connector != null && connector.isConnected()) {
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
            }
        }

        if (observerRegistrationStatus) {
            rfidReaderHelper.unRegisterObserver(rxObserver);
            rfidReaderHelper.unRegisterObservers();
        }

        if (rfidReaderHelper != null) {
            rfidReaderHelper.signOut();
        }

        listeners.clear();
        BeeperHelper.release();
    }

    public void enableRfidReader(boolean status) {

        if (status) {
            logger.i(TAG, "enabling rfid");
        } else {
            logger.i(TAG, "disabling rfid");
        }

        if (connector != null && connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(status);
        }
    }

    public void deactivateRfidReader() {
        logger.i(TAG, "Activating Rfid Reader");
        if (connector != null && connector.isConnected()) {
            ModuleManager.newInstance().setUHFStatus(false);
        }
    }

    public void setOnRFIDReaderListener(RFIDReaderListener listener) {
        this.listener = listener;

        listeners.remove(listener);
        listeners.add(listener);
    }

    public void unregisterListener(RFIDReaderListener l) {
        listeners.remove(l);
    }


    public void beepEnable(boolean enabled) {
        session.setBooleanValue("beep", enabled);
    }

    private void beep() {
        if (session.getBooleanValue("beep")) {
            logger.i(TAG, "playing beep");
            BeeperHelper.beep();
        } else {
            logger.i(TAG, "beeper is disabled");
        }
    }


    //    methods for RF Output Power
    public int setRFOutputPower(int value) {
//        logger.i(TAG, "Setting RSSI value " + value);
        if (INSTANCE != null && isConnected()) {
            byte val = (byte) value;
            int status = rfidReaderHelper.setOutputPower(ReaderSetting.newInstance().btReadId, val);
            if (status == 0) {
                beep();
                logger.i(TAG, "rf output power set success " + value);
//                session.setIntValue(session.KEY_RF_OUTPUT_POWER, value);
                session.setIntValue("rf_output_power", value);
            } else {
                logger.i(TAG, "rf output power set Failed " + status);
            }
            return status;
        } else {
            logger.e(TAG, "RFID Reader is not connected");
            return 1;
        }
    }

    public int getRFOutputPower() {
        int value = 0;
        try {
            if (INSTANCE != null && rfidReaderHelper != null && isConnected()) {
                int status = rfidReaderHelper.getOutputPower(mReaderSetting.btReadId);
            } else {
                logger.e(TAG, "RFID Reader is not connected");
                return 1;
            }
        } catch (Exception e) {
            logger.e(TAG, "" + e.getLocalizedMessage());
        }
        return value;
    }

    private int byteArrayToInt(byte[] bytes) {
        int rssiValue = 0;
        String str = Arrays.toString(bytes);
        str = str.replace("[", "");
        str = str.replace("]", "");
        rssiValue = Integer.parseInt(str);

        return rssiValue;
    }

    public void startScan() {
        rfidReaderHelper.realTimeInventory(mReaderSetting.btReadId, (byte) 0x01);
    }

    public int reset() {
        int status = -1;
        if (isConnected()) {
            status = rfidReaderHelper.reset(mReaderSetting.btReadId);
        }

        logger.i(TAG, "Resetting reader status (success = 0 ; failed = -1) : " + status);
        return status;
    }
}
