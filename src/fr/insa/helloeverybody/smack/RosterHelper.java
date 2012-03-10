package fr.insa.helloeverybody.smack;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import android.util.Log;

import fr.insa.helloeverybody.models.Profile;

public class RosterHelper {
	public static String TAG = "RosterHelper";
	
	public enum GROUP_NAME {
		FAVORIS, RECOMMANDES, PROCHES, RECENTS
	};

	private Roster mRoster;
	private ConnectionHelper mConnection;

	public RosterHelper(ConnectionHelper connectionHelper) {
		mRoster = connectionHelper.getRoster();
		mConnection = connectionHelper;
		mRoster.setSubscriptionMode(SubscriptionMode.accept_all);
		mRoster.addRosterListener(null);
	}

	public void rebuildRosterGroups() {
		Collection<RosterGroup> rosterGroups = mRoster.getGroups();
		Integer count = 0;

		for (RosterGroup group : rosterGroups) {
			if (GROUP_NAME.valueOf(group.getName()) != null) {
				count++;
			}
		}

		GROUP_NAME[] groupNames = GROUP_NAME.values();
		if (count != groupNames.length) {
			for (GROUP_NAME name : groupNames) {
				mRoster.createGroup(name.toString());
			}
		}
	}
	
	public void flushGroup(GROUP_NAME gn) {
		RosterGroup group = mRoster.getGroup(gn.toString());
		
		if (group != null) {
			Collection<RosterEntry> groupEntries = mRoster.getGroup(gn.toString()).getEntries();
			
			for (RosterEntry rosterEntry : groupEntries) {
				try {
					group.removeEntry(rosterEntry);
				} catch (XMPPException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}
	
	public Boolean addContactsToGroup(Collection<Profile> profileList, GROUP_NAME gn) {
		RosterGroup group = mRoster.getGroup(gn.toString());
		String[] groupList = {gn.toString()};
		Boolean succes = true;
		
		if (group != null) {
			for (Profile profile : profileList) {
				try {
					mRoster.createEntry(profile.getJid() + "@" + mConnection.getServerDomain(), profile.getFullName(), groupList);
				} catch (XMPPException e) {
					succes = false;
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		
		return succes;
	}
	
	public Profile loadProfile(String jid) {
		VCard newVCard = mConnection.getVCard(jid);
		
		if (newVCard != null) {
			if(newVCard.getField("age") != null)
				return new Profile(newVCard.getFirstName(), newVCard.getLastName(), Integer.parseInt(newVCard.getField("age")), newVCard.getField("sex"), newVCard.getField("relationship"));
			else
				return new Profile(newVCard.getFirstName(), newVCard.getLastName());
		}
		
		return null;
	}
	
	public Boolean saveMyProfile(Profile myProfile) {
		VCard myVCard = new VCard();
		
		myVCard.setFirstName(myProfile.getFirstName());
		myVCard.setLastName(myProfile.getLastName());
		myVCard.setJabberId(myProfile.getJid());
		//myVCard.setAvatar(null);
		myVCard.setField("age", myProfile.getAge().toString());
		myVCard.setField("sex", myProfile.getSexString());
		myVCard.setField("relationship", myProfile.getRelationshipString());
		
		return mConnection.saveVCard(myVCard);
	}
}