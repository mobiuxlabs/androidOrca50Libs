package in.mobiux.android.orca50libs.model;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.io.Serializable;
import java.util.Calendar;

//import in.mobiux.android.commonlibs.utils.AppUtils;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */

public class RFIDTag implements Serializable {

    private String epc;
    private int scanCount = 0;
    private String rssi;
    private long updatedAt = System.currentTimeMillis();
    private boolean scanStatus = false;
    private String timestamp = Calendar.getInstance().getTime().toString();

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getScanCount() {
        return scanCount;
    }

    public void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(boolean scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }



    @Override
    public String toString() {
        return "" + epc;
    }

    public static class InventoryTagEnd {
        public int mCurrentAnt = 0;
        public int mTagCount = 0;
        public int mReadRate = 0;
        public int mTotalRead = 0;
        public byte cmd = 0;

        public InventoryTagEnd() {
        }

        public InventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            mCurrentAnt = tagEnd.mCurrentAnt;
            mTagCount = tagEnd.mTagCount;
            mReadRate = tagEnd.mReadRate;
            mTotalRead = tagEnd.mTotalRead;
            cmd = tagEnd.cmd;
        }
    }
}

