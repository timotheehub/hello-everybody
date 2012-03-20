package fr.insa.helloeverybody.helpers;

import java.util.List;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Profile;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Profile> {
	
	private Context context;

	public ContactAdapter(Context context, int textViewResourceId, List<Profile> profilesList) {
		super(context, textViewResourceId, profilesList);
		this.context = context;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

        	// Création d'une nouvelle View de mise en forme pour les contacts
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.contact_item, null);
            }
            
            // Affectation des variables du profile
            Profile profile = getItem(position);
            if (profile != null) {
            		// Prénom
            		TextView firstName = (TextView) view.findViewById(R.id.first_name);
                    if (firstName != null) {
                    	firstName.setText(profile.getFirstName());                            
                    }
                    
                    // Nom
                    TextView lastName = (TextView) view.findViewById(R.id.last_name);
                    if(lastName != null){
                          lastName.setText(profile.getLastName());
                    }       
                    
                    // Avatar
                    ImageView avatar = (ImageView) view.findViewById(R.id.picture);               
			        if(avatar != null){
			        	Bitmap avatarBitmap = profile.getAvatar();
			        	if (avatarBitmap != null) {
				            avatar.setImageBitmap(avatarBitmap);
			        	}
			        	else {
			        		avatar.setImageResource(Profile.DEFAULT_AVATAR);
			        	}
			        }
            }
            
            return view;
    }

}
