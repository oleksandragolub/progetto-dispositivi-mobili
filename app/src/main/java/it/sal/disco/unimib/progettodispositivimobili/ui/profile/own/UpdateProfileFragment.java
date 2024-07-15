package it.sal.disco.unimib.progettodispositivimobili.ui.profile.own;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ui.profile.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdateProfileBinding;

public class UpdateProfileFragment extends Fragment {
    private FragmentUpdateProfileBinding binding;
    private TextInputEditText usernameEditText, dobEditText, descrizioneEditText;
    private String email, authMethod, username, dob, gender, descrizione, uid, userType, profileImage;
    private Boolean emailVerificato = true;
    private RadioButton radioButtonRegisterGenderSelected;
    private Button updateProfileButton, changeEmailButton, changePasswordButton;
    private RadioGroup radioGroupRegisterGender;
    private TextView btnBack;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnBack = binding.txtBack;
        usernameEditText = binding.textViewUsername;
        dobEditText = binding.textViewDoB;
        radioGroupRegisterGender = binding.radioGroupRegisterGender;
        updateProfileButton = binding.profileUpdateBtn;
        changeEmailButton = binding.changeEmailBtn;
        changePasswordButton = binding.changePwBtn;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        showProfile(Objects.requireNonNull(currentUser));

        dobEditText.setOnClickListener(v -> {
            int day, month, year;

            if (dob != null && !dob.isEmpty()) {
                String[] textASDoB = dob.split("/");

                day = Integer.parseInt(textASDoB[0]);
                month = Integer.parseInt(textASDoB[1]) - 1;
                year = Integer.parseInt(textASDoB[2]);
            } else {
                Calendar calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
            }

            // Crea e mostra il DatePickerDialog con la data corrente o quella di default
            DatePickerDialog picker = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
                // Formatta la data selezionata e aggiorna il testo di dobEditText
                String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                dobEditText.setText(selectedDate);
            }, year, month, day);

