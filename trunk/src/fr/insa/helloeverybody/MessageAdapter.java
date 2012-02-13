package fr.insa.helloeverybody;

import java.util.ArrayList;

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
public class MessageAdapter extends ArrayAdapter<Message> {
	
	private Context context;
	private ArrayList<Message> items;
	 
	public MessageAdapter(Context context, int textViewResourceId, ArrayList<Message> items) {
         super(context, textViewResourceId, items);
         this.items = items;
         this.context = context;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
            	// On cr�e une nouvelle View de mise en forme "message"
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.message, null);
            }
            Message message = items.get(position);
            if (message != null) {
            		// Changement de la couleur d'arri�re plan en fonction de l'origine du message
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
                          name.setText(message.getContact().getPrenom()+" "+context.getString(R.string.say));
                    }                       
			        if(avatar != null){
			            avatar.setImageResource(message.getContact().getAvatar());
			        }
            }
            return view;
    }

}
