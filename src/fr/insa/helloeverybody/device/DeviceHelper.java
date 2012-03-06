package fr.insa.helloeverybody.device;

import java.util.UUID;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceHelper {
	private TelephonyManager mTelephonyMgr;
	private Context mContext;
	private static String mEmulatorImei = UUID.randomUUID().toString().substring(0, 16).replaceAll("-", "");
	
	public DeviceHelper(Context context) {
		mContext = context;
		mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public String getPhoneImei() {
		if (isEmulator()) {
			Log.d("DEVICEHELPER", "JID for this emulator : " + mEmulatorImei);
			return mEmulatorImei;
		} else
			return mTelephonyMgr.getDeviceId();
	}
	
	public Boolean isEmulator() {
		return Build.FINGERPRINT.startsWith("generic");
	}
}
