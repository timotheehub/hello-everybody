package fr.insa.helloeverybody.device;

import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;

public class DeviceHelper {
	private TelephonyManager mTelephonyMgr;
	private Context mContext;
	
	public DeviceHelper(Context context) {
		mContext = context;
		mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public String getDeviceIMEI() {
		return mTelephonyMgr.getDeviceId();
	}
	
	public Location getDeviceLocation() {
		GpsHelper gps = new GpsHelper(mContext);
		return gps.getLocation();
	}
}
