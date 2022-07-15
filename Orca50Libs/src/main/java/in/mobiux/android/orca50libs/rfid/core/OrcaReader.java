package in.mobiux.android.orca50libs.rfid.core;

import android.content.Context;

import androidx.core.util.Consumer;


//this is under development
public interface OrcaReader {

    boolean connect(Context context);

    void setResultCallback(Consumer<String> onResult);

    void scan(boolean loop);

    void release();
}
