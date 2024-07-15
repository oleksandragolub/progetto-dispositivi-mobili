package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.ui.main.MainActivity;
import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdateEmailBinding;

public class UpdateEmailFragment extends Fragment {

    FragmentUpdateEmailBinding binding;

    private TextView textViewAuthenticated, textViewOldEmail;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail, buttonAutentica;
    private EditText editTextNewEmail, editTextPwd, editTextOldEmail;

    TextView btnBack;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

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
        buttonAutentica = binding.btnAuthenticate;

        //serve solo pr il textwatcher
        editTextOldEmail = binding.emailCorrente;

        buttonUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userOldEmail = Objects.requireNonNull(currentUser).getEmail();
        TextView textViewOldEmail = binding.emailCorrente;
        textViewOldEmail.setText(userOldEmail);

        if(currentUser.equals("")){
            Toast.makeText(getActivity(), "Qualcosa è andato storto! I dati dell'utente non sono disponibili.", Toast.LENGTH_SHORT).show();
            if(getActivity() != null){
                openFragment(new ProfileFragment());
                getActivity().finish();
            }
        } else {
            reAuthenticate(currentUser);
        }

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UpdateProfileFragment());
            }
        });

        editTextOldEmail.addTextChangedListener(textWatcher);
        editTextPwd.addTextChangedListener(textWatcher);

        return root;
    }

    //Abilita/disabilita il bottone autentica se email e password sono inserite/vuote
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String textEmailWatcher = String.valueOf(editTextOldEmail.getText());
            String textPasswordWatcher = String.valueOf(editTextPwd.getText());

            buttonAutentica.setEnabled(!textEmailWatcher.isEmpty() && !textPasswordWatcher.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @SuppressLint("SetTextI18n")
    private void reAuthenticate(FirebaseUser currentUser) {


        Button buttonVerifyUser = binding.btnAuthenticate;
        buttonVerifyUser.setOnClickListener(v -> {
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

                            buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.md_theme_error));

                            buttonUpdateEmail.setOnClickListener(v1 -> {
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
                            });
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (Exception e){
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void updateEmail(FirebaseUser currentUser) {
        Boolean emailVerificato = true;
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        databaseRef.child(currentUser.getUid()).child("email").setValue(userNewEmail);

        currentUser.verifyBeforeUpdateEmail(userNewEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.sendEmailVerification();
                        databaseRef.child(currentUser.getUid()).child("emailVerificato").setValue(false);
                        Toast.makeText(getActivity(), "L'email è stata aggiornata. Adesso verifica la tua nuova email per il messaggio!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        mAuth.signOut();
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e){
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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