package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    /*private final MutableLiveData<String> mText = new MutableLiveData<>();
    public MutableLiveData<String> mUsername = new MutableLiveData<>();
    public MutableLiveData<String> mPhone = new MutableLiveData<>();
    public MutableLiveData<Uri> mProfileImageUri = new MutableLiveData<>();
    public MutableLiveData<Boolean> inProgress = new MutableLiveData<>();

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getUsername() {
        return mUsername;
    }

    public LiveData<String> getPhone() {
        return mPhone;
    }

    public MutableLiveData<Uri> getProfileImageUri() {
        return mProfileImageUri;
    }

    public MutableLiveData<Boolean> getInProgress() {
        return inProgress;
    }*/

    private final MutableLiveData<String> mText;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }


    /*public ProfileViewModel() {
        // Inizializza con dati di esempio o recuperali da una fonte
        mText.setValue("This is profile fragment");
        mUsername.setValue("Esempio Username");
        mPhone.setValue("1234567890");
        inProgress.setValue(false);
    }

    public void updateProfile(String newUsername, Uri selectedImageUri) {
        // Logica per aggiornare il profilo
        // Aggiorna il database Firebase, gestisci l'upload dell'immagine, ecc.
    }

    public void getUserData() {
        // Recupera i dati dell'utente da Firebase o da una fonte
        // Aggiorna username, phone, e profileImageUri di conseguenza
    }*/
}