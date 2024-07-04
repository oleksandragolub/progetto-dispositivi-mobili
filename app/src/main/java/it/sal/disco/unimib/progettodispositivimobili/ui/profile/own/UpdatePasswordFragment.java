package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdatePasswordBinding;

public class UpdatePasswordFragment extends Fragment {

    FragmentUpdatePasswordBinding binding;

    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private String userPwdCurr;
    TextView btnBack;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnBack = binding.txtBack;

        editTextPwdCurr = binding.passwordCorrente;
        editTextPwdNew = binding.passwordNuova;
        editTextPwdConfirmNew = binding.confermaPasswordNuova;
        textViewAuthenticated = binding.textProfileNotAuthenticated;
        buttonReAuthenticate = binding.btnAuthenticate;
        buttonChangePwd = binding.btnPasswordNuova;

        editTextPwdNew.setEnabled(false);
        editTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser.equals("")) {
            Toast.makeText(getActivity(), "Qualcosa è andato storto! I dati dell'utente non sono disponibili.", Toast.LENGTH_SHORT).show();
            if(getActivity() != null){
                openFragment(new ProfileFragment());
            }
        } else {
            reAuthenticateUser(currentUser);
        }

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UpdateProfileFragment());
            }
        });

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void reAuthenticateUser(FirebaseUser currentUser) {
        buttonReAuthenticate.setOnClickListener(v -> {
            userPwdCurr = editTextPwdCurr.getText().toString();

            if(TextUtils.isEmpty(userPwdCurr)){
                Toast.makeText(getActivity(), "Serve la password per continuare!", Toast.LENGTH_SHORT).show();
                editTextPwdCurr.setError("Password corrente richiesta");
                editTextPwdCurr.requestFocus();
                //return;
            } else {
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), userPwdCurr);

                currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        editTextPwdCurr.setEnabled(true);
                        editTextPwdNew.setEnabled(true);
                        editTextPwdConfirmNew.setEnabled(true);

                        buttonReAuthenticate.setEnabled(false);
                        buttonChangePwd.setEnabled(true);

                        textViewAuthenticated.setText("Sei stato autenticato. Adesso puoi aggiornare la tua password!");

                        Toast.makeText(getActivity(), "La password è stata verificata." + " Adesso puoi aggiornare la tua password!", Toast.LENGTH_SHORT).show();

                        buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.md_theme_error));

                        buttonChangePwd.setOnClickListener(v1 -> changePwd(currentUser));
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e){
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void changePwd(FirebaseUser currentUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirmNew.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(getActivity(), "Inserisci la tua nuova password", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Nuova password richiesta");
            editTextPwdNew.requestFocus();
            //return;
        } else if (TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(getActivity(), "Conferma la tua nuova password", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Richiesta di conferma nuova password");
            editTextPwdConfirmNew.requestFocus();
            //return;
        } else if(!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(getActivity(), "Le password non coincidono", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Password identica richiesta");
            editTextPwdConfirmNew.requestFocus();
            //return;
        } else if(userPwdCurr.matches(userPwdNew)){
            Toast.makeText(getActivity(), "La nuova password non può essere la stessa di prima", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Nuova password richiesta");
            editTextPwdNew.requestFocus();
            //return;
        } else {
            currentUser.updatePassword(userPwdNew).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "La password è stata aggiornata con successo!", Toast.LENGTH_SHORT).show();
                    if(getActivity() != null) {
                        openFragment(new ProfileFragment());
                    }
                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (Exception e){
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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