package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Conversation {
	
	// Attributs
	private boolean isPublic = false;
	private Long id;
	private String title;
	private String destID; //A qui les messages doivent etre envoyes
	private List<Profile> members = new ArrayList<Profile>();
	private List<ConversationMessage> messages = new ArrayList<ConversationMessage>();
	private int nbUnreadMessages = 0;
	
	
	
	// Constructeurs
	public Conversation() {
		setDefault();
	}
	
	public Conversation(boolean isPublic, String title) {
		setDefault();
		this.isPublic = isPublic;
		this.title = title;
	}
	
	private void setDefault() {
		id = new Random().nextLong();
	}

	public void setDestID(String destID){
		this.destID=destID;
	}
		
	public String getDestID(){
		return destID;
	}

	// Liste des participants 
	public void addMember(Profile profile) {
		members.add(profile);
	}
	
	public boolean removeMember(Profile profile) {
		return members.remove(profile);
	}
	
	
	
	
	// Liste des messages
		public void addMessage(ConversationMessage message) {
			messages.add(message);
		}
		
		public boolean removeMember(ConversationMessage message) {
			return messages.remove(message);
		}
	

	
	
	// Getters et setters
	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getNbUnreadMessages() {
		return nbUnreadMessages;
	}

	public void setNbUnreadMessages(int nbUnreadMessages) {
		this.nbUnreadMessages = nbUnreadMessages;
	}

	public List<ConversationMessage> getMessages() {
		return messages;
	}
	
	
}
