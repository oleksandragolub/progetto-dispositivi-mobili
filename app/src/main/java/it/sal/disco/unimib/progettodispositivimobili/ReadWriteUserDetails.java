package it.sal.disco.unimib.progettodispositivimobili;

import android.os.Parcel;
import android.os.Parcelable;


public class ReadWriteUserDetails implements Parcelable {
    private String userId;
    public String username, dob, gender, email, authMethod;
    public Boolean emailVerificato;
    public String dataImage;

    public ReadWriteUserDetails() {
        // Costruttore vuoto richiesto da Firebase
    }

    protected ReadWriteUserDetails(Parcel in) {
        // Leggi i dati da Parcel e inizializza gli altri membri
    }

    public ReadWriteUserDetails(String Username, String Email, String dataImage) {
        this.username = Username;
        this.email = Email;
        this.dataImage = dataImage;
    }

    public ReadWriteUserDetails(String Id, String Username, String Email, String dataImage) {
        this.userId = Id;
        this.username = Username;
        this.email = Email;
        this.dataImage = dataImage;
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

    public ReadWriteUserDetails(String userId, String username, String dob, String gender, String email, Boolean emailVerificato, String authMethod) {
        this.userId = userId;
        this.username = username;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.emailVerificato = emailVerificato;
        this.authMethod = authMethod;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Scrivi i membri nella Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReadWriteUserDetails> CREATOR = new Creator<ReadWriteUserDetails>() {
        @Override
        public ReadWriteUserDetails createFromParcel(Parcel in) {
            return new ReadWriteUserDetails(in);
        }

        @Override
        public ReadWriteUserDetails[] newArray(int size) {
            return new ReadWriteUserDetails[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDataImage() {
        if (dataImage != null) {
            return dataImage;
        } else {
            return null;
        }/*else {
            // Recupera l'URL dell'immagine utilizzando l'UID dell'utente corrente
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("VisualizzaImmagini")
                        .child(uid);

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    dataImage = uri.toString();
                    // Esegui qui qualsiasi altra azione che desideri con l'URL dell'immagine
                }).addOnFailureListener(e -> {
                    // Gestisci eventuali errori nel recupero dell'URL dell'immagine
                });
            }
            return dataImage; // Restituisce null o l'URL dell'immagine se disponibile
        }*/
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
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

    public void setEmailVerificato(Boolean emailVerificato) {
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
