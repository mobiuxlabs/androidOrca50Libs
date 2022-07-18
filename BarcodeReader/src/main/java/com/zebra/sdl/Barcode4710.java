package com.zebra.sdl;

import android.content.Context;

import com.zebra.decoder.BarCodeReader;
import com.zebra.model.Consumer;
import com.zebra.util.ThreadPool;

/**
 * @author naz
 * Email 961057759@qq.com
 * Date 2020/7/30
 */
public class Barcode4710 implements Reader {
    private Context mContext;
    private Consumer<String> mOnResult;
    private BarCodeReader mBarcode = null;
    private boolean mNeedReinitializeScanner = false;

    @Override
    public boolean open(Context context) {
        this.mContext = context;
        try {
            close();
            int num = BarCodeReader.getNumberOfReaders();
            mBarcode = BarCodeReader.open(num, context.getApplicationContext());
            mBarcode.setErrorCallback((error, reader) -> {
                if (error == 2) {
                    mNeedReinitializeScanner = true;
                }
            });
            mBarcode.setDecodeCallback(new BarCodeReader.DecodeCallback() {
                @Override
                public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
                    if (length > 0) {
                        byte[] bytes = new byte[length];
                        System.arraycopy(data, 0, bytes, 0, length);
                        String strData = new String(bytes);
                        if (mOnResult != null) {
                            ThreadPool.execute(() -> {
                                try {
                                    mOnResult.accept(strData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
                }
            });

            // Set parameter - Uncomment for QC/MTK platforms
            mBarcode.setParameter(765, 0);
            mBarcode.setParameter(764, 3);
            mBarcode.setParameter(687, 4);
            return true;
        } catch (Exception e) {
            mNeedReinitializeScanner = true;
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setResultCallback(Consumer<String> onResult) {
        this.mOnResult = onResult;
    }

    @Override
    public void read(boolean loop) {
        if (mNeedReinitializeScanner) {
            mNeedReinitializeScanner = false;
            open(mContext);
        }
        if (mBarcode != null) {
            mBarcode.startDecode();
        }
    }

    @Override
    public void close() {
        if (mBarcode != null) {
            mBarcode.release();
            mBarcode = null;
        }
    }
}
