package fr.insa.helloeverybody.preferences;

import android.preference.PreferenceActivity;
import android.os.Bundle;

import fr.insa.helloeverybody.R;

public class UserPreferencesActivity extends PreferenceActivity {
	 @Override
     public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.user_preferences);
	 }
}
