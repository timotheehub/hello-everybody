package fr.insa.helloeverybody.preferences;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import fr.insa.helloeverybody.R;

public class UserPreferencesActivity extends PreferenceActivity 
						implements OnSharedPreferenceChangeListener {
	public static final String KEY_DISTANCE_PREFERENCE = "distance_preference";
	public static final String KEY_AGE_PREFERENCE = "age_preference";
	public static final String DEFAULT_DISTANCE = "50000";
	private ListPreference distancePreference;
	private Preference agePreference;
	
	// Création de l'activité
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_preferences);
        
        // Préférence de distance
		distancePreference = (ListPreference) findPreference(KEY_DISTANCE_PREFERENCE);
		if (distancePreference.getValue() == null) {
			distancePreference.setValue(DEFAULT_DISTANCE);
		}
		
		// Préférence d'âge
		final Intent ageActivity = new Intent(getBaseContext(), AgePreferenceActivity.class);
		agePreference = findPreference(KEY_AGE_PREFERENCE);
		agePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
		        startActivity(ageActivity);
				return true;
			}
		});
	}
	
	// Mise à jour de l'activité
	@Override
	public void onResume() {
		super.onResume();
		updateDistanceSummary();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);  
	}
	
	// Pause de l'activité
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);  
	}

	// Met a jour l'affichage lors d'un changement de préférence
	public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_DISTANCE_PREFERENCE)) {
			updateDistanceSummary();
		}
	}
	
	// Met a jour l'affichage de la distance
	private void updateDistanceSummary() {
		distancePreference.setSummary(
				getString(R.string.contacts_visibility_up_to)
					+ " " + distancePreference.getEntry());
	}
}
