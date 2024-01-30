package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.RegisterActivity;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdateEmailBinding;
import it.sal.disco.unimib.progettodispositivimobili.ui.home.HomeFragment;

public class UpdateEmailFragment extends Fragment {

    FragmentUpdateEmailBinding binding;

    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail, editTextPwd;

    private TextView btnBack;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUpdateEmailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnBack = binding.txtBack;
        editTextPwd = binding.password;
        editTextNewEmail = binding.emailNuovo;
        textViewAuthenticated = binding.textProfileNotAuthenticated;
        buttonUpdateEmail = binding.btnEmailNuovo;

        buttonUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userOldEmail = currentUser.getEmail();
        TextView textViewOldEmail = binding.emailCorrente;
        textViewOldEmail.setText(userOldEmail);

        if(currentUser.equals("")){
            Toast.makeText(getActivity(), "Qualcosa è andato storto! I dati dell'utente non sono disponibili.", Toast.LENGTH_SHORT).show();
            if(getActivity() != null){
                openFragment(new ProfileFragment());
            }
        } else {
            reAuthenticate(currentUser);
        }




        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    openFragment(new UpdateProfileFragment());
                }
            }
        });

        return root;
    }

    private void reAuthenticate(FirebaseUser currentUser) {
        Button buttonVerifyUser = binding.btnAuthenticate;
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(getActivity(), "Serve la password per continuare!", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Password richiesta");
                    editTextPwd.requestFocus();
                    //return;
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "L'email è stata verificata." + " Adesso puoi aggiornare la tua email!", Toast.LENGTH_SHORT).show();

                                textViewAuthenticated.setText("Sei stato autenticato. Adesso puoi aggiornare la tua email!");
                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.dark_red));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(getActivity(), "Inserisci la tua nuova email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Nuova email richiesta");
                                            editTextNewEmail.requestFocus();
                                            //return;
                                        } else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(getActivity(), "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Email valida richiesta");
                                            editTextNewEmail.requestFocus();
                                            //return;
                                        } else if(userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(getActivity(), "La nuova email non può essere la stessa di prima", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Nuova email richiesta");
                                            editTextNewEmail.requestFocus();
                                            //return;
                                        } else {
                                            updateEmail(currentUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser currentUser) {
        currentUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    currentUser.sendEmailVerification();
                    Toast.makeText(getActivity(), "L'email è stata aggiornata. Adesso verifica la tua nuova email per il messaggio!", Toast.LENGTH_SHORT).show();
                    if(getActivity() != null) {
                        openFragment(new HomeFragment());
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
