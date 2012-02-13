package fr.insa.helloeverybody.communication;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

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
		HashMap<String, String> params = new HashMap<String, String>(2);
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
		return getPeopleAround(100);
	}
	
	public ArrayList<Profil> getPeopleAround(int dist) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		Location loc = new Location("gps");
		loc.setLatitude(47);
		loc.setLongitude(4);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("dist", dist);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "search");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		
		ArrayList<Profil> profilArray = null;
		
		try {
			JSONArray jsonArray = new JSONArray(answer);
			profilArray = new ArrayList<Profil>(jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String ip = jsonObject.getString("ip");
				String fname = jsonObject.getString("fname");
				String lname = jsonObject.getString("lname");
				
				Profil profil = new Profil(fname, lname, ip);
				profilArray.add(profil);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return profilArray;
	}
}
