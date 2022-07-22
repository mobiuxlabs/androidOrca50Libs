package in.mobiux.android.orca50libs.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.mobiux.android.commonlibs.utils.AppLogger;
import in.mobiux.android.commonlibs.utils.FileUtils;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected AppLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileUtils.init(getApplicationContext());
        logger = AppLogger.getInstance();
    }
}
