package fr.insa.helloeverybody.communication;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import fr.insa.helloeverybody.models.Profile;

public class ServerInteractionHelper {
	private String mServerAdr;
	private HttpHelper mHttpHelper;
	
	public ServerInteractionHelper(Context activityContext, String serverAdr) {
		mServerAdr = serverAdr;
		mHttpHelper = new HttpHelper();
	}
	
	public boolean register(Profile userProfil, Location loc, String jid) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("jid", jid);
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("fname", userProfil.getFirstName());
			jsonarray.put("lname", userProfil.getLastName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "update");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		return answer.equals("OK");
	}
	
	public ArrayList<Profile> getPeopleAround(String jid, Location loc) {
		return getPeopleAround(jid, loc, 100);
	}
	
	public ArrayList<Profile> getPeopleAround(String jid, Location loc, int dist) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("jid", jid);
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("dist", dist);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "search");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		
		ArrayList<Profile> profilArray = null;
		
		try {
			JSONArray jsonArray = new JSONArray(answer);
			profilArray = new ArrayList<Profile>(jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String fname = jsonObject.getString("fname");
				String lname = jsonObject.getString("lname");
				
				Profile profile = new Profile();
				profile.setFirstName(fname);
				profile.setLastName(lname);
				profile.setJid(jid);
				profilArray.add(profile);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return profilArray;
	}
}
