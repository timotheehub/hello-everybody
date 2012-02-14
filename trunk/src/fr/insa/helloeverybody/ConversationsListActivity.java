package fr.insa.helloeverybody;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConversationsListActivity extends Activity {
	
	// Listes de conversations
	private List<Conversation> pendingConversationsList = new ArrayList<Conversation>();
	private List<Conversation> publicConversationsList = new ArrayList<Conversation>();
	
	// ListView des contacts
	private ListView conversationsListView;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.conversations_list);
		
		fillPendingConversationsList();
		fillPublicConversationsList();
		fillConversationsView();

		/*final Intent intent;  // Reusable Intent for each tab
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ConversationActivity.class);
        
        Button button = new Button(this);
        button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		startActivity(intent);
        	}
        });
        button.setText("Test");
        setContentView(button);*/
    }
    
    
    
    // M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML sp�cifier en un objet Menu
        inflater.inflate(R.menu.conversations, menu);
 
        return true;
    }
 
    
    
    // M�thode qui se d�clenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fen�tre des param�tres
               Toast.makeText(ConversationsListActivity.this, "Param�tres Conversations", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.add_public_group:
            	// Cr�er un groupe publique
                Toast.makeText(ConversationsListActivity.this, "Cr�ation d'un groupe publique", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// D�connexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
    
    
    
    // Creer la vue des conversations
    private void fillConversationsView() {
    	SeparatedListAdapter listAdapter = new SeparatedListAdapter(this);

		listAdapter.addSection(getString(R.string.pending), getPendingAdapter());
		listAdapter.addSection(getString(R.string.opened_to_all), getPublicAdapter());
		
		conversationsListView = (ListView) findViewById(R.id.conversations_list);
		conversationsListView.setAdapter(listAdapter);
		
		final Intent intent;  // Reusable Intent for each tab
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ConversationActivity.class);
		
        conversationsListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On r�cup�re la HashMap
        		Map<String, String> map = (Map<String, String>) adapter.getItemAtPosition(position);

        		// On affiche le bouton cliqu�
        		if (map != null)
        		{
        			startActivity(intent);
        		}
        	}
         });
    }
    
    
    
    // Retourne l'adaptateur des discussions en cours
    private SimpleAdapter getPendingAdapter() {
    	List<Map<String, String>> pendingList = new ArrayList<Map<String, String>>();
		
		Map<String, String> pendingAttributesMap;
		
		for (Conversation conversation : pendingConversationsList) {
			pendingAttributesMap = new HashMap<String, String>();
			pendingAttributesMap.put("title", conversation.getTitle());
			pendingList.add(pendingAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			pendingList, R.layout.conversation_item,
        		new String[] {"title"}, 
        		new int[] {R.id.title});
    	
    	return pendingAdapter;
    }
    
    
    // Retourne l'adaptateur des discussions publiques
    private SimpleAdapter getPublicAdapter() {
    	List<Map<String, String>> publicList = new ArrayList<Map<String, String>>();
		Map<String, String> publicAttributesMap;
		
		for (Conversation conversation : publicConversationsList) {
			publicAttributesMap = new HashMap<String, String>();
			publicAttributesMap.put("title", conversation.getTitle());
			publicList.add(publicAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			publicList, R.layout.conversation_item,
        		new String[] {"title"}, 
        		new int[] {R.id.title});
    	
    	return pendingAdapter;
    }
    
    
    // Creer les conversations en cours
    private void fillPendingConversationsList() {
    	pendingConversationsList.add(new Conversation(false, "Arthur, Julian, Timoth�e"));
    	pendingConversationsList.add(new Conversation(false, "Bob l'�ponge"));
    	pendingConversationsList.add(new Conversation(true, "Conf�rence Marketing"));
    }
    
    
    
    // Creer les conversations publics
    private void fillPublicConversationsList() {
    	publicConversationsList.add(new Conversation(true, "Amphi de Biennier"));
    }
}