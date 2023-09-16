package it.sal.disco.unimib.progettodispositivimobili.ui.ricerca;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;



public class RicercaViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RicercaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is ricerca fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}