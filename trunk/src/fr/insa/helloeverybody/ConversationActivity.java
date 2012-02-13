package fr.insa.helloeverybody;


import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ConversationActivity extends Activity {
	

    // Layout Views
    private ListView mConversationView;

    // Array adapter for the conversation thread
    private MessageAdapter mConversationMessageAdapter;
	
    // Liste des messages d'une conversation
    private ArrayList<Message> mConversationArrayList;
    
    // Profil de l'utilisateur
    private Profil userProfil;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation);
        
        mConversationArrayList = new ArrayList<Message>();
        
    // Création du profil de l'utilisateur
        userProfil = new Profil();
        userProfil.setAvatar(R.drawable.default_profile_icon);
        userProfil.setPrenom("Moi");
        userProfil.setUser(true);
        
    // Test - START  
        
        final Profil bob = new Profil();
        bob.setAvatar(R.drawable.sponge_bob);
        bob.setNom("L'Eponge)");
        bob.setPrenom("Bob");
        bob.setUser(false);
        
        Message essai = new Message();
        essai.setContact(bob);
        essai.setMessage("Hello World !");
        
        mConversationArrayList.add(essai);
        
     // Test - END
        
     // Initialize the array adapter for the conversation thread
        mConversationMessageAdapter = new MessageAdapter(this, R.layout.message, mConversationArrayList);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationMessageAdapter);
        
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                EditText view = (EditText) findViewById(R.id.edit_text_out);
                sendMessage(view.getText().toString());
                view.setText("");
            }
        });
    }
    
    public void sendMessage(String content) {
        Message monMessage = new Message();
        monMessage.setContact(userProfil);
        monMessage.setMessage(content);
        mConversationMessageAdapter.add(monMessage);
    }
    
}
