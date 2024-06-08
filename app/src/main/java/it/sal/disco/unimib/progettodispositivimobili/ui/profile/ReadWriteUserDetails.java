package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.os.Parcel;
import android.os.Parcelable;


public class ReadWriteUserDetails {
    private String userId;
    public String username, dob, gender, email, authMethod, userType, profileImage;
    public Boolean emailVerificato;

    public ReadWriteUserDetails() {
        // Costruttore vuoto richiesto da Firebase
    }

    protected ReadWriteUserDetails(Parcel in) {
        // Leggi i dati da Parcel e inizializza gli altri membri
    }

    public ReadWriteUserDetails(String Username, String Email) {
        this.username = Username;
        this.email = Email;
    }

    public ReadWriteUserDetails(String Id, String Username, String Email) {
        this.userId = Id;
        this.username = Username;
        this.email = Email;
    }

    public ReadWriteUserDetails(String userId, String username, String email, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImage = profileImage;
    }

    public ReadWriteUserDetails(String email, String Username, String Dob, String Gender, Boolean emailVerificato) {
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
        this.emailVerificato = emailVerificato;
    }

    public ReadWriteUserDetails(String userId, String email, String Username, String Dob, String Gender, Boolean emailVerificato, String authMethod, String userType, String profileImage) {
        this.userId = userId;
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
        this.emailVerificato = emailVerificato;
        this.authMethod = authMethod;
        this.userType = userType;
        this.profileImage = profileImage;
    }

    public ReadWriteUserDetails(String email, String Username, String Dob, String Gender, Boolean emailVerificato, String authMethod, String userType) {
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
        this.emailVerificato = emailVerificato;
        this.authMethod = authMethod;
        this.userType = userType;
    }

   public ReadWriteUserDetails(String userId, String username, String dob, String gender, String email, Boolean emailVerificato, String authMethod) {
        this.userId = userId;
        this.username = username;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.emailVerificato = emailVerificato;
        this.authMethod = authMethod;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Boolean getEmailVerificato() {
        return emailVerificato;
    }

    public void setEmailVerificato(Boolean emailVerificato) {
        this.emailVerificato = emailVerificato;
    }
}
