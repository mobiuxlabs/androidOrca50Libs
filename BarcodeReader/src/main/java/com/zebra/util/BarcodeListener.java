package com.zebra.util;

public interface BarcodeListener {

    void onBarcodeScan(String barcode);

    void onScanStatus(boolean status);

    void onConnection(boolean status);

    void message(int type, String msg);
}
