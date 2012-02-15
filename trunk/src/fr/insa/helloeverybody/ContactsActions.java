package fr.insa.helloeverybody;

import java.util.ArrayList;

import fr.insa.helloeverybody.communication.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.device.GpsHelperCallbackInterface;
import fr.insa.helloeverybody.device.NetworkConnectivityListener;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ContactsActions implements GpsHelperCallbackInterface {
	private final String SERVER_ADDR = "http://91.121.5.172/otsims/ws.php";
	
	private Profile mUserProfile;
	private Boolean mUpdateContacts;
	private Context mActivityContext;
	private UpdateIPHandler mHandler;
	
	private ServerInteractionHelper mServerInteraction;
	private DeviceHelper mDeviceHelper;
	private GpsHelper mGpsHelper;
	private NetworkConnectivityListener mConnectivityListener;
	
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
	
	/*
	 * Classe privée définissant ce que doit faire le Handler sur changement de connectivité
	 */
	private class UpdateIPHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.v("Contacts", "Network change !");
			new UpdateDatasTask().execute(mGpsHelper.getLocation());
			super.handleMessage(msg);
		}
	}
	
	public ContactsActions(Context activityContext, Profile userProfile, ContactsCallbackInterface callback) {
		super();
		mContactsCallback = callback;
		mUserProfile = userProfile;
		mUpdateContacts = false;
		mActivityContext = activityContext;
		
		mServerInteraction = new ServerInteractionHelper(activityContext, SERVER_ADDR);
		mDeviceHelper = new DeviceHelper(activityContext);
		mGpsHelper = new GpsHelper(activityContext, this);
		mConnectivityListener = new NetworkConnectivityListener();
		mHandler = new UpdateIPHandler();
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
		//Lancement du monitoring de la connection
		mConnectivityListener.registerHandler(mHandler, 0);
		mConnectivityListener.startListening(mActivityContext);
	}
	
	/*
	 * Arret du timer d'evenements programmes
	 */
	public void stopScheduledUpdate() {
		//Arret du monitoring de la connection
		mConnectivityListener.stopListening();
		mConnectivityListener.unregisterHandler(mHandler);
		mGpsHelper.stopListening();
	}
	
	/*
	 * Implementation de l'interface GpsHelperCallback
	 */
	public void newLocationFound(Location loc) {
		new UpdateDatasTask().execute(loc);
	}
}