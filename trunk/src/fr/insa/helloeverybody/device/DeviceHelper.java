package fr.insa.helloeverybody.device;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

public class DeviceHelper {
	private TelephonyManager mTelephonyMgr;
	private LocationManager mLocationMgr;
	private Context mContext;
	
	public DeviceHelper(Context context) {
		mContext = context;
		mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public String getDeviceIMEI() {
		return mTelephonyMgr.getDeviceId();
	}
	
	public Location getDeviceLocation() {
		Criteria criteria = new Criteria();
		String provider = mLocationMgr.getBestProvider(criteria, true);
		return mLocationMgr.getLastKnownLocation(provider);
	}
}
