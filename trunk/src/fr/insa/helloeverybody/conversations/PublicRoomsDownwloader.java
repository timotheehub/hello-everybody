package fr.insa.helloeverybody.conversations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.location.Location;
import android.os.AsyncTask;
import fr.insa.helloeverybody.customserver.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.interfaces.GpsListener;
import fr.insa.helloeverybody.smack.XmppRoomManager;
import fr.insa.helloeverybody.viewmodels.ConversationList;

/* Classe pour télécharger la liste des salons publics
-----------------------------------------------------------------------------*/
public class PublicRoomsDownwloader implements GpsListener {
	
	// Constructeur public
	public PublicRoomsDownwloader() { }
	
	// Demande le téléchargement de la liste des salons publics
	public void startDownloadPublicRooms() {
		if (DeviceHelper.isEmulator()) {
			Location testLoc = new Location("gps");
			//testLoc.setLatitude(45.78167135);
			//testLoc.setLongitude(4.87290860);
			testLoc.setLatitude(47.6189259);
			testLoc.setLongitude(-122.1213425);
			onLocationUpdated(testLoc);
		} else {
			GpsHelper.getInstance().addListener(this);
		}
	}
	
	// Arrête de demander des mises à jour au GPS
	public void stopDownloadPublicRooms() {
		if (!DeviceHelper.isEmulator()) {
			GpsHelper.getInstance().removeListener(this);
		}
	}
	
	// Implementation de l'interface GpsHelperCallback
	public void onLocationUpdated(Location loc) {
		stopDownloadPublicRooms();
		new UpdateDatasTask().execute(loc);
	}
	
	
	/* Classe pour mettre à jour la liste des salons publics
	-------------------------------------------------------------------------*/
	private class UpdateDatasTask extends AsyncTask<Location, Void, Void> {
		private HashMap<String, String> mPublicGroupsList;
		
		// Récuperer la liste des salons publics
		@Override
		protected Void doInBackground(Location... locs) {
			
			// Récuperer la liste des discussions publiques 
			HashMap<String, String> nearMePublicRooms = 
					ServerInteractionHelper.gePublicRoomsAround(locs[0]);
			HashMap<String, String> availablePublicRooms = 
					XmppRoomManager.getInstance().getPublicRooms();
			
			// TODO(performance): Ne pas demander la liste complète des salons
			// mais vérifier au cas par cas.
			
			// Supprimer les salons qui n'existent plus
			List<String> nearMePublicRoomJidList = 
					new LinkedList<String>(nearMePublicRooms.keySet());
			for (String jid : nearMePublicRoomJidList) {
				if (!availablePublicRooms.containsKey(jid)) {
					nearMePublicRooms.remove(jid);
				}
			}

			// Sauvegarder la liste des salons
			mPublicGroupsList = nearMePublicRooms;
			return null;
		}
		
		// Ajouter la liste des salons à ConversationsList
		@Override
		protected void onPostExecute(Void result) {
			Set<String> nearMeGroupsJidSet = mPublicGroupsList.keySet();
			for (String string : nearMeGroupsJidSet) {
				ConversationList.getInstance().addPublicConversation(string, mPublicGroupsList.get(string));
			}
			
			super.onPostExecute(result);
		}
	}
}