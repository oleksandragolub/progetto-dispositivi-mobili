package it.sal.disco.unimib.progettodispositivimobili.ui.start_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

import it.sal.disco.unimib.progettodispositivimobili.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    Button btnReset;
    TextView btnBack;
    EditText editEmail;
    FirebaseAuth mAuth;
    String strEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.txt_back);
        btnReset = findViewById(R.id.btn_ripristina_password);
        editEmail = findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(v -> {
            strEmail = editEmail.getText().toString().trim();

            if (TextUtils.isEmpty(strEmail)){
                Toast.makeText(ForgotPasswordActivity.this, "Inserisci la tua email", Toast.LENGTH_SHORT).show();
                editEmail.setError("Email richiesta");
                editEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
                Toast.makeText(ForgotPasswordActivity.this, "Re-inserisci la tua email", Toast.LENGTH_SHORT).show();
                editEmail.setError("Email valida richiesta");
                editEmail.requestFocus();
            } else {
                resetPassword();
            }

        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void resetPassword(){
        mAuth.sendPasswordResetEmail(strEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Controlla la tua email, ti abbiamo inviato il link per il reset della password.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            handleResetPasswordError(task.getException());
                        }
                    }
                });
    }

    private void handleResetPasswordError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            editEmail.setError("L'account non esiste oppure non è più valido.");
            editEmail.requestFocus();
        } else {
            Log.e(TAG, Objects.requireNonNull(exception.getMessage()));
            Toast.makeText(ForgotPasswordActivity.this, "Errore di reset password: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}