package fr.insa.helloeverybody.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import fr.insa.helloeverybody.R;

public class AgePreferenceActivity extends Activity {
	
	public static String KEY_FILTER_AGE = "filter_age";
	public static String KEY_AGE_FROM = "age_from";
	public static String KEY_AGE_TO = "age_to";
	private SharedPreferences sharedPreferences;
	
	// Crée l'activité
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.age_preference);
		
		// Has filter
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		boolean hasFilter = sharedPreferences.getBoolean(KEY_FILTER_AGE, false);
		CheckBox ageCheckBox = (CheckBox) findViewById(R.id.age_checkbox);
		ageCheckBox.setChecked(hasFilter);
		LinearLayout ageRangeLayout = (LinearLayout) findViewById(R.id.age_range_layout);
		ageRangeLayout.setVisibility(hasFilter ? View.VISIBLE : View.INVISIBLE);
		
	}
	
	// La checkbox est cliquée
	public void ageCheckBoxClick(View view) {
		
	}
	
	// Le bouton "Valider" est cliqué
	public void acceptButtonClick(View view) {
		acceptModifications();
	}
	
	// Le bouton "Annuler" est cliqué
	public void cancelButtonClick(View view) {
		cancelModifications();
	}
	
	// Accepter les modifications
	private void acceptModifications() {
		finish();
	}
	
	// Annuler les modifications
	private void cancelModifications() {
		finish();
	}
}
