package it.sal.disco.unimib.progettodispositivimobili.ui.preferiti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import it.sal.disco.unimib.progettodispositivimobili.LoginActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentPreferitiBinding;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentProfileBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.UpdateProfileFragment;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.UploadProfilePicFragment;

public class PreferitiFragment extends Fragment {

    private FragmentPreferitiBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPreferitiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}