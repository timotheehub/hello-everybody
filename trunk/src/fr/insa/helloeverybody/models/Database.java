package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.List;

import fr.insa.helloeverybody.helpers.DatabaseHelper;
import fr.insa.helloeverybody.models.Profile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
 
	private static Database instance = new Database();
	
	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "db_profile";
 
	private SQLiteDatabase db;
	private DatabaseHelper databaseHelper;
 
	public Database(){

	}
	
	public void initDatabase(Context context) {
		this.databaseHelper = new DatabaseHelper(context, NOM_BDD, null, VERSION_BDD);
	}
	
	public static Database getInstance(){
		return Database.instance;
	}
 
	public void open(){
		//on ouvre la BDD en écriture
		db = databaseHelper.getWritableDatabase();
	}
 
	public void close(){
		//on ferme l'accès à la BDD
		db.close();
	}
 
	public SQLiteDatabase getBDD(){
		return db;
	}
 
	public void insertProfile(Profile profile){
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		//on lui ajoute une valeur associée à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put("jid", profile.getJid());
		values.put("firstName", profile.getFirstName());
		values.put("lastName", profile.getLastName());
		values.put("age", profile.getAge());
		values.put("relationshipStatus", profile.getRelationshipString());
		values.put("sexStatus", profile.getSexString());

		db.insert("table_profile", null, values);
		
//		for (String interest : profile.getInterestsList()) {
//			ContentValues value = new ContentValues();
//			value.put("interest", interest);
//			db.insert("table_interests", null, value);
//		}			
	}
 
	public void updateProfile(Profile profile){
		//La mise à jour d'un profile dans la BDD fonctionne plus ou moins comme une insertion
		//il faut simplement préciser quel profile on doit mettre à jour grâce à l'ID
		ContentValues values = new ContentValues();
		values.put("firstName", profile.getFirstName());
		values.put("lastName", profile.getLastName());
		values.put("age", profile.getAge());
		values.put("relationshipStatus", profile.getRelationshipString());
		values.put("sexStatus", profile.getSexString());
		//on insère l'objet dans la BDD via le ContentValues
		db.update("table_profile", values, null, null);
		
		databaseHelper.cleanInterestTable(db);
		for (String interest : profile.getInterestsList()) {
			ContentValues value = new ContentValues();
			value.put("interest", interest);
			db.insert("table_interests", null, value);
		}	
	}
 
	public int removeProfile(String jid){
		//Suppression d'un profile de la BDD grâce à l'ID
		return db.delete("table_profile", "jid = " + jid, null);
	}
	
	public Profile retrieveProfile() {
		//Récupère dans un Cursor les valeurs correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
		Cursor c = db.query("table_profile", new String[] {"jid", "firstName", "lastName", "Age", "relationshipStatus", "SexStatus"}, null, null, null, null, null);
		Cursor d = db.query("table_interests", null, null, null, null, null, null, null);
		return cursorToProfile(c, d);
	}
 
	//Cette méthode permet de convertir un cursor en un profile
	private Profile cursorToProfile(Cursor c, Cursor d){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return null;
 
		//Sinon on se place sur le premier élément
		c.moveToFirst();
		//On créé un profile
		Profile profile = new Profile();
		//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
		profile.setJid(c.getString(0));
		profile.setFirstName(c.getString(1));
		profile.setLastName(c.getString(2));
		profile.setAge(c.getInt(3));
		String relationship = c.getString(4);
		String sex = c.getString(5);
		
		if (relationship.equals("Célibataire")) {
			profile.setRelationshipStatus(RelationshipStatus.SINGLE);
		} else if (relationship.equals("En couple")) {
			profile.setRelationshipStatus(RelationshipStatus.COUPLE);
		} else if (relationship.equals("Non divulguée")) {
			profile.setRelationshipStatus(RelationshipStatus.SECRET);
		}
		
		if (sex.equals("Homme")) {
			profile.setSexStatus(SexStatus.MAN);
		} else if (sex.equals("Femme")) {
			profile.setSexStatus(SexStatus.WOMAN);
		}
		c.close();
		
		if (d.getCount() != 0) {
			List<String> interestsList = new ArrayList<String>();
			int i;
			d.moveToFirst();
			for (i=0; i<d.getCount(); i++) {
				interestsList.add(d.getString(0));
				d.moveToNext();
			}
		profile.setInterestsList(interestsList)	;
		}
		d.close();
		
		// TODO récupérer les centre d'interets
		//On ferme le cursor
		
		//On retourne le profile
		return profile;
	}
}