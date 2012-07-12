package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;

import android.graphics.Bitmap;
import android.util.Log;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

public class Profile implements Comparable<Profile> {

	public static final String TAG = "Profile";
	public static final int DEFAULT_AVATAR = R.drawable.default_profile_icon; 
			
	// Attributs
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
	private boolean isDownloaded;
	
	// Constructeurs	
	public Profile(String jid) {
		setDefault();
		this.jid = jid;
	}
	
	public Profile(String jid, String firstName, String lastName) {
		setDefault();
		this.jid = jid;
		this.firstName = firstName;
		this.lastName = lastName;
	}
		
	public Profile(String jid, String firstName, String lastName, 
			Integer age, String sex, String relationshipStatus, 
			String interests, String friendsJids, Bitmap avatar, boolean isDownloaded) {
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
		this.isDownloaded = isDownloaded;
	}

	private void setDefault() {
		firstName = "";
		lastName = "";
		jid = firstName +'/'+ new Random().nextLong();
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
		isDownloaded = false;
	}
	
	// Mise a jour
	public void update(Profile profile) {
		jid = profile.jid;
		avatar = profile.avatar;
		age = profile.age;
		distance = profile.distance;
		sexStatus = profile.sexStatus;
		relationshipStatus = profile.relationshipStatus;
		isDownloaded = profile.isDownloaded();
		interestsList.clear();
		interestsList.addAll(profile.interestsList);
		friendsJidList.clear();
		friendsJidList.addAll(profile.friendsJidList);
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
	
	// Retourne vrai si c'est le profil de l'utilisateur du téléphone
	public boolean isLocalUser() {
		// Vérifier que le profil d'utilisateur local existe
		Profile localUserProfile = LocalUserProfile.getInstance().getProfile();
		if (localUserProfile == null) {
			return false;
		}
		
		// Retourner vrai si c'est le profil de l'utilisateur du téléphone
		return (localUserProfile.getJid() == jid);
	}

	// Retourne les Jid des amis
	public List<String> getFriendsJidList() {
		return friendsJidList;
	}
	
	// Remplace les Jid des amis
	public List<String> setFriendsJidList(List<String> jidsList) {
		friendsJidList.clear();
		friendsJidList.addAll(jidsList);

		return friendsJidList;
	}
	
	// Ajoute un centre d'interet
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
	
	// Ajoute un centre d'interet
	public void addInterest(String interest) {
		this.interestsList.add(interest);
	}
	
	// Suppression d'un centre d'interet
	public void removeInterest(String interest) {
		this.interestsList.remove(interest);
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
	
	public String getJid() {
		return jid;
	}
	
	public void setJid(String jid) {
		this.jid = jid;
	}
	
	public boolean isDownloaded() {
		return isDownloaded;
	}

	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
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
				Log.e(TAG, e.getMessage(), e);
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
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}
