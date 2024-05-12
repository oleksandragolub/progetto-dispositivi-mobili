package it.sal.disco.unimib.progettodispositivimobili.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mTextAdmin;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        mTextAdmin = new MutableLiveData<>();
        mTextAdmin.setValue("This is admin home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getText2() {
        return mTextAdmin;
    }
}