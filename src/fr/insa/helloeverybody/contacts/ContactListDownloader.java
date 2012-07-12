package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.location.Location;
import android.os.AsyncTask;
import fr.insa.helloeverybody.customserver.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DatabaseManager;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.interfaces.GpsListener;
import fr.insa.helloeverybody.models.ContactRelationship;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.smack.XmppContactsManager;
import fr.insa.helloeverybody.smack.XmppEventsManager;
import fr.insa.helloeverybody.viewmodels.ContactsList;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

/* Classe capable de télécharger la liste de contacts
-----------------------------------------------------------------------------*/
public class ContactListDownloader implements GpsListener {
			
	// Demande la mise à jour de la position GPS
	public void startDownloadContactList() {
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
	
	// Se désabonne du listener du GPS
	public void stopDownloadContactList() {
		if (!DeviceHelper.isEmulator()) {
			GpsHelper.getInstance().removeListener(this);
		}
	}
	
	// Implementation de l'interface GpsHelperCallback
	public void onLocationUpdated(Location loc) {
		stopDownloadContactList();
		new DownloaderTask().execute(loc);
	}
	
	
	/* Tâche capable de télécharger la liste de contacts en arrière-plan
	-------------------------------------------------------------------------*/
	private class DownloaderTask extends AsyncTask<Location, Void, Void> {
		
		private ArrayList<Profile> mContactsList;
		
		// Télécharge la liste des contacts
		@Override
		protected Void doInBackground(Location... locs) {
			
			// Envoyer la position de l'utilisateur local
			Profile localProfile = LocalUserProfile.getInstance().getProfile();
			ServerInteractionHelper.registerUser(localProfile, locs[0], localProfile.getJid());
			
			// Télécharge la liste des contacts
			mContactsList = ServerInteractionHelper.getPeopleAround(localProfile.getJid(), locs[0]);
			
			// Supprimer le listener du groupe de contacts
	        XmppEventsManager.getInstance().removeRosterListener();
			
			// Ajouter les contacts au roster pour savoir qui est en ligne
			// TODO(performance): Ne pas supprimer les profils déjà téléchargés
			// s'ils sont dans la nouvelle liste 
			XmppContactsManager.removeNearMeRosterMembers();
			XmppContactsManager.addContactsToNearMeRoster(mContactsList);
			mContactsList = XmppContactsManager.getOnlineList(mContactsList);
			
			return null;
		}
		
		// Signale la fin du téléchargement de la liste des contacts
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
						
			// Ajouter une liste de faux contacts
			addFakeContactList();
				
			// Affecter les types de contacts
			for (Profile profile : mContactsList) {
				ContactRelationship contact = DatabaseManager.getInstance()
						.retrieveContact(profile.getJid());
				if (contact != null) {
					profile.setFavorite(contact.getFavorite());
					profile.setKnown(contact.getKnown());
					profile.setRecommended(contact.getRecommended());
				}
			}
			
			// Ajouter les contacts à la liste de contacts
			ContactsList contactsList = ContactsList.getInstance();
			
			// TODO(performance): Ne pas supprimer les profils déjà téléchargés
			// s'ils sont dans la nouvelle liste 
	        contactsList.clearAllLists();
	        contactsList.addProfileList(mContactsList);
	        
	        // Ajouter un listener au groupe de contacts
	        XmppEventsManager.getInstance().addRosterListener();
		}
				
		// Ajoute une fausse liste de contacts
		private void addFakeContactList() {
	        mContactsList.add(new Profile("abma01234567890", "Bob", "L'éponge"));
	        mContactsList.add(new Profile("abma01234567891", "Patrick", "L'étoile de mer"));
	        mContactsList.add(new Profile("abma01234567892", "Timothée", "L."));
	        mContactsList.add(new Profile("abma01234567893", "Julian", "Dos Santos"));
	        mContactsList.add(new Profile("abma01234567894", "Vincent", "B."));
	        mContactsList.add(new Profile("abma01234567895", "Li Chen", "T."));
	        mContactsList.add(new Profile("abma01234567896", "Loïc", "T."));
	        mContactsList.add(new Profile("abma01234567897", "Rafael", "Corral"));
		}
	}
}