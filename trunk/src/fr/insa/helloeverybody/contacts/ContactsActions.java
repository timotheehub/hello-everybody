package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import fr.insa.helloeverybody.communication.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.device.GpsHelperCallbackInterface;
import fr.insa.helloeverybody.models.Profile;

public class ContactsActions implements GpsHelperCallbackInterface {
	private final String SERVER_ADDR = "http://91.121.5.172/otsims/ws.php";
	
	private Profile mUserProfile;
	private Boolean mUpdateContacts;
	private ServerInteractionHelper mServerInteraction;
	private DeviceHelper mDeviceHelper;
	private GpsHelper mGpsHelper;
	
	private ContactsCallbackInterface mContactsCallback;
	
	/*
	 * Classe Privée représentant une tache à faire en background
	 * Ici, MAJ du serveur
	 */
	private class UpdateDatasTask extends AsyncTask<Location, Void, Void> {
		private ArrayList<Profile> mContactsList;
		
		@Override
		protected Void doInBackground(Location... locs) {
			mServerInteraction.register(mUserProfile, locs[0], mDeviceHelper.getDeviceIMEI());
			
			if (mUpdateContacts) {
				mContactsList = mServerInteraction.getPeopleAround(mDeviceHelper.getDeviceIMEI(), locs[0]);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (mUpdateContacts) {
				mUpdateContacts = false;
				mContactsCallback.contactsListUpdated(mContactsList);
			}
			
			super.onPostExecute(result);
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
	}
	
	/*
	 * Demande de mise a jour depuis l'UI
	 */
	public void askUpdatePosition() {
		mGpsHelper.startListening();
	}
	
	public void askUpdateContacts() {
		//Demande la MAJ des contacts
		mUpdateContacts = true;
		
		//Demande de coordonnées GPS
		askUpdatePosition();
	}
	
	/*
	 * Lancement du timer d'evenements programmes
	 */
	public void launchScheduledUpdate() {
	}
	
	/*
	 * Arret du timer d'evenements programmes
	 */
	public void stopScheduledUpdate() {
		mGpsHelper.stopListening();
	}
	
	/*
	 * Implementation de l'interface GpsHelperCallback
	 */
	public void newLocationFound(Location loc) {
		new UpdateDatasTask().execute(loc);
	}
}