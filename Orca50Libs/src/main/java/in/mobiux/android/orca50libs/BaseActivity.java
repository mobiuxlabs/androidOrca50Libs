package in.mobiux.android.orca50libs;

import android.os.Bundle;

import in.mobiux.android.commonlibs.activity.AppActivity;
import in.mobiux.android.commonlibs.utils.AppLogger;

public class BaseActivity extends AppActivity {

    private static final String TAG = "BaseActivity";
    protected AppLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = AppLogger.getInstance();
    }
}
