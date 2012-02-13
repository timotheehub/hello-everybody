package fr.insa.helloeverybody;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class HelloEverybodyActivity extends TabActivity {
	
	private ProgressDialog loading;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Progress dialog and thread for searching contacts
        loading = ProgressDialog.show(HelloEverybodyActivity.this,
        		"Chargement...", "Récupération des contacts", true);
    	new Thread() {
    		@Override
    		public void run() {
    			//TODO: récupération de la liste des contacts à proximité
    			try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			loading.dismiss();
		    }
		}.start();
        
		
		setContentView(R.layout.main);
    	
    	TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ProfilActivity.class);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("profil").setIndicator("Profil")
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ContactsActivity.class);
        spec = tabHost.newTabSpec("contacts").setIndicator("Contacts")
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, ConversationsActivity.class);
        spec = tabHost.newTabSpec("conversations").setIndicator("Conversations")
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(1);
        
    }
    
}