package in.mobiux.android.orca50libs.rfid.core;

public interface Reader {
    enum ReaderType {
        RFID, BARCODE;
    }

    void connect(ReaderType type);
    boolean isConnected();
}
