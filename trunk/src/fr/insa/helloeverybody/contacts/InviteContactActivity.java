package fr.insa.helloeverybody.contacts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.*;
import fr.insa.helloeverybody.models.*;

import android.app.Activity;
import android.app.ProgressDialog;
//import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class InviteContactActivity  extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	private ProgressDialog loading;
	
	// Listes de contacts
	private ListView contactsListView;
	private List<Profile> favoritesList;
	private List<Profile> knownList;
	private List<Profile> recommendedList;
	private List<Profile> nearMeList;
	
	private List<Long> selectedList;

    // Appele a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_invite_list);
        // Recupere les listes de profiles
        ContactsList contactsList = ContactsList.getInstance();
        favoritesList = contactsList.getFavoritesList();
        knownList = contactsList.getKnownList();
        recommendedList = contactsList.getRecommendedList();
        nearMeList = contactsList.getNearMeList();
        selectedList= new ArrayList<Long>();
        //Creation du profil utilisateur
        //TODO: Récupération du vraie profil
        profile = new Profile();
        profile.setFirstName("Prenom");
        profile.setLastName("Nom");
        
        //Création du gestionnaire des actions
        contactsActions = new ContactsActions(this, profile, this);
        
        //Demande de MAJ des Contacts
        contactsActions.askUpdateContacts();
        contactsActions.launchScheduledUpdate();
        
        //declarations des actions des boutons
        final Button inviteBtn = (Button) findViewById(R.id.btn_invite);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	//TODO: get selected contacts and invite
            }
        });
        
        /* final CheckBox ch=(CheckBox) findViewById(R.id.contact_selected);
        ch.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                {
                    
                }
                else {
                	
                }

            }
        });*/
        
        final Button cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	InviteContactActivity.this.finish();
            }
        });
        
        // Fenetre de chargement
        loading = ProgressDialog.show(InviteContactActivity.this, "Chargement...", "Récupération des contacts", true);
    }
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> contactsList) {
		loading.dismiss();
		nearMeList.addAll(contactsList);
        
		
		fillContactsView();
	}
    
	// Remplit les différentes listes de contacts
	private void fillContactsView() {
		
		// Intent pour lancer une activite
	//	final Intent intent; 
    //    intent = new Intent().setClass(this, ContactProfileActivity.class);

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
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
        //	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        		CheckBox c= (CheckBox) contactsListView.getChildAt(position).findViewById(R.id.contact_selected);
        		c.performClick();
        		if(c.isChecked())
        		{
        			if(!selectedList.contains(adapter.getItemIdAtPosition(position)))
        				selectedList.add(adapter.getItemIdAtPosition(position));
        		}
        		else{
        			selectedList.remove(adapter.getItemIdAtPosition(position));
        		}
            	//intent.putExtra("id", adapter.getItemIdAtPosition(position));
        		
        		//startActivity(intent);
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
			favoriteAttributesMap = new HashMap<String, String>();
			favoriteAttributesMap.put("firstName", profile.getFirstName());
			favoriteAttributesMap.put("lastName", profile.getLastName());
			favoriteAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			favoritesAttributesList.add(favoriteAttributesMap);
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
			knownAttributesMap = new HashMap<String, String>();
			knownAttributesMap.put("firstName", profile.getFirstName());
			knownAttributesMap.put("lastName", profile.getLastName());
			knownAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			knownAttributesList.add(knownAttributesMap);
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
			recommendedAttributesMap = new HashMap<String, String>();
			recommendedAttributesMap.put("firstName", profile.getFirstName());
			recommendedAttributesMap.put("lastName", profile.getLastName());
			recommendedAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			recommendedAttributesList.add(recommendedAttributesMap);
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
			nearMeAttributesMap = new HashMap<String, String>();
			nearMeAttributesMap.put("firstName", profile.getFirstName());
			nearMeAttributesMap.put("lastName", profile.getLastName());
			nearMeAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			nearMeAttributesList.add(nearMeAttributesMap);
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
