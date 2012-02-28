package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.insa.helloeverybody.R;


public class Profile implements Comparable<Profile> {
	
	// Attributs
	private boolean user;
	private Long id;
	private Integer avatar;
	private Integer age;
	private String firstName;
	private String lastName;
	private String jid;
	private RelationshipStatus relationshipStatus;
	private SexStatus sexStatus;
	private List<String> interestsList;
	private boolean isFavorite;
	private boolean isRecommended;
	private boolean isKnown;
	


	// Constructeurs
	public Profile() {
		setDefault();
	}
	
	public Profile(int avatar, String firstName, String lastName) {
		setDefault();
		this.avatar = avatar;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public Profile(Integer avatar, String firstName, String lastName,
			boolean isFavorite, boolean isRecommended, boolean isKnown) {
		setDefault();
		this.avatar = avatar;
		this.firstName = firstName;
		this.lastName = lastName;
		this.isFavorite = isFavorite;
		this.isRecommended = isRecommended;
		this.isKnown = isKnown;
	}
	
	public Profile(String firstName, String lastName, Integer age, 
			RelationshipStatus relationshipStatus, List<String> interestsList) {
		super();
		this.age = age;
		this.firstName = firstName;
		this.lastName = lastName;
		this.relationshipStatus = relationshipStatus;
		this.interestsList = interestsList;
	}

	private void setDefault() {
		id = new Random().nextLong();
		avatar = R.drawable.default_profile_icon;
		age = 18;
		interestsList = Collections.synchronizedList(new ArrayList<String>());
		relationshipStatus = RelationshipStatus.SINGLE;
		sexStatus = SexStatus.MAN;
		isFavorite = false;
		isKnown = false;
		isRecommended = false;
	}

	
	
	// Compare avec un profil
	public int compareTo(Profile comparedProfile) {
		if (firstName != comparedProfile.firstName) {
			return firstName.compareTo(comparedProfile.firstName);
		}
		else if (lastName != comparedProfile.lastName) {
			return lastName.compareTo(comparedProfile.lastName);
		}
		return 0;
	}

	
	
	// Retourne la liste d'interets
	public List<String> getInterestsList() {
		return interestsList;
	}
	
	// Ajour d'un centre d'interet
	public void addInterest(String string) {
		this.interestsList.add(string);
	}
	
	// Suppression d'un centre d'interet
	public void removeInterest(String interest) {
		this.interestsList.remove(this.interestsList.indexOf(interest));
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
	
	public String getJid() {
		return jid;
	}
	
	public void setJid(String jid) {
		this.jid = jid;
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

	
	public ProfileType getProfileType() {
		if (isFavorite) {
			return ProfileType.FAVORITE;
		}
		else if (isKnown) {
			return ProfileType.KNOWN;
		}
		else if (isRecommended) {
			return ProfileType.RECOMMENDED;
		}
		return ProfileType.NEAR_ME;
	}
	
	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public boolean isRecommended() {
		return isRecommended;
	}

	public void setRecommended(boolean isRecommended) {
		this.isRecommended = isRecommended;
	}

	public boolean isKnown() {
		return isKnown;
	}

	public void setKnown(boolean isKnown) {
		this.isKnown = isKnown;
	}
}
