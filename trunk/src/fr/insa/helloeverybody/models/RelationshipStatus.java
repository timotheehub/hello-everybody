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
}
