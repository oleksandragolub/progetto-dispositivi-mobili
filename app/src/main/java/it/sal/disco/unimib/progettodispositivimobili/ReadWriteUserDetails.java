package it.sal.disco.unimib.progettodispositivimobili;

public class ReadWriteUserDetails {
    public String username, dob, gender, email, authMethod;
    public Boolean emailVerificato;

    public ReadWriteUserDetails() {}

    public ReadWriteUserDetails(String Username, String Dob, String Gender) {
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
    }

    public ReadWriteUserDetails(String email, String Username, String Dob, String Gender) {
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
    }

    public ReadWriteUserDetails(String email, String Username, String Dob, String Gender, Boolean emailVerificato) {
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
        this.emailVerificato = emailVerificato;
    }

    public ReadWriteUserDetails(String email, String Username, String Dob, String Gender, Boolean emailVerificato, String authMethod) {
        this.email = email;
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
        this.emailVerificato = emailVerificato;
        this.authMethod = authMethod;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public Boolean getEmailVerificato() {
        return emailVerificato;
    }

    public void setEmailVerificato(Boolean email) {
        this.emailVerificato = emailVerificato;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDob() { // Modifica "DoB" in "dob"
        return dob;
    }

    public void setDob(String dob) { // Modifica "DoB" in "dob"
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
