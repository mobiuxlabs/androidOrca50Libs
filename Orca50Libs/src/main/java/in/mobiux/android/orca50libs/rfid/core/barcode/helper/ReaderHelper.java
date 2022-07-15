package in.mobiux.android.orca50libs.rfid.core.barcode.helper;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.InputStream;
import java.io.OutputStream;

import in.mobiux.android.orca50libs.rfid.core.barcode.base.QueryMessageTran;
import in.mobiux.android.orca50libs.rfid.core.barcode.base.ReaderBase;

public class ReaderHelper {
	public final static String BROADCAST_ON_LOST_CONNECT = "com.reader.helper.onLostConnect";
	public final static String BROADCAST_REFRESH_BAR_CODE = "com.reader.helper.refresh.barcode";
	public final static String BROADCAST_CMD_SET_STATUS = "com.reader.helper.refresh.command.status";
	public final static String BROADCAST_CMD_QUERY_VALUE = "com.reader.helper.refresh.query.value";
	
	private static LocalBroadcastManager mLocalBroadcastManager = null;

	private static ReaderBase mReader;
	private static Context mContext;
	
	private static ReaderHelper mReaderHelper;
	
	private static ScannerSetting  mScannerSetting = ScannerSetting.newInstance();
	private static TDCodeTagBuffer m_curOperateTagBinDCodeBuffer;
    
    /**
     * Constructor
     */
    public ReaderHelper() {
		m_curOperateTagBinDCodeBuffer = new TDCodeTagBuffer();
	}
    
    /**
     * Set Context
     * @param context		Set Context
     * @throws Exception	Throw an error when the Context is empty
     */
	public static void setContext(Context context) throws Exception {
		mContext = context;
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		
		mReaderHelper = new ReaderHelper();
	}
	
	/**
     * Returns the global reader helper class in helper.
     * @return				Returns the global reader helper class in helper
     * @throws Exception	Throw an error when the global reader helper class is empty
     */
	public static ReaderHelper getDefaultHelper() throws Exception {
		
		if (mReaderHelper == null || mContext == null) 
			throw new NullPointerException("mReaderHelper Or mContext is Null!");

		return mReaderHelper;
	}

	public TDCodeTagBuffer getCurOperateTagBinDCodeBuffer(){
		return m_curOperateTagBinDCodeBuffer;
	}
	public ScannerSetting getCurScannerSetting() {
		return mScannerSetting;
	}
	
	/**
	 * Set and return the global reader helper base class in helper.
	 * @param in			Input stream
	 * @param out			Output stream
	 * @return				Global reader helper base class in helper
	 * @throws Exception	Throw an error when in or out is empty
	 */
	public ReaderBase setReader(final InputStream in, OutputStream out) throws Exception {
		
		if (in == null || out == null) throw new NullPointerException("in Or out is NULL!");
		
		if (mReader == null) {

			mReader = new ReaderBase(in, out) {
				
				@Override
				public void onLostConnect() {
					mLocalBroadcastManager.sendBroadcast(new Intent(BROADCAST_ON_LOST_CONNECT));
				}
				
				@Override
				public void analyData(QueryMessageTran msgTran){
				}

				@Override
				public void receive2DCodeData(String string){
					m_curOperateTagBinDCodeBuffer.getmRawData().add(string);
					mLocalBroadcastManager.sendBroadcast(new Intent(BROADCAST_REFRESH_BAR_CODE));
				}

				@Override
				public void commandSetStatus(byte status) {
                    Intent intent = new Intent(BROADCAST_CMD_SET_STATUS);
                    intent.putExtra("status",status);
                    mLocalBroadcastManager.sendBroadcast(intent);
				}

				@Override
				public void commandQueryValue(byte[] value) {
					//mScannerSetting.mReturnParameterValue = new String(value);
					Intent intent = new Intent(BROADCAST_CMD_QUERY_VALUE);
					intent.putExtra("value",value);
					mLocalBroadcastManager.sendBroadcast(intent);
				}
			};
		}

		return mReader;
	}

	/**
	 * Return the global reader helper base class in helper.
	 * @return				Global reader helper base class in helper
	 * @throws Exception	Throw an error when the global reader helper class is empty
	 */
	public ReaderBase getReader() throws Exception {
		if (mReader == null) {
			throw new NullPointerException("mReader is Null!");
		}
		
		return mReader;
	}
}
