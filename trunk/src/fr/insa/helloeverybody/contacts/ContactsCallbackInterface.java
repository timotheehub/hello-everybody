package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import fr.insa.helloeverybody.models.Profile;

public interface ContactsCallbackInterface {
	public void contactsListUpdated(ArrayList<Profile> contactsList);
	public void contactWentOnline(String jid);
	public void contactWentOffline(String jid);
	public void contactAdded(String jid);
	public void contactDeleted(String jid);
}
