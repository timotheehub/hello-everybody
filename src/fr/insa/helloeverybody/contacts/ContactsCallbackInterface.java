package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import fr.insa.helloeverybody.models.Profile;

public interface ContactsCallbackInterface {
	public void contactsListUpdated(ArrayList<Profile> contactsList);
}
