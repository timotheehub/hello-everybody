package fr.insa.helloeverybody.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.insa.helloeverybody.R;

public class AgePreferenceActivity extends Activity {
	
	public static final String KEY_FILTER_AGE = "filter_age";
	public static final String KEY_AGE_FROM = "age_from";
	public static final String KEY_AGE_TO = "age_to";
	
	private SharedPreferences sharedPreferences;
	private CheckBox ageCheckBox;
	private LinearLayout ageRangeLayout;
	private EditText ageFromEditText;
	private EditText ageToEditText;
	
	// Crée l'activité
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.age_preference);
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		
		// Element d'interface
		ageCheckBox = (CheckBox) findViewById(R.id.age_checkbox);
		ageRangeLayout = (LinearLayout) findViewById(R.id.age_range_layout);
		ageFromEditText = (EditText) findViewById(R.id.from_age_edittext);
		ageToEditText = (EditText) findViewById(R.id.to_age_edittext);
		
		// Checkbox
		boolean hasFilter = sharedPreferences.getBoolean(KEY_FILTER_AGE, false);
		ageCheckBox.setChecked(hasFilter);
		ageRangeLayout.setVisibility(hasFilter ? View.VISIBLE : View.INVISIBLE);
		
		// Tranche d'âge
		ageFromEditText.setText(String.valueOf(sharedPreferences.getInt(KEY_AGE_FROM, 18)));
		ageToEditText.setText(String.valueOf(sharedPreferences.getInt(KEY_AGE_TO, 25)));
	}
	
	// Création du menu
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profil, menu);
 
        return true;
     }
 
	// Execute les actions du menu
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        	case R.id.accept:
        		acceptModifications();
        		return true;
        	case R.id.cancel:
        		cancelModifications();
        		return true;
    	}
    	return false;
    }
	
	// La checkbox est cliquée
	public void ageCheckBoxClick(View view) {
		ageRangeLayout.setVisibility(ageCheckBox.isChecked() ? View.VISIBLE : View.INVISIBLE);
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
		Editor editor = sharedPreferences.edit();
		
		// Tranche d'âge
		if (ageCheckBox.isChecked()) {
			int fromAge;
			int toAge;
			
			try {
				fromAge = Integer.parseInt(ageFromEditText.getText().toString());
				toAge = Integer.parseInt(ageToEditText.getText().toString());
			}
			catch (NumberFormatException nfe) {
				showError();
				return;
			}
			
			// Affiche une erreur si l'âge minimum est plus grand que l'âge maximum
			if (fromAge > toAge) {
				showError();
				return;
			}
			
			editor.putInt(KEY_AGE_FROM, fromAge);
			editor.putInt(KEY_AGE_TO, toAge);
		}
				
		// Savegarder les changements
		editor.putBoolean(KEY_FILTER_AGE, ageCheckBox.isChecked());
		editor.commit();
		
		// Quitter l'écran
		finish();
	}
	
	// Annuler les modifications
	private void cancelModifications() {
		finish();
	}
	
	// Afficher une erreur
	private void showError() {
		TextView errorEditText = (TextView) findViewById(R.id.error_textview);
		errorEditText.setVisibility(View.VISIBLE);
		
		// Texte vide
		if ((ageFromEditText.getText() == null) 
				|| (ageFromEditText.getText().length() == 0)) {
			errorEditText.setText(getString(R.string.min_age_empty));
			return;
		}
		if ((ageToEditText.getText() == null)
				|| (ageToEditText.getText().length() == 0)) {
			errorEditText.setText(getString(R.string.max_age_empty));
			return;
		}
		
		// Texte n'est pas un entier
		int fromAge;
		int toAge;
		
		try {
			fromAge = Integer.parseInt(ageFromEditText.getText().toString());
			toAge = Integer.parseInt(ageToEditText.getText().toString());
		}
		catch (NumberFormatException nfe) {
			errorEditText.setText(getString(R.string.age_string));
			return;
		}
		
		// Age minimum plus grand que l'age maximum
		if (fromAge > toAge) {
			errorEditText.setText(getString(R.string.min_greater_than_max));
			return;
		}
		
		// Autre error
		errorEditText.setText(getString(R.string.age_error));
	}
}
