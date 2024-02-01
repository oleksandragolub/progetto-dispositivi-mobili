package it.sal.disco.unimib.progettodispositivimobili.ui.profile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;
import it.sal.disco.unimib.progettodispositivimobili.ReadWriteUserDetails;
import it.sal.disco.unimib.progettodispositivimobili.databinding.FragmentUpdateProfileBinding;

public class UpdateProfileFragment extends Fragment {

    FragmentUpdateProfileBinding binding;

    private TextInputEditText usernameEditText, dobEditText, descrizioneEditText;
    private String email, username, dob, gender, descrizione;
    private RadioButton radioButtonRegisterGenderSelected;
    Button updateProfileButton, changeEmailButton, changePasswordButton;
    RadioGroup radioGroupRegisterGender;
    TextView btnBack;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

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
            String[] textASDoB = dob.split("/");

            int day = Integer.parseInt(textASDoB[0]);
            int month = Integer.parseInt(textASDoB[1]) - 1;
            int year = Integer.parseInt(textASDoB[2]);

            DatePickerDialog picker;

            picker = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) ->
                    dobEditText.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1), year, month, day);
            picker.show();
        });


        updateProfileButton.setOnClickListener(v -> updateProfile(currentUser));

        changeEmailButton.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UpdateEmailFragment());
            }
        });

        changePasswordButton.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new UpdatePasswordFragment());
            }
        });

        btnBack.setOnClickListener(v -> {
            if(getActivity() != null) {
                openFragment(new ProfileFragment());
            }
        });

        return root;
    }

    private void updateProfile(FirebaseUser currentUser) {
        int selectedGenderID = binding.radioGroupRegisterGender.getCheckedRadioButtonId();

        if (selectedGenderID != -1) {
            radioButtonRegisterGenderSelected = binding.getRoot().findViewById(selectedGenderID);
            gender = radioButtonRegisterGenderSelected.getText().toString();
        } else {
            // Gestione del caso in cui nessun RadioButton sia selezionato
            Toast.makeText(getActivity(), "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
            return;
        }

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
        } else{
            gender = radioButtonRegisterGenderSelected.getText().toString();
            username = Objects.requireNonNull(usernameEditText.getText()).toString();
            dob = Objects.requireNonNull(dobEditText.getText()).toString();

            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(currentUser.getEmail(), username, dob, gender);
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Utenti registrati");

            String userID = currentUser.getUid();

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                            setDisplayName(username).build();
                    currentUser.updateProfile(profileUpdates);
                    Toast.makeText(getActivity(), "Aggiornamento è completato con il successo!", Toast.LENGTH_SHORT).show();
                    if(getActivity() != null) {
                        openFragment(new ProfileFragment());
                    }
                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (Exception e) {
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