package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

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
import fr.insa.helloeverybody.device.GpsTimerTaskStartListening;
import fr.insa.helloeverybody.device.GpsTimerTaskStopListening;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.RosterHelper.GROUP_NAME;

public class ContactsActions implements GpsHelperCallbackInterface {
	/* ---------------------------------------------
	 * Static vars et singleton
	 * ---------------------------------------------
	 */
	private static ContactsActions mContactsActions = null;
	
	private final String SERVER_ADDR = "http://im.darkserver.eu.org:8080/otsims/ws.php";
	
	// Temps pour le timer du GPS, en millisecondes
	private final long EXECUTION_TIME = 1000 * 60 * 2;
	private final long TIME_BEFORE_FIRST_EXECUTION = 1000 * 60 * 2;
	private final long TIME_BETWEEN_TWO_EXECUTIONS = 1000 * 60 * 5;
	
	private Profile mUserProfile;
	private Boolean mUpdateContacts;
	private ServerInteractionHelper mServerInteraction;
	private DeviceHelper mDeviceHelper;
	private GpsHelper mGpsHelper;
	private ChatService mChatService;
	
	private GpsTimerTaskStartListening mGpsTimerTaskStartListening;
	private GpsTimerTaskStopListening mGpsTimerTaskStopListening;
	private Timer mGpsTimerStartListening;
	private Timer mGpsTimerStopListening;
	
	private ContactsCallbackInterface mContactsCallback;
	
	/* ----------------------------------------------------------
	 * Classe Privée représentant une tache à faire en background
	 * Ici, MAJ du serveur
	 * ----------------------------------------------------------
	 */
	private class UpdateDatasTask extends AsyncTask<Location, Void, Void> {
		private ArrayList<Profile> mContactsList;
		
		@Override
		protected Void doInBackground(Location... locs) {
			mServerInteraction.register(mUserProfile, locs[0], mUserProfile.getJid());
			
			if (mUpdateContacts) {
				mContactsList = mServerInteraction.getPeopleAround(mUserProfile.getJid(), locs[0]);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (mUpdateContacts) {
				mUpdateContacts = false;
				mChatService.updateGroup(GROUP_NAME.PROCHES, mContactsList, true);
				mContactsList = mChatService.getPresence(mContactsList).get("online");
				mContactsCallback.contactsListUpdated(mContactsList);
			}
			
			super.onPostExecute(result);
		}
	}
	
	
	// Constructeur prive
	private ContactsActions(Context activityContext, Profile userProfile) {
		super();
		mUserProfile = userProfile;
		mUpdateContacts = false;
		mServerInteraction = new ServerInteractionHelper(activityContext, SERVER_ADDR);
		mDeviceHelper = new DeviceHelper(activityContext);
		mGpsHelper = new GpsHelper(activityContext, this);
		
		ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				mChatService = ((ChatService.LocalBinder) arg1).getService();
				mChatService.addRosterListener(new RosterListener() {
					
					public void presenceChanged(Presence arg0) {
						if (mContactsCallback != null) {
							if (arg0.isAvailable())
								mContactsCallback.contactWentOnline(arg0.getFrom().split("@")[0]);
							else
								mContactsCallback.contactWentOffline(arg0.getFrom().split("@")[0]);
						}
					}
					
					public void entriesDeleted(Collection<String> arg0) {
						if (mContactsCallback != null) {
							for (String string : arg0) {
								mContactsCallback.contactDeleted(string);
							}
						}
					}
					
					public void entriesAdded(Collection<String> arg0) {
						if (mContactsCallback != null) {
							for (String string : arg0) {
								mContactsCallback.contactAdded(string);
							}
						}
					}

					public void entriesUpdated(Collection<String> arg0) {
					}
				});
			}

			public void onServiceDisconnected(ComponentName arg0) {
				mChatService = null;
			}
		};
		
		activityContext.getApplicationContext().bindService(new Intent(activityContext, ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public static ContactsActions getInstance(Context activityContext, Profile userProfile) {
		if(mContactsActions == null) {
			mContactsActions = new ContactsActions(activityContext, userProfile);
		}
		
		return mContactsActions;
	}
	
	public void register (ContactsCallbackInterface callback) {
		mContactsCallback = callback;
	}
	
	
	/*
	 * Demande de mise a jour depuis l'UI
	 */
	public void askUpdatePosition() {
		if (mDeviceHelper.isEmulator()) {
			Location testLoc = new Location("gps");
			testLoc.setLatitude(45.78167135);
			testLoc.setLongitude(4.87290860);
			newLocationFound(testLoc);
		} else {
			mGpsHelper.startListeningNoWait();
		}
	}
	
	public void askUpdateContacts() {
		//Demande la MAJ des contacts
		mUpdateContacts = true;
		
		//Demande de coordonnées GPS
		askUpdatePosition();
	}
	
	public void contactsReceived() {
		mGpsHelper.stopListeningNoWait();
	}
	
	/*
	 * Lancement du timer d'evenements programmes
	 */
	public void launchScheduledUpdate() {
		mGpsTimerTaskStartListening = new GpsTimerTaskStartListening(mGpsHelper);
		mGpsTimerTaskStopListening = new GpsTimerTaskStopListening(mGpsHelper, mGpsTimerTaskStartListening);
		mGpsTimerStartListening = new Timer();
		mGpsTimerStopListening = new Timer();
		
		mGpsTimerStartListening.schedule(mGpsTimerTaskStartListening,
				TIME_BEFORE_FIRST_EXECUTION,
				EXECUTION_TIME + TIME_BETWEEN_TWO_EXECUTIONS);
		mGpsTimerStopListening.schedule(mGpsTimerTaskStopListening,
				TIME_BEFORE_FIRST_EXECUTION + EXECUTION_TIME,
				EXECUTION_TIME + TIME_BETWEEN_TWO_EXECUTIONS);
	}
	
	/*
	 * Arret du timer d'evenements programmes
	 */
	public void stopScheduledUpdate() {
		mGpsTimerTaskStartListening.cancel();
		mGpsTimerTaskStopListening.cancel();
		mGpsTimerStartListening.cancel();
		mGpsTimerStopListening.cancel();
	}
	
	/*
	 * Implementation de l'interface GpsHelperCallback
	 */
	public void newLocationFound(Location loc) {
		new UpdateDatasTask().execute(loc);
	}
}