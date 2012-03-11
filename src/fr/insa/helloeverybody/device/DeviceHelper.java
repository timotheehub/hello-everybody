package fr.insa.helloeverybody.device;

import java.util.UUID;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class DeviceHelper {
	private Context mContext;
	private static String mEmulatorImei = UUID.randomUUID().toString().substring(0, 16).replaceAll("-", "");
	
	public DeviceHelper(Context context) {
		mContext = context;
	}
	
	public String generateUniqueId() {
		Log.d("DEVICEHELPER", "JID generated : " + mEmulatorImei);
		return mEmulatorImei;
	}
	
	public Boolean isEmulator() {
		return Build.FINGERPRINT.startsWith("generic");
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}
