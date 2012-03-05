package fr.insa.helloeverybody.smack;

import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterGroup;

public class RosterHelper {
	private enum GROUP_NAME {
		FAVORIS, RECOMMANDES, PROCHES, RECENTS
	};

	private Roster mRoster;

	public RosterHelper(Roster connectionRoster) {
		mRoster = connectionRoster;
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
}