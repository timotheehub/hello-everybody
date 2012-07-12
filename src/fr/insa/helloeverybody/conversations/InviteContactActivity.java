package fr.insa.helloeverybody.conversations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.controls.SeparatedInviteContactsAdapter;

public class InviteContactActivity extends Activity {
	
	// Listes de contacts
	private ListView contactsListView;
	private Set<String> memberJidSet;
	private ArrayList<String> selectedList;
	private SeparatedInviteContactsAdapter contactListAdapter;

    // Appel a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_invite_list);
        
        List<String> memberJidList =
        		getIntent().getStringArrayListExtra("memberJidList");
        memberJidSet = new HashSet<String>(memberJidList);
        final String roomName = getIntent().getStringExtra("roomName");
        
        // declarations des actions des boutons
        final Button inviteBtn = (Button) findViewById(R.id.btn_invite);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent data = new Intent();
            	data.putStringArrayListExtra("toInvite", selectedList);
            	data.putExtra("roomName", roomName);
            	setResult(RESULT_OK, data);
            	finish();
            }
        });
        
        final Button cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	setResult(0,null);
            	finish();
            }
        });
        
        // Recupere les listes de profiles
        selectedList= new ArrayList<String>();
        updateContactsView();
        
		// Listener pour selectionner les contacts à inviter
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            	String profileJid = contactListAdapter.getStringId(position);
    			if (!selectedList.contains(profileJid)) {
    				view.setBackgroundColor(Color.DKGRAY);
    				selectedList.add(profileJid);
    			} else {
    				view.setBackgroundColor(Color.BLACK);
        			selectedList.remove(profileJid);
        		}
        	}
         });
    }
    
	// Met à jour la vue des listes de contacts
	private void updateContactsView() {
		contactListAdapter = 
				new SeparatedInviteContactsAdapter(this, memberJidSet);
		contactsListView = (ListView) findViewById(R.id.contacts_invite_list);
		contactsListView.setAdapter(contactListAdapter);
	}
}
