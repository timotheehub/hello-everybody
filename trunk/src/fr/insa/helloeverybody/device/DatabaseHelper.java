package fr.insa.helloeverybody.device;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* Classe capable de créer, d'ouvrir et de fermer la base de données
-----------------------------------------------------------------------------*/
public class DatabaseHelper extends SQLiteOpenHelper {

	// Constantes
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "db_helloeverybody";
		
	// Constructeur
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Crée les tables de la base de données
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE table_userprofile (jid TEXT NOT NULL, "
				+ "firstName TEXT NOT NULL, lastName TEXT, age INT, "
				+ "relationshipStatus TEXT, sexStatus TEXT, password TEXT);");
		database.execSQL("CREATE TABLE table_interests (interest TEXT NOT NULL);");
		database.execSQL("CREATE TABLE table_contacts (jid TEXT NOT NULL, "
				+ "favorite INT, known INT, recommend INT);");
	}

	// Efface les anciennes tables si le numéro de version change
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS table_userprofile;");
		database.execSQL("DROP TABLE IF EXISTS table_interests;");
		database.execSQL("DROP TABLE IF EXISTS table_contacts;");
		onCreate(database);		
	}
}