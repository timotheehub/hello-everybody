package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;
import java.util.Timer;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import fr.insa.helloeverybody.communication.ChatService;
import fr.insa.helloeverybody.communication.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.device.GpsHelperCallbackInterface;
import fr.insa.helloeverybody.device.GpsTimerTaskStartListening;
import fr.insa.helloeverybody.device.GpsTimerTaskStopListening;
import fr.insa.helloeverybody.models.Profile;

public class ContactsActions implements GpsHelperCallbackInterface {
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
			mServerInteraction.register(mUserProfile, locs[0], mDeviceHelper.getPhoneImei());
			
			if (mUpdateContacts) {
				mContactsList = mServerInteraction.getPeopleAround(mDeviceHelper.getPhoneImei(), locs[0]);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (mUpdateContacts) {
				mUpdateContacts = false;
				mChatService.replaceNearMeContacts(mContactsList);
				mContactsCallback.contactsListUpdated(mContactsList);
			}
			
			super.onPostExecute(result);
		}
	}
	
	/* ----------------------------------------------------------
	 * Handler pour ChatService
	 * ---------------------------------------------------------- 
	 */
	private class ChatServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.equals("connection established")) {
				askUpdateContacts();
			}
		}
	}
	
	public ContactsActions(Context activityContext, Profile userProfile, ContactsCallbackInterface callback) {
		super();
		mContactsCallback = callback;
		mUserProfile = userProfile;
		mUpdateContacts = false;
		mServerInteraction = new ServerInteractionHelper(activityContext, SERVER_ADDR);
		mDeviceHelper = new DeviceHelper(activityContext);
		mGpsHelper = new GpsHelper(activityContext, this);
		
		mGpsTimerTaskStartListening = new GpsTimerTaskStartListening(mGpsHelper);
		mGpsTimerTaskStopListening = new GpsTimerTaskStopListening(mGpsHelper, mGpsTimerTaskStartListening);
		mGpsTimerStartListening = new Timer();
		mGpsTimerStopListening = new Timer();
		
		mChatService = ChatService.GetChatService();
		ChatService.RegisterHandler(new ChatServiceHandler());
		
	}
	
	/*
	 * Demande de mise a jour depuis l'UI
	 */
	public void askLogin() {
		ChatService.AskLogin("vincenttest", "test");
	}
	
	public void askUpdatePosition() {
		if (mDeviceHelper.isEmulator()) {
			Location testLoc = new Location("gps");
			testLoc.setLatitude(46);
			testLoc.setLongitude(5);
			newLocationFound(testLoc);
		} else {
			mGpsHelper.startListening();
		}
	}
	
	public void askUpdateContacts() {
		//Demande la MAJ des contacts
		mUpdateContacts = true;
		
		//Demande de coordonnées GPS
		askUpdatePosition();
	}
	
	public void contactsReceived() {
		mGpsHelper.stopListening();
	}
	
	/*
	 * Lancement du timer d'evenements programmes
	 */
	public void launchScheduledUpdate() {
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