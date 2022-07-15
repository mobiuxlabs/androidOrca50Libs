package in.mobiux.android.orca50libs.rfid.core;

import in.mobiux.android.orca50libs.model.OperationTag;
import in.mobiux.android.orca50libs.model.RFIDTag;


/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public interface RFIDReaderListener {

    void onScanningStatus(boolean status);

    void onRfidScan(RFIDTag rfidTag);

    void onOperationTag(OperationTag operationTag);

    void onRFIDScanEnd(RFIDTag.InventoryTagEnd tagEnd);

    void onConnection(boolean status);
}
