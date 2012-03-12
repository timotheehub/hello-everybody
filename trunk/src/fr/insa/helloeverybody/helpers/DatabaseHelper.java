package fr.insa.helloeverybody.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "db_profile";
	private static final String CREATE_TABLE1 = "CREATE TABLE table_profile (jid TEXT NOT NULL, firstName TEXT NOT NULL, lastName TEXT, age INT, relationshipStatus TEXT, sexStatus TEXT, password TEXT)";
	private static final String CREATE_TABLE2 = "CREATE TABLE table_interests (interest TEXT NOT NULL)";
	private static final String CREATE_TABLE3 = "CREATE TABLE table_contacts (jid TEXT NOT NULL, favorite INT, known INT, recommend INT)";
	
	public DatabaseHelper(Context context, String nomBdd, SQLiteDatabase.CursorFactory factory, int versionBdd) {
		super(context, nomBdd, factory, versionBdd);
	}
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE1);
		db.execSQL(CREATE_TABLE2);
		db.execSQL(CREATE_TABLE3);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE table_interests;");
		db.execSQL("DROP TABLE table_profile;");
		db.execSQL("DROP TABLE table_contacts;");
		onCreate(db);		
	}
	
	public void cleanInterestTable(SQLiteDatabase db) {
		db.delete("table_interests", null, null);
	}
}