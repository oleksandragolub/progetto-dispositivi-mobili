package it.sal.disco.unimib.progettodispositivimobili.ui.start_app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
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
import it.sal.disco.unimib.progettodispositivimobili.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText username, email, dob, password, confermaPassword;
    private RadioGroup radioGroupRegisterGender;
    private Button btnRegister;
    private MaterialToolbar topAppbar;

    private String name, emailInput, dobInput, passwordInput, confermaPasswordInput, gender;
    private Boolean emailVerificato = false;
    private FirebaseAuth firebaseAuth;
    private DatePickerDialog picker;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize FirebaseAuth instance first
        firebaseAuth = FirebaseAuth.getInstance();

        // Now check if the user is already authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }

        // Inizializzazione dei widget
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        dob = findViewById(R.id.DoB);
        password = findViewById(R.id.password);
        confermaPassword = findViewById(R.id.conferma_password);
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        btnRegister = findViewById(R.id.btn_register);
        topAppbar = findViewById(R.id.top_appbar);

        setupDatePicker();

        btnRegister.setOnClickListener(v -> validateData());

        topAppbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        //per il textwatcher sotto
        username.addTextChangedListener(textWatcher);
        email.addTextChangedListener(textWatcher);
        dob.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);
        confermaPassword.addTextChangedListener(textWatcher);

        radioGroupRegisterGender.setOnCheckedChangeListener((group, checkedId) -> btnRegister.setEnabled(fieldsAreValid()));
    }

    //metodo che ritorna true se i campi sono tutti inseriti
    private boolean fieldsAreValid(){
        String textUsernameWatcher = String.valueOf(username.getText());
        String textEmailWatcher = String.valueOf(email.getText());
        String textDoBWatcher = String.valueOf(dob.getText());
        String textPasswordWatcher = String.valueOf(password.getText());
        String textConfirmPasswordWatcher = String.valueOf(confermaPassword.getText());

        int intGenderIndexWatcher = radioGroupRegisterGender.getCheckedRadioButtonId();

        return !textUsernameWatcher.isEmpty() && !textEmailWatcher.isEmpty() && !textDoBWatcher.isEmpty() &&
                !textPasswordWatcher.isEmpty() && !textConfirmPasswordWatcher.isEmpty() && intGenderIndexWatcher != -1;
    }

    //Abilita/disabilita il bottone di registrazione se i campi sono inseriti/vuoti
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            btnRegister.setEnabled(fieldsAreValid());
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void setupDatePicker() {
        dob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            picker = new DatePickerDialog(RegisterActivity.this, (view, year1, monthOfYear, dayOfMonth) -> {
                // Set the day of birth EditText to the chosen date
                String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                dob.setText(selectedDate);
            }, year, month, day);
            picker.show();
        });
    }

    private void validateData() {
        name = username.getText().toString().trim();
        emailInput = email.getText().toString().trim();
        dobInput = dob.getText().toString().trim();
        passwordInput = password.getText().toString().trim();
        confermaPasswordInput = confermaPassword.getText().toString().trim();

        int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            gender = selectedGenderButton.getText().toString();
        } else {
            gender = null; // Handle appropriately if no gender is selected
            Toast.makeText(this, "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Inserisci il tuo username", Toast.LENGTH_SHORT).show();
            username.setError("Username richiesto");
            username.requestFocus();
        } else if(TextUtils.isEmpty(emailInput)){
            Toast.makeText(this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
            email.setError("Email richiesta");
            email.requestFocus();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            Toast.makeText(this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
            email.setError("Email valida richiesta");
            email.requestFocus();
        } else if(TextUtils.isEmpty(dobInput)){
            Toast.makeText(this, "Inserisci la tua data di nascita", Toast.LENGTH_SHORT).show();
            dob.setError("Data di nascita richiesta");
            dob.requestFocus();
        } else if(gender == null){
            Toast.makeText(this, "Seleziona il tuo genere", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(passwordInput)){
            Toast.makeText(this, "Inserisci la tua password", Toast.LENGTH_SHORT).show();
            password.setError("Password richiesta");
            password.requestFocus();
        } else if(passwordInput.length() < 6){
            Toast.makeText(this, "La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show();
            password.setError("Password troppo corta");
            password.requestFocus();
        } else if(TextUtils.isEmpty(confermaPasswordInput)){
            Toast.makeText(this, "Conferma la tua password", Toast.LENGTH_SHORT).show();
            confermaPassword.setError("Password di conferma richiesta");
            confermaPassword.requestFocus();
        } else if(!passwordInput.equals(confermaPasswordInput)){
            Toast.makeText(this, "Le password non corrispondono", Toast.LENGTH_SHORT).show();
            confermaPassword.setError("Le password non corrispondono");
            password.clearComposingText();
            confermaPassword.clearComposingText();
        } else{
            createUserAccount();
        }
    }

    private void createUserAccount() {
        firebaseAuth.createUserWithEmailAndPassword(emailInput, passwordInput).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    sendVerificationEmail(firebaseUser);
                    updateUserInfo(firebaseUser.getUid());
                    // Redirect to login screen with a message
                    Toast.makeText(RegisterActivity.this, "Registrazione effettuata con successo. Per favore verifica la tua email.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
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
            hashMap.put("email", emailInput);
            hashMap.put("username", name);
            hashMap.put("gender", gender);
            hashMap.put("dob", dobInput);
            hashMap.put("authMethod", "PasswordEmail");
            hashMap.put("userType", "user");
            hashMap.put("emailVerificato", emailVerificato); // Initially false, will be true after verification
            hashMap.put("profileImage", "");

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
            password.setError("La tua password è troppo debole.");
            password.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            email.setError("L'email inserita non esiste oppure non è valida.");
            email.requestFocus();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            email.setError("Questa email è già in uso.");
            email.requestFocus();
        } else {
            Log.e(TAG, exception.getMessage());
            Toast.makeText(RegisterActivity.this, "Errore di registrazione: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
