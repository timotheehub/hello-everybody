package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Bitmap;
import android.util.Log;
import fr.insa.helloeverybody.R;


public class Profile implements Comparable<Profile> {
	
	public static final int DEFAULT_AVATAR = R.drawable.default_profile_icon; 
			
	// Attributs
	private Long id;
	private Bitmap avatar;
	private Integer age;
	private Integer distance;
	private String firstName;
	private String lastName;
	private String jid;
	private String password;
	private RelationshipStatus relationshipStatus;
	private SexStatus sexStatus;
	private List<String> interestsList;
	private List<String> friendsJidList;
	private boolean isFavorite;
	private boolean isRecommended;
	private boolean isKnown;
	private boolean isUpdated;
	


	// Constructeurs
	public Profile() {
		setDefault();
	}
	
	public Profile(String jid) {
		setDefault();
		this.jid = jid;
	}
	
	public Profile(String firstName, String lastName) {
		setDefault();
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public Profile(Bitmap avatar, String firstName, String lastName,
			boolean isFavorite, boolean isKnown, boolean isRecommended) {
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
		
	public Profile(String jid, String firstName, String lastName, Integer age, String sex, 
			String relationshipStatus, String interests, String friendsJids, Bitmap avatar, boolean isUpdated) {
		setDefault();
		this.jid = jid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.relationshipStatus = RelationshipStatus.fromString(relationshipStatus);
		this.sexStatus = SexStatus.fromString(sex);
		this.setInterestsListFromJson(interests);
		this.setFriendsJidListFromJson(friendsJids);
		this.avatar = avatar;
		this.isUpdated = isUpdated;
	}

	public Profile(Bitmap avatar, String firstName, String lastName) {
		setDefault();
		this.avatar = avatar;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	private void setDefault() {
		firstName = "";
		lastName = "";
		id = new Random().nextLong();
		jid = firstName +'/'+ id;
		avatar = null;
		age = 18;
		distance = 1;
		interestsList = Collections.synchronizedList(new ArrayList<String>());
		friendsJidList = Collections.synchronizedList(new ArrayList<String>());
		relationshipStatus = RelationshipStatus.SINGLE;
		sexStatus = SexStatus.MAN;
		isFavorite = false;
		isKnown = false;
		isRecommended = false;
		isUpdated = false;
	}
	
	
	
	// Mise a jour
	public void update(Profile profile) {
		jid = profile.jid;
		avatar = profile.avatar;
		age = profile.age;
		distance = profile.distance;
		sexStatus = profile.sexStatus;
		relationshipStatus = profile.relationshipStatus;
		isUpdated = profile.isUpdated();
		interestsList.clear();
		interestsList.addAll(profile.interestsList);
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

	// Retourne les Jid des amis
	public List<String> getFriendsJidList() {
		return friendsJidList;
	}
	
	// Remplacer les Jid des amsi
	public List<String> setFriendsJidList(List<String> jidsList) {
		friendsJidList.clear();
		friendsJidList.addAll(jidsList);

		return friendsJidList;
	}
	
	// Ajour d'un centre d'interet
	public void addFriendJid(String friendJid) {
		this.friendsJidList.add(friendJid);
	}
	
	// Suppression d'un centre d'interet
	public void removeFriendJid(String friendJid) {
		this.friendsJidList.remove(friendJid);
	}
	
	// Retourne la liste d'interets
	public List<String> getInterestsList() {
		return interestsList;
	}
	
	// Ajour d'un centre d'interet
	public void addInterest(String interest) {
		this.interestsList.add(interest);
	}
	
	// Suppression d'un centre d'interet
	public void removeInterest(String interest) {
		this.interestsList.remove(interest);
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

	public Bitmap getAvatar() {
		return avatar;
	}
	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}
	
	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	
	public boolean isUser() {
		return (UserProfile.getInstance().getProfile().jid==jid);
	}
	
	public String getJid() {
		return (jid!=null) ? jid : "";
	}
	
	public void setJid(String jid) {
		this.jid = jid;
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public void setInterestsList(List<String> interestsList) {
		this.interestsList = interestsList;
	}

	public String getInterestsListToJson() {
		JSONArray jarray = new JSONArray(interestsList);
		return jarray.toString();
	}
	
	public void setInterestsListFromJson(String json) {
		JSONArray jarray;
		interestsList.clear();
		if (json != null) {
			try {
				jarray = new JSONArray(json);
				
				for (int i = 0; i < jarray.length(); i++) {
					addInterest(jarray.getString(i));
				}
			} catch (Exception e) {
				Log.e("PROFILE", e.getMessage(), e);
			}
		}
	}
	
	public String getFriendsJidListToJson() {
		JSONArray jarray = new JSONArray(friendsJidList);
		return jarray.toString();
	}
	
	public void setFriendsJidListFromJson(String json) {
		JSONArray jarray;
		friendsJidList.clear();
		if (json != null) {
			try {
				jarray = new JSONArray(json);
				
				for (int i = 0; i < jarray.length(); i++) {
					addFriendJid(jarray.getString(i));
				}
			} catch (Exception e) {
				Log.e("PROFILE", e.getMessage(), e);
			}
		}
	}

	public void setContact(Contact contact) {
		this.isFavorite = contact.getFavorite();
		this.isKnown = contact.getKnown();
		this.isRecommended = contact.getRecommend();
	}
	
	public Contact getContact() {
		Contact contact = new Contact();
		contact.setJid(this.getJid());
		contact.setFavorite(this.isFavorite);
		contact.setKnown(this.isKnown);
		contact.setRecommend(this.isRecommended);
		return contact;
	}
}
