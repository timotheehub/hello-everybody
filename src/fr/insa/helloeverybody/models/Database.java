package fr.insa.helloeverybody.models;

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
		values.put("ID", profile.getId());
		values.put("firstName", profile.getFirstName());
		values.put("lastName", profile.getLastName());
		values.put("age", profile.getAge());
		values.put("relationshipStatus", profile.getRelationshipString());
		values.put("sexStatus", profile.getSexString());
//		values.put("Sex", profile.getSexString());
		//on insère l'objet dans la BDD via le ContentValues
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
		db.update("table_profile", values, "ID = 0", null);
		
//		databaseHelper.cleanInterestTable(db);
//		for (String interest : profile.getInterestsList()) {
//			ContentValues value = new ContentValues();
//			value.put("interest", interest);
//			db.insert("table_interests", null, value);
//		}	
	}
 
	public int removeProfile(int id){
		//Suppression d'un profile de la BDD grâce à l'ID
		return db.delete("table_profile", "ID = " +id, null);
	}
	
	public Profile retrieveProfile(Long id) {
		//Récupère dans un Cursor les valeurs correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
		Cursor c = db.query("table_profile", new String[] {"ID", "firstName", "lastName", "Age", "relationshipStatus", "SexStatus"}, "ID" + " LIKE \"" + id +"\"", null, null, null, null);
		return cursorToProfile(c);
	}
 
	//Cette méthode permet de convertir un cursor en un profile
	private Profile cursorToProfile(Cursor c){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c.getCount() == 0)
			return null;
 
		//Sinon on se place sur le premier élément
		c.moveToFirst();
		//On créé un profile
		Profile profile = new Profile();
		//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
		profile.setId(c.getLong(0));
		profile.setFirstName(c.getString(1));
		profile.setLastName(c.getString(2));
		profile.setAge(c.getInt(3));
//		profile.setRelationshipStatus(c.getString(4));
//		profile.setSexStatus(c.getString(5));
		//On ferme le cursor
		c.close();
		Log.d("profile", profile.getFirstName());
 
		//On retourne le profile
		return profile;
	}
}