package it.sal.disco.unimib.progettodispositivimobili.ui.start_app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

import it.sal.disco.unimib.progettodispositivimobili.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final String TAG = "RegisterActivity";

    private String name, email, dob, password, conferma_password, gender;
    private Boolean emailVerificato = false;
    private FirebaseAuth firebaseAuth;
    private TextInputEditText editTextEmail, editTextPassword, editTextDoB, editTextConfirmPassword, editTextUsername;
    private DatePickerDialog picker;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth instance first
        firebaseAuth = FirebaseAuth.getInstance();

        // Now check if the user is already authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }

        setupDatePicker();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.loginNow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupDatePicker() {
        binding.DoB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            picker = new DatePickerDialog(RegisterActivity.this, (view, year1, monthOfYear, dayOfMonth) -> {
                // Set the day of birth EditText to the chosen date
                String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                binding.DoB.setText(selectedDate);
            }, year, month, day);
            picker.show();
        });
    }


    private void validateData() {
        name = binding.username.getText().toString().trim();
        email = binding.email.getText().toString().trim();
        dob = binding.DoB.getText().toString().trim();
        password = binding.password.getText().toString().trim();
        conferma_password = binding.confermaPassword.getText().toString().trim();

        int selectedGenderId = binding.radioGroupRegisterGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            gender = selectedGenderButton.getText().toString();
        } else {
            gender = null; // Handle appropriately if no gender is selected
            Toast.makeText(this, "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Inserisci il tuo username", Toast.LENGTH_SHORT).show();
            binding.username.setError("Username richiesto");
            binding.username.requestFocus();
        } else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
            binding.email.setError("Email richiesta");
            binding.email.requestFocus();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
            binding.email.setError("Email valida richiesta");
            binding.email.requestFocus();
        } else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Inserisci la tua data di nascita", Toast.LENGTH_SHORT).show();
            binding.DoB.setError("Data di nascita richiesta");
            binding.DoB.requestFocus();
        } else if(gender == null){
            Toast.makeText(this, "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Inserisci la tua password", Toast.LENGTH_SHORT).show();
            binding.password.setError("Password richiesta");
            binding.password.requestFocus();
        } else if(password.length() < 6){
            Toast.makeText(this, "La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show();
            binding.password.setError("Password troppo corta");
            binding.password.requestFocus();
        } else if(TextUtils.isEmpty(conferma_password)){
            Toast.makeText(this, "Conferma la tua password", Toast.LENGTH_SHORT).show();
            binding.confermaPassword.setError("Password di conferma richiesta");
            binding.confermaPassword.requestFocus();
        } else if(!password.equals(conferma_password)){
            Toast.makeText(this, "Le password non corrispondono", Toast.LENGTH_SHORT).show();
            binding.confermaPassword.setError("Le password non corrispondono");
            binding.password.clearComposingText();
            binding.confermaPassword.clearComposingText();
        } else{
            createUserAccount();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void createUserAccount() {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    sendVerificationEmail(firebaseUser);
                    updateUserInfo(firebaseUser.getUid());
                    // Redirect to login screen with a message
                    Toast.makeText(RegisterActivity.this, "Registrazione effettuata con successo. Per favore verifica la tua email.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registrazione fallita. Riprova.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            handleRegistrationError(e);
            Toast.makeText(RegisterActivity.this, "Registrazione fallita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Email di verifica inviata.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Il tentativo di invio della mail di verifica fallito.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(String uid) {
        if (uid != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", uid);
            hashMap.put("email", email);
            hashMap.put("username", name);
            hashMap.put("gender", gender);
            hashMap.put("dob", dob);
            hashMap.put("authMethod", "PasswordEmail");
            hashMap.put("userType", "user");
            hashMap.put("emailVerificato", emailVerificato); // Initially false, will be true after verification

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Utenti registrati");
            ref.child(uid).setValue(hashMap).addOnSuccessListener(unused -> {
                Toast.makeText(RegisterActivity.this, "Registrazione effettuata con successo. Verifica la tua email.", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(RegisterActivity.this, "Error: User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            editTextPassword.setError("La tua password è troppo debole.");
            editTextPassword.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            editTextEmail.setError("L'email inserita non esiste oppure non è valida.");
            editTextEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            editTextEmail.setError("Questa email è già in uso.");
            editTextEmail.requestFocus();
        } else {
            Log.e(TAG, exception.getMessage());
            Toast.makeText(RegisterActivity.this, "Errore di registrazione: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}









