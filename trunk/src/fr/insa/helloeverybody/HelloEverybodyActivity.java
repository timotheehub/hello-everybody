package fr.insa.helloeverybody;

import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.profile.ProfileActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class HelloEverybodyActivity extends TabActivity {
	
	public final static int CONVERSATION_LAUCHED = 1;
	public final static int DECONNECTION = 2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
		
		setContentView(R.layout.main);
    	
    	TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ProfileActivity.class);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("profil").setIndicator("Profil")
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ContactsListActivity.class);
        spec = tabHost.newTabSpec("contacts").setIndicator("Contacts")
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, ConversationsListActivity.class);
        spec = tabHost.newTabSpec("conversations").setIndicator("Conversations")
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(1);
        
    }
    
}