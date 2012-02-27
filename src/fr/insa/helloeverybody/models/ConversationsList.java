package fr.insa.helloeverybody.models;

import java.util.LinkedList;
import java.util.Collections;
import java.util.List;

public class ConversationsList {

	// Singleton
	private static ConversationsList instance = null;
	
	// Attributes
	private List<Conversation> publicList;
	private List<Conversation> privateList;
	private List<Conversation> openList;
	
	// Constructeur privee
	private ConversationsList() {
		publicList = Collections.synchronizedList(new LinkedList<Conversation>());
		privateList = Collections.synchronizedList(new LinkedList<Conversation>()); 
		openList = Collections.synchronizedList(new LinkedList<Conversation>()); 
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ConversationsList getInstance() {
		if (instance == null) {
			instance = new ConversationsList();
		}
		return instance;
	}
	
	// Retourne la liste des conversation publiques
	public List<Conversation> getPublicList() {
		return publicList;
	}
	
	// Retourne la liste des conversations privées
	public List<Conversation> getPrivateList() {
		return privateList;
	}
	
	// Retourne la liste des conversations en cours
	public List<Conversation> getOpenList() {
		return openList;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Conversation getConversationById(Long id) {
		for (Conversation conversation : publicList) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		for (Conversation conversation : privateList) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		for (Conversation conversation : openList) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		return null;
	}
}
