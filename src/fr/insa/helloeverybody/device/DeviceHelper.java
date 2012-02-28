package fr.insa.helloeverybody.device;

import android.content.Context;
import android.telephony.TelephonyManager;

public class DeviceHelper {
	private TelephonyManager mTelephonyMgr;
	private Context mContext;
	
	public DeviceHelper(Context context) {
		mContext = context;
		mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public String getPhoneImei() {
		return mTelephonyMgr.getDeviceId();
	}
}
