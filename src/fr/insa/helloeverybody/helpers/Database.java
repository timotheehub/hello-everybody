package fr.insa.helloeverybody.helpers;

import fr.insa.helloeverybody.models.Profile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Database {
 
	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "db_profile";
 
	private SQLiteDatabase db;
 
	private DatabaseHelper databaseHelper;
 
	public Database(Context context){
		//On crée la BDD et sa table
		databaseHelper = new DatabaseHelper(context, NOM_BDD, null, VERSION_BDD);
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
		values.put("First_name", profile.getFirstName());
		values.put("Last_name", profile.getLastName());
		values.put("Age", profile.getAge());
		values.put("Relationship", profile.getRelationshipString());
		values.put("Sex", profile.getSexString());
		//on insère l'objet dans la BDD via le ContentValues
		db.insert("table_profile", null, values);
		
		for (String interest : profile.getInterestsList()) {
			ContentValues value = new ContentValues();
			value.put("interest", interest);
			db.insert("table_interests", null, value);
		}			
	}
 
	public void updateProfile(int id, Profile profile){
		//La mise à jour d'un profile dans la BDD fonctionne plus ou moins comme une insertion
		//il faut simplement préciser quel profile on doit mettre à jour grâce à l'ID
		ContentValues values = new ContentValues();
		values.put("First_name", profile.getFirstName());
		values.put("Last_name", profile.getLastName());
		values.put("Age", profile.getAge());
		values.put("Relationship", profile.getRelationshipString());
		values.put("Sex", profile.getSexString());
		//on insère l'objet dans la BDD via le ContentValues
		db.update("table_profile", values, "ID = " +id, null);
		
		databaseHelper.cleanInterestTable(db);
		for (String interest : profile.getInterestsList()) {
			ContentValues value = new ContentValues();
			value.put("interest", interest);
			db.insert("table_interests", null, value);
		}	
	}
 
	public int removeProfile(int id){
		//Suppression d'un profile de la BDD grâce à l'ID
		return db.delete("table_profile", "ID = " +id, null);
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
		//On ferme le cursor
		c.close();
 
		//On retourne le profile
		return profile;
	}
}