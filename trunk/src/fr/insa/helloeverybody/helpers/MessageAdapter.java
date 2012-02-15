package fr.insa.helloeverybody.helpers;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.conversations.ConversationMessage;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Permet d'afficher les objets Message avec une ListView
 */
public class MessageAdapter extends ArrayAdapter<ConversationMessage> {
	
	private Context context;
	 
	public MessageAdapter(Context context, int textViewResourceId) {
         super(context, textViewResourceId);
         this.context = context;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
            	// On crée une nouvelle View de mise en forme "message"
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.message, null);
            }
            ConversationMessage message = getItem(position);
            if (message != null) {
            		// Changement de la couleur d'arrière plan en fonction de l'origine du message
            		if (message.getContact().isUser()) {
            			view.setBackgroundResource(R.color.user_bg_color);
            		} else {
            			view.setBackgroundResource(R.color.contact_bg_color);
            		}
            		TextView content = (TextView) view.findViewById(R.id.message);
                    TextView name = (TextView) view.findViewById(R.id.name);
                    ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
                    if (content != null) {
                          content.setText(message.getMessage());                            
                    }
                    if(name != null){
                          name.setText(message.getContact().getFirstName());
                    }                       
			        if(avatar != null){
			            avatar.setImageResource(message.getContact().getAvatar());
			        }
            }
            return view;
    }

}
