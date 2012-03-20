package fr.insa.helloeverybody.conversations;

import java.util.HashMap;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import fr.insa.helloeverybody.communication.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.device.GpsHelperCallbackInterface;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.smack.ChatService;

public class ConversationsListActions implements GpsHelperCallbackInterface {
	/* ---------------------------------------------
	 * Static vars et singleton
	 * ---------------------------------------------
	 */
	private static ConversationsListActions mConversationsListActions = null;
	
	private final String SERVER_ADDR = "http://im.darkserver.eu.org:8080/otsims/ws.php";
	
	private ServerInteractionHelper mServerInteraction;
	private GpsHelper mGpsHelper;
	private ChatService mChatService;
	private DeviceHelper mDeviceHelper;
	private ConversationsListCallbackInterface mCallback;
	
	/* ----------------------------------------------------------
	 * Classe Privée représentant une tache à faire en background
	 * Ici, MAJ du serveur
	 * ----------------------------------------------------------
	 */
	private class UpdateDatasTask extends AsyncTask<Location, Void, Void> {
		private HashMap<String, String> mPublicGroupsList;
		
		@Override
		protected Void doInBackground(Location... locs) {
			HashMap<String, String> nearMeGroups, availableGroups;
			
			nearMeGroups = mServerInteraction.getGroupsAround(locs[0]);
			availableGroups = mChatService.discoverPublicRooms();
			
			Set<String> nearMeGroupsJidSet = nearMeGroups.keySet();
			for (String jid : nearMeGroupsJidSet) {
				if (!availableGroups.containsKey(jid)) {
					nearMeGroups.remove(jid);
				}
			}
			
			mPublicGroupsList = nearMeGroups;
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Set<String> nearMeGroupsJidSet = mPublicGroupsList.keySet();
			for (String string : nearMeGroupsJidSet) {
				//Log.d("PUBLIC", "Adding : " + string + " : " + mPublicGroupsList.get(string));
				ConversationsList.getInstance().addPublicConversation(string, mPublicGroupsList.get(string));
			}
			
			super.onPostExecute(result);
		}
	}
	
	
	// Constructeur prive
	private ConversationsListActions(Context activityContext) {
		super();
		mServerInteraction = new ServerInteractionHelper(activityContext, SERVER_ADDR);
		mGpsHelper = new GpsHelper(activityContext, this);
		mDeviceHelper = new DeviceHelper(activityContext);
		
		ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				mChatService = ((ChatService.LocalBinder) arg1).getService();
			}

			public void onServiceDisconnected(ComponentName arg0) {
				mChatService = null;
			}
		};
		
		activityContext.getApplicationContext().bindService(new Intent(activityContext, ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public static ConversationsListActions getInstance(Context activityContext) {
		if(mConversationsListActions == null) {
			mConversationsListActions = new ConversationsListActions(activityContext);
		}
		
		return mConversationsListActions;
	}
	
	public void register (ConversationsListCallbackInterface callback) {
		mCallback = callback;
	}
	
	/*
	 * Demande de mise a jour depuis l'UI
	 */
	public void askUpdateGroups() {
		if (mDeviceHelper.isEmulator()) {
			Location testLoc = new Location("gps");
			testLoc.setLatitude(45.78167135);
			testLoc.setLongitude(4.87290860);
			newLocationFound(testLoc);
		} else {
			mGpsHelper.startListeningNoWait();
		}
	}
	
	public void contactsReceived() {
		mGpsHelper.stopListeningNoWait();
	}
	
	/*
	 * Implementation de l'interface GpsHelperCallback
	 */
	public void newLocationFound(Location loc) {
		new UpdateDatasTask().execute(loc);
	}
}