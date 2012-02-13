package fr.insa.helloeverybody.communication;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import fr.insa.helloeverybody.Profil;
import fr.insa.helloeverybody.device.DeviceHelper;

public class ServerInteraction {
	private String mServerAdr;
	private Context mActivityContext;
	private HttpHelper mHttpHelper;
	private DeviceHelper mDeviceHelper;
	
	public ServerInteraction(Context activityContext, String serverAdr) {
		mServerAdr = serverAdr;
		mActivityContext = activityContext;
		
		mHttpHelper = new HttpHelper();
		mDeviceHelper = new DeviceHelper(mActivityContext);
	}
	
	public boolean register(Profil userProfil) {
		HashMap<String, String> params = new HashMap<String, String>(1);
		//Location loc = mDeviceHelper.getDeviceLocation();
		Location loc = new Location("gps");
		loc.setLatitude(47);
		loc.setLongitude(4);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("imei", mDeviceHelper.getDeviceIMEI());
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("fname", userProfil.getPrenom());
			jsonarray.put("lname", userProfil.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "update");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		return answer.equals("OK");
	}
	
	public ArrayList<Profil> getPeopleAround() {
		return null;
	}
}
