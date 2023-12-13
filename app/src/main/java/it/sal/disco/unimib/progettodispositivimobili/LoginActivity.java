package it.sal.disco.unimib.progettodispositivimobili;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView text_ForgotPassword, text_registerNow;


    @Override
    public void onStart() {
        super.onStart();
    }

    private void signIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        AppCompatButton btnGoogleSignIn = findViewById(R.id.btn_login_google);
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            firebaseAuthWithGoogle(signInAccount);
                        } catch (ApiException e) {
                            // Gestisci l'errore di autenticazione qui
                            handleSignInError(e);
                    }
                } else {
        // Gestisci la situazione in cui il risultato non è OK
        Toast.makeText(LoginActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
    }
   });

    private void handleSignInError(ApiException e) {
        // Ottieni il codice di errore e prepara un messaggio di errore appropriato
        int statusCode = e.getStatusCode();
        String errorMessage = "Failed to sign in: ";
        switch (statusCode) {
            case GoogleSignInStatusCodes.NETWORK_ERROR:
                errorMessage += "Network error";
                break;
            case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                errorMessage += "Sign in cancelled";
                break;
            case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                errorMessage += "Sign in failed";
                break;
            // Aggiungi altri casi se necessario
            default:
                errorMessage += "Unknown error";
                break;
        }

        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        Log.e("GoogleSignIn", "Sign in error: " + e.getStatusCode());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }else{
                Toast.makeText(LoginActivity.this,"Autentification failed!", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            signIn();
        }else{
            updateUI(currentUser);
        }

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        text_ForgotPassword = findViewById(R.id.text_forgot_pw);
        text_registerNow = findViewById(R.id.registerNow);
        buttonLogin = findViewById(R.id.btn_login);

        text_ForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });

        text_registerNow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            String email, password;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email:", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password:", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login successful.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}