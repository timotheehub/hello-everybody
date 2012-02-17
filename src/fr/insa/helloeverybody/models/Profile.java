package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.insa.helloeverybody.R;


public class Profile {
	
	// Attributs
	private boolean user;
	private Long id;
	private Integer avatar;
	private Integer age;
	private String firstName;
	private String lastName;
	private String ip;
	private RelationshipStatus relationshipStatus;
	private SexStatus sexStatus;
	private List<String> interestsList;
	
	
	
	// Constructeurs
	public Profile() {
		setDefault();
	}
	
	public Profile(String firstName, String lastName, String ip) {
		setDefault();
		this.firstName = firstName;
		this.lastName = lastName;
		this.ip = ip;
	}
	
	public Profile(int avatar, String firstName, String lastName) {
		setDefault();
		this.avatar = avatar;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	private void setDefault() {
		id = new Random().nextLong();
		avatar = R.drawable.default_profile_icon;
		age = 18;
		interestsList = Collections.synchronizedList(new ArrayList<String>());
		relationshipStatus = RelationshipStatus.SINGLE;
		sexStatus = SexStatus.MAN;
	}

	
	
	// Retourne la liste d'interets
	public List<String> getInterestsList() {
		return interestsList;
	}
	
	
	
	// Getters et setters	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
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
	
	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getAvatar() {
		return avatar;
	}
	public void setAvatar(Integer avatar) {
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

	public SexStatus getSexStatus() {
		return sexStatus;
	}

	public void setSexStatus(SexStatus sexStatus) {
		this.sexStatus = sexStatus;
	}
	
	public String getSexString() {
		return sexStatus.toString();
	}
	
	
}
