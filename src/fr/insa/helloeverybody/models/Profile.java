package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Profile {
	
	// Attributs
	private boolean user;
	private int avatar;
	private String firstName;
	private String lastName;
	private String ip;
	private RelationshipStatus relationshipStatus;
	private List<String> interestsList;
	
	
	
	// Constructeurs
	public Profile() {
		interestsList = Collections.synchronizedList(new ArrayList<String>());
	}
	
	public Profile(String firstName, String lastName, String ip) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.ip = ip;
	}
	
	public Profile(int avatar, String firstName, String lastName) {
		super();
		this.avatar = avatar;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	// Retourne la liste d'interets
	public List<String> getInterestsList() {
		return interestsList;
	}
	
	
	
	// Getters et setters
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String name) {
		this.lastName = name;
	}
	
	public int getAvatar() {
		return avatar;
	}
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}
	
	public boolean isUser() {
		return user;
	}
	
	public void setUser(boolean user) {
		this.user = user;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public RelationshipStatus getRelationshipStatus() {
		return relationshipStatus;
	}

	public void setRelationshipStatus(RelationshipStatus relationshipStatus) {
		this.relationshipStatus = relationshipStatus;
	}
	
	public String getRelationshipString() {
		return relationshipStatus.toString();
	}
}
