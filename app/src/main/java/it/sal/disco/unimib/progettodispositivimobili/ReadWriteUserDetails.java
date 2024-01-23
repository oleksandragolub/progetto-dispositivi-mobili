package it.sal.disco.unimib.progettodispositivimobili;

public class ReadWriteUserDetails {
    public String username, dob, gender;

    public ReadWriteUserDetails() {}

    public ReadWriteUserDetails(String Username, String Dob, String Gender) {
        this.username = Username;
        this.dob = Dob;
        this.gender = Gender;
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
