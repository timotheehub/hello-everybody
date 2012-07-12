package fr.insa.helloeverybody.smack;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import fr.insa.helloeverybody.helpers.LogWriter;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

/* Gère la liste des contacts (roster) sur le serveur XMPP
-----------------------------------------------------------------------------*/
public class XmppContactsManager {
	public static final String TAG = "XmppContactsManager";
	public static final String NEAR_ME_ROSTER = "NearMe";

	// Construit les rosters s'ils n'existent pas
	public static void createNearMeGroup() {
		Roster roster = XmppConnectionManager.getInstance().getRoster();
		Collection<RosterGroup> rosterGroups = roster.getGroups();
		
		// Créer la liste des rosters existants
		HashSet<String> rosterNameSet = new HashSet<String>();
		for (RosterGroup rosterGroup : rosterGroups) {
			rosterNameSet.add(rosterGroup.getName());
		}

		// Créer le groupe s'il n'existe pas
		if (!rosterNameSet.contains(NEAR_ME_ROSTER)) {
			try {
				roster.createGroup(NEAR_ME_ROSTER);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
	
	// Supprime tous les contacts d'un groupe
	public static void removeNearMeRosterMembers() {
		Roster roster = XmppConnectionManager.getInstance().getRoster();
		RosterGroup group = roster.getGroup(NEAR_ME_ROSTER);
		
		// Vérifie que le groupe existe
		if (group == null) {
			return;
		}
		
		// Supprime les membres du groupe
		Collection<RosterEntry> groupEntries = group.getEntries();
		for (RosterEntry rosterEntry : groupEntries) {
			try {
				group.removeEntry(rosterEntry);
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
	
	// Ajoute une liste de contacts à un groupe
	public static boolean addContactsToNearMeRoster(Collection<Profile> profileList) {
		Roster roster = XmppConnectionManager.getInstance().getRoster();
		RosterGroup group = roster.getGroup(NEAR_ME_ROSTER);
		String[] groupList = { NEAR_ME_ROSTER };
		
		// Vérifier que le groupe existe
		if (group == null) {
			return false;
		}
		
		// Ajouter les contacts au groupe
		for (Profile profile : profileList) {
			try {
				roster.createEntry(profile.getJid() + "@" 
						+ XmppConnectionManager.SERVER_ADDRESS, 
						profile.getFullName(), groupList);
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		}
		
		return true;
	}
		
	// Retourne la liste de personnes en lignes 
	public static ArrayList<Profile> getOnlineList(Collection<Profile> profileList) {
		ArrayList<Profile> onlineList = new ArrayList<Profile>();
		
		for (Profile profile : profileList) {
			if (isOnline(profile.getJid() + "@" 
					+ XmppConnectionManager.SERVER_ADDRESS)) {
				onlineList.add(profile);
			}
		}
		
		return onlineList;
	}
	
	// Vérifie si un utilisateur est en ligne
	public static boolean isOnline(String jid) {
		return XmppConnectionManager.getInstance().getRoster()
				.getPresence(jid).isAvailable();
	}
	
	// Télécharge un profil depuis le serveur XMPP
	public static Profile downloadProfile(String jid) {
		// Vérifier que la connection XMPP
		XMPPConnection xmppConnection = XmppConnectionManager.getInstance()
				.getXmppConnection();
		if (!xmppConnection.isConnected()) {
			return null;
		}
		
		// Essayer de charger de le profil
		VCard profileVCard = new VCard();
		try {
			profileVCard.load(xmppConnection, jid + "@" 
					+ XmppConnectionManager.SERVER_ADDRESS);
		} catch (XMPPException e) {
			return null;
		}
		
		// Vérifier que le profil est correct
		if ((profileVCard == null) || (profileVCard.getFirstName() == null) 
				|| (profileVCard.getLastName() == null)) {
			return null;
		}
		
		// Construire le profil correspondant à la VCard
		Bitmap bmp = null;
		byte[] avatar = profileVCard.getAvatar();
		if (avatar != null) {
			bmp = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
		}
		String strAge = profileVCard.getField("age");
		Integer age = 18;
		if (strAge != null) {
			age = Integer.parseInt(strAge);
		}
		
		// Retourner le profil
		return new Profile(jid, profileVCard.getFirstName(), 
				profileVCard.getLastName(), age, profileVCard.getField("sex"), 
				profileVCard.getField("relationship"), 
				profileVCard.getField("interests"), 
				profileVCard.getField("friendsJids"), bmp, true);
	}
	
	// Sauvegarder le profil local sur le serveur XMPP
	public static void saveLocalProfile() {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				Profile localProfile = LocalUserProfile.getInstance().getProfile();
				String localJid = (localProfile != null) ? 
						localProfile.getJid() : "unknown jid"; 
				if (saveLocalProfileInternal()) {
					LogWriter.logIfDebug(TAG, "Profile saved : " + localJid);
				} else {
					LogWriter.logIfDebug(TAG, "Profile unsaved : " + localJid);
				}
			}
		});
	}

	// Sauvegarde le profil local sur le serveur XMPP
	private static boolean saveLocalProfileInternal() {
		// Vérifier qu'on est connecté et que le profile existe
		Profile localProfile = LocalUserProfile.getInstance().getProfile();
		XMPPConnection xmppConnection = XmppConnectionManager.getInstance()
				.getXmppConnection();
		if ((!xmppConnection.isConnected())
			|| (localProfile == null)) {
			return false;
		}
		
		// Créer la VCard
		VCard localProfileVCard = new VCard();
		localProfileVCard.setFirstName(localProfile.getFirstName());
		localProfileVCard.setLastName(localProfile.getLastName());
		localProfileVCard.setJabberId(localProfile.getJid());
		localProfileVCard.setField("age", localProfile.getAge().toString());
		localProfileVCard.setField("sex", localProfile.getSexString());
		localProfileVCard.setField("relationship", localProfile.getRelationshipString());
		localProfileVCard.setField("interests", localProfile.getInterestsListToJson());
		localProfileVCard.setField("friendsJids", localProfile.getFriendsJidListToJson());
		
		// Ajouter l'image à la VCard
		Bitmap bmp = localProfile.getAvatar();
		if (bmp != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			localProfileVCard.setAvatar(byteArray);
		}
		
		// Essayer de sauvegarder la VCard
		try {
			localProfileVCard.save(xmppConnection);
		} catch (XMPPException e) {
			return false;
		}
		
		return true;
	}
}
