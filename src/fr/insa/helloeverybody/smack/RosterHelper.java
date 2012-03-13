package fr.insa.helloeverybody.smack;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
				try {
					mRoster.createGroup(name.toString());
				} catch (Exception e) {
				}
			}
		}
		
		rosterGroups = mRoster.getGroups();
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
	
	public void addRosterListener(RosterListener rl) {
		mRoster.addRosterListener(rl);
	}
	
	public void removeRosterListener(RosterListener rl) {
		mRoster.removeRosterListener(rl);
	}
	
	public Boolean isOnline(String jid) {
		return mRoster.getPresence(jid).isAvailable();
	}
	
	public Profile loadProfile(String jid) {
		VCard newVCard = mConnection.getVCard(jid);
		
		if (newVCard != null) {
			Bitmap bmp = null;
			byte[] avatar = newVCard.getAvatar();
			
			if (avatar != null) {
				bmp = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
			}
			
			String strAge = newVCard.getField("age");
			Integer age = null;
			if (strAge != null) {
				age = Integer.parseInt(strAge);
			}
			
			return new Profile(newVCard.getFirstName(), newVCard.getLastName(), age, newVCard.getField("sex"), 
					newVCard.getField("relationship"), newVCard.getField("interests"), bmp);
		}
		
		return null;
	}
	
	public Boolean saveMyProfile(Profile myProfile) {
		VCard myVCard = new VCard();
		
		myVCard.setFirstName(myProfile.getFirstName());
		myVCard.setLastName(myProfile.getLastName());
		myVCard.setJabberId(myProfile.getJid());
		myVCard.setField("age", myProfile.getAge().toString());
		myVCard.setField("sex", myProfile.getSexString());
		myVCard.setField("relationship", myProfile.getRelationshipString());
		myVCard.setField("interests", myProfile.getInterestsListToJson());
		
		Bitmap bmp = myProfile.getAvatar();
		
		if (bmp != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			myVCard.setAvatar(byteArray);
		}
		
		return mConnection.saveVCard(myVCard);
	}
}