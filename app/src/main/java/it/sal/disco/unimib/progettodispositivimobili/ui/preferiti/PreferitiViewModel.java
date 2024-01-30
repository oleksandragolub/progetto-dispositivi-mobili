package it.sal.disco.unimib.progettodispositivimobili.ui.preferiti;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class PreferitiViewModel extends ViewModel {

    private final MutableLiveData<String> mText;


    public PreferitiViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Preferiti fragment");

    }

    public LiveData<String> getText() {
        return mText;
    }
}
