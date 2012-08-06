package fr.insa.helloeverybody.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.viewmodels.ConversationList;

/* Adaptateur pour les listes de conversations
-----------------------------------------------------------------------------*/
public class SeparatedConversationListAdapter extends SeparatedListAdapter
{
	private Context context;
	
	// Constructeur public
	public SeparatedConversationListAdapter(Context context) {
		super(context);
		this.context = context;
		setAdapter();
	}
	
	// Initialise l'adaptateur
	private void setAdapter() {
		
		// Récuperer les listes de conversations
        ConversationList conversationList = ConversationList.getInstance();
        List<Conversation> pendingConversationList = 
        		conversationList.getPendingRoomList();
        List<Conversation> publicConversationList = 
        		conversationList.getPublicRoomList();
        
        // Ajouter les conversations aux adaptateurs
		addSection(context.getString(R.string.pending),
				getConversationAdapter(pendingConversationList),
				getProfileIds(pendingConversationList));
		addSection(context.getString(R.string.opened_to_all),
				getConversationAdapter(publicConversationList),
				getProfileIds(publicConversationList));
	}
	
	// Retourne un adaptateur basée sur une liste
	private ConversationAdapter getConversationAdapter(List<Conversation> conversationList) {
		return new ConversationAdapter(context, R.layout.conversation_item, conversationList);
	}
	
	// Retourne la liste des identifiants
	private List<String> getProfileIds(List<Conversation> conversationList) {
		List<String> converstionIdList = new ArrayList<String>();
		
		for (Conversation conversation : conversationList) {
			converstionIdList.add(conversation.getRoomName());
		}
		
		return converstionIdList;
	}
}