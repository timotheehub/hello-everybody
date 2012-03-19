package fr.insa.helloeverybody.helpers;

import fr.insa.helloeverybody.models.Contact;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseContactHelper {

	public static void updateOrInsertContact(Profile profile) {
		Database db = Database.getInstance();
		db.open();
		Contact contact = db.retrieveContact(profile.getJid());
		if (contact != null) {
			contact.setFavorite(profile.isFavorite());
			contact.setKnown(profile.isKnown());
			contact.setRecommend(profile.isRecommended());
			db.updateContact(contact);
		} else {
			contact = new Contact(profile.getJid(), profile.isFavorite(), profile.isKnown(), profile.isRecommended());
			db.insertContact(contact);
		}
		db.close();
	}
	
	public static void updateOrInsertContact(Contact contact) {
		Database db = Database.getInstance();
		db.open();
		Contact newContact = db.retrieveContact(contact.getJid());
		if (newContact != null) {
			newContact.setFavorite(contact.getFavorite());
			newContact.setKnown(contact.getKnown());
			newContact.setRecommend(contact.getRecommend());
			db.updateContact(contact);
		} else {
			newContact = new Contact(contact.getJid(), contact.getFavorite(), contact.getKnown(), contact.getRecommend());
			db.insertContact(contact);
		}
		db.close();
	}
}