package fr.insa.helloeverybody.contacts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.SeparatedListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;

public class InviteContactActivity  extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	
	// Listes de contacts
	private ListView contactsListView;
	private List<Profile> favoritesList;
	private List<Profile> knownList;
	private List<Profile> recommendedList;
	private List<Profile> nearMeList;
	private ArrayList<String> members;
	private ArrayList<String> selectedList;

    // Appel a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.contacts_invite_list);
        
         members= getIntent().getStringArrayListExtra("members");
        
        // Recupere les listes de profiles
        final ContactsList contactsList = ContactsList.getInstance();
        /*contactsList.getFavoritesList().clear();
        contactsList.getKnownList().clear();
        contactsList.getRecommendedList().clear();
        contactsList.getNearMeList().clear();*/
        favoritesList = contactsList.getFavoritesList();
        knownList = contactsList.getKnownList();
        recommendedList = contactsList.getRecommendedList();
        nearMeList = contactsList.getNearMeList();
        selectedList= new ArrayList<String>();
        
        //Creation du profil utilisateur
        //TODO: Récupération du vraie profil
        profile = new Profile();
        profile.setFirstName("Prenom");
        profile.setLastName("Nom");
        
        //Création du gestionnaire des actions
        contactsActions = ContactsActions.getInstance(this, profile);
        contactsActions.register(this);
        
        //Arret des timers et demande de MAJ des Contacts
        contactsActions.stopScheduledUpdate();
        contactsActions.askUpdateContacts();
        
        //declarations des actions des boutons
        final Button inviteBtn = (Button) findViewById(R.id.btn_invite);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	//TODO: send an invite msg to the selected contacts
            /*	for(String selectedId:selectedList){
            		System.out.println("ID: "+selectedId);
            		System.out.println("Send message to: "+contactsList.getProfileById(Long.parseLong(selectedId)));
            		contactsList.getProfileById(Long.parseLong(selectedId));
            		
            	}*/
            	
            	setResult(8, new Intent().putStringArrayListExtra("toInvite", selectedList));
            	InviteContactActivity.this.finish();
            }
        });
        
        final Button cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	InviteContactActivity.this.finish();
            }
        });
        // Fenetre de chargement
       // loading = ProgressDialog.show(InviteContactActivity.this, "Chargement...", "Récupération des contacts", true);
    }
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> contactsList) {
		//loading.dismiss();
		// Lancement des timers GPS
		//contactsActions.contactsReceived();
      //  contactsActions.launchScheduledUpdate();
		
        //	nearMeList.clear();
		//nearMeList.addAll(contactsList);
        
		fillContactsView();
	}
    
	// Remplit les différentes listes de contacts
	private void fillContactsView() {
        // Adaptateur pour la liste de contacts
		final SeparatedListAdapter listAdapter = new SeparatedListAdapter(this);
		
		listAdapter.addSection(getString(R.string.favorites),
				getFavoritesAdapter(), getProfileIds(favoritesList));
		listAdapter.addSection(getString(R.string.known),
				getKnownAdapter(), getProfileIds(knownList));
		listAdapter.addSection(getString(R.string.recommended),
				getRecommendedAdapter(), getProfileIds(recommendedList));
		listAdapter.addSection(getString(R.string.near_me),
				getNearMeAdapter(), getProfileIds(nearMeList));
		
		// Mettre a jour la ListView
		contactsListView = (ListView) findViewById(R.id.contacts_invite_list);
		contactsListView.setAdapter(listAdapter);
		
		// Listener pour selectionner les contacts à inviter
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
    			if(!selectedList.contains(adapter.getItemIdAtPosition(position)+"")){
    				view.setBackgroundColor(Color.DKGRAY);
    				selectedList.add(adapter.getItemIdAtPosition(position)+"");
    			}else{
    				view.setBackgroundColor(Color.BLACK);
        			selectedList.remove(adapter.getItemIdAtPosition(position)+"");
        		}
        	}
         });
	}
	
	// Retourne la liste des identifiants
	private List<Long> getProfileIds(List<Profile> profilesList) {
		List<Long> profileIds = new ArrayList<Long>();
		
		for (Profile profile : profilesList) {
			profileIds.add(profile.getId());
		}
		
		return profileIds;
	}
	
	// Retourne l'adaptateur des favoris
	private SimpleAdapter getFavoritesAdapter() {
		List<Map<String, String>> favoritesAttributesList = new ArrayList<Map<String, String>>();
				
		Map<String, String> favoriteAttributesMap;
			for (Profile profile : favoritesList) {

			if(!members.contains(profile.getId().toString())){
				favoriteAttributesMap = new HashMap<String, String>();
				favoriteAttributesMap.put("firstName", profile.getFirstName());
				favoriteAttributesMap.put("lastName", profile.getLastName());
				favoriteAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
				favoritesAttributesList.add(favoriteAttributesMap);
			}
		}
        
        // Creation d'un SimpleAdapter qui se chargera de mettre
        // les favoris de la liste dans la vue contact_invite_item
        SimpleAdapter favoritesAdapter = new SimpleAdapter (this.getBaseContext(),
        		favoritesAttributesList, R.layout.contact_invite_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return favoritesAdapter;
	}
	
	// Retourne l'adaptateur des récents
	private SimpleAdapter getKnownAdapter() {
		List<Map<String, String>> knownAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> knownAttributesMap;
		for (Profile profile : knownList) {
			if(!members.contains(profile.getId().toString())){
				knownAttributesMap = new HashMap<String, String>();
				knownAttributesMap.put("firstName", profile.getFirstName());
				knownAttributesMap.put("lastName", profile.getLastName());
				knownAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
				knownAttributesList.add(knownAttributesMap);
			}
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les récents de la liste dans la vue contact_invite_item
        SimpleAdapter knownAdapter = new SimpleAdapter (this.getBaseContext(),
        		knownAttributesList, R.layout.contact_invite_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return knownAdapter;
	}
	
	// Retourne l'adaptateur des recommandés
	private SimpleAdapter getRecommendedAdapter() {
		List<Map<String, String>> recommendedAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> recommendedAttributesMap;
		for (Profile profile : recommendedList) {
			if(!members.contains(profile.getId().toString())){
				recommendedAttributesMap = new HashMap<String, String>();
				recommendedAttributesMap.put("firstName", profile.getFirstName());
				recommendedAttributesMap.put("lastName", profile.getLastName());
				recommendedAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
				recommendedAttributesList.add(recommendedAttributesMap);
			}
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les recommandés de la liste dans la vue contact_invite_item
        SimpleAdapter recommendedAdapter = new SimpleAdapter (this.getBaseContext(),
        		recommendedAttributesList, R.layout.contact_invite_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return recommendedAdapter;
	}
	
	// Retourne l'adaptateur des gens à proxmité
	private SimpleAdapter getNearMeAdapter() {
		List<Map<String, String>> nearMeAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> nearMeAttributesMap;
		for (Profile profile : nearMeList) {
			if(!members.contains(profile.getId().toString())){
				nearMeAttributesMap = new HashMap<String, String>();
				nearMeAttributesMap.put("firstName", profile.getFirstName());
				nearMeAttributesMap.put("lastName", profile.getLastName());
				nearMeAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
				nearMeAttributesList.add(nearMeAttributesMap);
			}
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les personnes proches de la liste dans la vue contact_invite_item
        SimpleAdapter nearMeAdapter = new SimpleAdapter (this.getBaseContext(),
        		nearMeAttributesList, R.layout.contact_invite_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
		return nearMeAdapter;
	}
}