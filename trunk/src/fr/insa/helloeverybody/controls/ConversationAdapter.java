package fr.insa.helloeverybody.controls;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Conversation;

public class ConversationAdapter extends ArrayAdapter<Conversation> {
	
	private Context context;

	public ConversationAdapter(Context context, int textViewResourceId, List<Conversation> conversationList) {
		super(context, textViewResourceId, conversationList);
		this.context = context;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

    	// Création d'une nouvelle View de mise en forme pour les contacts
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) 
            		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.conversation_item, null);
        }
        
        // Affectation des variables du profile
        Conversation conversation = getItem(position);
        if (conversation != null) {

            // Clef pour signaler une conversation privée
            ImageView privateKey = (ImageView) view.findViewById(R.id.private_conversation);               
	        if (privateKey != null){
	        	if (conversation.isPublic()) {
	        		privateKey.setImageResource(R.drawable.empty_key);
	        	}
	        	else {
	        		privateKey.setImageResource(R.drawable.key);
	        	}
	        }
	        
    		// Titre de la conversation
    		TextView title = (TextView) view.findViewById(R.id.title);
            if (title != null) {
            	title.setText(conversation.getRoomSubject());                            
            }
            
            // Nombre de messages non lus
            TextView unreadMessages = (TextView) view.findViewById(R.id.unread_message);
            if (unreadMessages != null) {
            	int nbUnreadMessages = conversation.getNbUnreadMessages();
            	String unreadMessagesStr = (nbUnreadMessages > 0) ?
            			Integer.toString(nbUnreadMessages) : "";
            	unreadMessages.setText(unreadMessagesStr);
            }       
        }
        
        return view;
    }
}
