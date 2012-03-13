package fr.insa.helloeverybody.models;

//EnumÃ©ration des types de situations
public enum RelationshipStatus {
    SINGLE("Célibataire"),
    COUPLE("En couple"),
    SECRET("Non divulguée");

    private final String text;
   
    private RelationshipStatus(String text) {
        this.text = text;
    }
   
    @Override
    public String toString() {
        return text;
    }
    
	public static RelationshipStatus fromString(String text) {
		if (text != null) {
			for (RelationshipStatus b : RelationshipStatus.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return SINGLE;
	}
}
