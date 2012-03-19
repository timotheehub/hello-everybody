package fr.insa.helloeverybody.communication;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import fr.insa.helloeverybody.models.Profile;

public class ServerInteractionHelper {
	private final static int DISTANCE_VISIBILITY = 50000;
	
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
	
	public boolean registerGroup(String jid, String name, Location loc) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("jid", jid);
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("name", name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "updateGroup");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		return answer.equals("OK");
	}
	
	public boolean exists(String jid) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("jid", jid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "exists");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		return answer.equals("OK");
	}
	
	public ArrayList<Profile> getPeopleAround(String jid, Location loc) {
		return getPeopleAround(jid, loc, DISTANCE_VISIBILITY);
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
				double distance = Double.parseDouble(jsonObject.getString("distance"));
				String jidReceived = jsonObject.getString("jid");
				
				Profile profile = new Profile();
				profile.setFirstName(fname);
				profile.setLastName(lname);
				profile.setDistance((int)distance);
				profile.setJid(jidReceived);
				profilArray.add(profile);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return profilArray;
	}
	
	public HashMap<String, String> getGroupsAround(Location loc) {
		return getGroupsAround(loc, DISTANCE_VISIBILITY);
	}
	
	public HashMap<String, String> getGroupsAround(Location loc, int dist) {
		HashMap<String, String> params = new HashMap<String, String>(2);
		
		JSONObject jsonarray = new JSONObject();
		try {
			jsonarray.put("lat", loc.getLatitude());
			jsonarray.put("lon", loc.getLongitude());
			jsonarray.put("dist", dist);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		params.put("wsname", "searchGroups");
		params.put("arrayvar", jsonarray.toString());
		
		String answer = mHttpHelper.performPost(mServerAdr, params);
		
		HashMap<String, String> groupMap = null;
		
		try {
			JSONArray jsonArray = new JSONArray(answer);
			groupMap = new HashMap<String, String>(jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String jid = jsonObject.getString("jid");
				String name = jsonObject.getString("name");
				
				groupMap.put(jid, name);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return groupMap;
	}
}