            picker.show();
        });


        updateProfileButton.setOnClickListener(v -> updateProfile(currentUser));

        changeEmailButton.setOnClickListener(v -> {
            if(isFormComplete()) {
                // Solo se il form è completo, permetti di andare al UpdateEmailFragment
                if(getActivity() != null) {
                    openFragment(new UpdateEmailFragment());
                }
            } else {
                // Mostra un messaggio di errore se il form non è completo
                Toast.makeText(getActivity(), "Completa tutti i campi obbligatori", Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordButton.setOnClickListener(v -> {
            if(isFormComplete()) {
                // Solo se il form è completo, permetti di andare al UpdatePasswordFragment
                if(getActivity() != null) {
                    openFragment(new UpdatePasswordFragment());
                }
            } else {
                // Mostra un messaggio di errore se il form non è completo
                Toast.makeText(getActivity(), "Completa tutti i campi obbligatori", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            if(isFormComplete()) {
                // Solo se il form è completo, permetti di tornare al ProfileFragment
                if(getActivity() != null) {
                    openFragment(new ProfileFragment());
                }
            } else {
                // Mostra un messaggio di errore se il form non è completo
                Toast.makeText(getActivity(), "Completa tutti i campi obbligatori", Toast.LENGTH_SHORT).show();
            }
        });

        //per il textwatcher sotto
        usernameEditText.addTextChangedListener(textWatcher);
        dobEditText.addTextChangedListener(textWatcher);

        /*Serve solo per il caso particolare del textwatcher in cui il radiobutton è l'ultimo elemento
            ad essere cliccato/riempito. Con questo metodo il textwatcher funziona indipendentemente
            dall'ordine in cui i campi vengono compilati
         */
        radioGroupRegisterGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateProfileButton.setEnabled(fieldsAreValid());
            }
        });

        return root;
    }

    //metodo che ritorna true se i campi sono tutti inseriti
    private boolean fieldsAreValid(){
        String textUsernameWatcher = String.valueOf(usernameEditText.getText());
        String textDoBWatcher = String.valueOf(dobEditText.getText());

        int intGenderIndexWatcher = radioGroupRegisterGender.getCheckedRadioButtonId();

        return !textUsernameWatcher.isEmpty() && !textDoBWatcher.isEmpty() &&
                intGenderIndexWatcher != -1;
    }

    //Abilita/disabilita il bottone di update profile se i campi sono inseriti/vuoti
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            updateProfileButton.setEnabled(fieldsAreValid());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // Metodo per controllare se il form è completo
    private boolean isFormComplete() {
        // Controlla se genere e data di nascita non sono vuoti
        int selectedGenderID = radioGroupRegisterGender.getCheckedRadioButtonId();
        String dob = dobEditText.getText().toString();

        return selectedGenderID != -1 && !TextUtils.isEmpty(dob);
    }

    private void updateProfile(FirebaseUser currentUser) {
        uid = currentUser.getUid();
        int selectedGenderID = binding.radioGroupRegisterGender.getCheckedRadioButtonId();

        if (selectedGenderID != -1) {
            radioButtonRegisterGenderSelected = binding.getRoot().findViewById(selectedGenderID);
            gender = radioButtonRegisterGenderSelected.getText().toString();
        } else {
            // Gestione del caso in cui nessun RadioButton sia selezionato
            Toast.makeText(getActivity(), "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
            return;
        }

        username = usernameEditText.getText().toString();
        dob = dobEditText.getText().toString();
        //email = currentUser.getEmail().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(getActivity(), "Inserisci il tuo username", Toast.LENGTH_SHORT).show();
            usernameEditText.setError("Username richiesto");
            usernameEditText.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(dob)){
            Toast.makeText(getActivity(), "Inserisci la tua data di nascita", Toast.LENGTH_SHORT).show();
            dobEditText.setError("Data di nascita richiesta");
            dobEditText.requestFocus();
            //return;
        } else if(TextUtils.isEmpty(radioButtonRegisterGenderSelected.getText())){
            Toast.makeText(getActivity(), "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
            radioButtonRegisterGenderSelected.setError("Genere richiesto");
            radioButtonRegisterGenderSelected.requestFocus();
            //return;
        } else {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Continua con il salvataggio dei dati nel database
                    gender = radioButtonRegisterGenderSelected.getText().toString();
                    username = Objects.requireNonNull(usernameEditText.getText()).toString();
                    dob = Objects.requireNonNull(dobEditText.getText()).toString();

                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(uid, email, username, dob, gender, emailVerificato, authMethod, userType, profileImage);
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");

                    referenceProfile.child(uid).setValue(writeUserDetails).addOnCompleteListener(databaseTask -> {
                        if (databaseTask.isSuccessful()) {
                            Toast.makeText(getActivity(), "Aggiornamento è completato con successo!", Toast.LENGTH_SHORT).show();
                            if (getActivity() != null) {
                                openFragment(new ProfileFragment());
                            }
                        } else {
                            try {
                                throw Objects.requireNonNull(databaseTask.getException());
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Errore nell'aggiornamento del profilo", Toast.LENGTH_SHORT).show();
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

    private void showProfile(FirebaseUser currentUser) {
        String userIDofRegistered = currentUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");
        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    email = readUserDetails.getEmail();
                    username = readUserDetails.getUsername();
                    dob = readUserDetails.getDob();
                    gender = readUserDetails.getGender();
                    authMethod = readUserDetails.getAuthMethod();
                    userType = readUserDetails.getUserType();
                    profileImage = readUserDetails.getProfileImage();

                    usernameEditText.setText(username);
                    dobEditText.setText(dob);

                    if(gender.equals("Maschio")){
                        radioButtonRegisterGenderSelected = binding.genderMale;
                    } else{
                        radioButtonRegisterGenderSelected = binding.genderFem;
                    }
                    radioButtonRegisterGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(getActivity(), "Qualcosa è andato storto!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Qualcosa è andato storto!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}