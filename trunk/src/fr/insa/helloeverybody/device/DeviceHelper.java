package fr.insa.helloeverybody.device;

import java.util.UUID;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/* Classe permettant de donner des informations sur le device.
-----------------------------------------------------------------------------*/
public class DeviceHelper {
	private static String mEmulatorImei = 
			UUID.randomUUID().toString().substring(0, 16).replaceAll("-", "");
		
	public static String generateUniqueId() {
		Log.d("DeviceHelper", "JID generated : " + mEmulatorImei);
		return mEmulatorImei;
	}
	
	public static Boolean isEmulator() {
		return Build.FINGERPRINT.startsWith("generic");
	}
	
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) 
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected());
	}
}
