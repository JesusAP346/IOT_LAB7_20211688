package com.example.lab6_20211688;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Collections;

public class LoginActivity extends AppCompatActivity {

    private final static String TAG = "msg-test";
    Button btnCorreo, btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Ya es tu layout personalizado

        btnCorreo = findViewById(R.id.btnCorreo);
        btnGoogle = findViewById(R.id.btnGoogle);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.setLanguageCode("es-419");
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "Usuario logueado: " + currentUser.getUid());
            goToMainActivity();
        }

        btnCorreo.setOnClickListener(v -> signInWithProvider(AuthUI.IdpConfig.EmailBuilder.class));
        btnGoogle.setOnClickListener(v -> signInWithProvider(AuthUI.IdpConfig.GoogleBuilder.class));
    }

    private void signInWithProvider(Class<? extends AuthUI.IdpConfig.Builder> providerClass) {
        AuthUI.IdpConfig provider = null;

        if (providerClass.equals(AuthUI.IdpConfig.EmailBuilder.class)) {
            provider = new AuthUI.IdpConfig.EmailBuilder().build();
        } else if (providerClass.equals(AuthUI.IdpConfig.GoogleBuilder.class)) {
            provider = new AuthUI.IdpConfig.GoogleBuilder().build();
        }

        if (provider == null) return;

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.Base_Theme_Lab6_20211688)
                .setLogo(R.drawable.logo_momentaneo)
                .setAvailableProviders(Collections.singletonList(provider))
                .build();

        signInLauncher.launch(intent);
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "Login correcto: " + user.getUid());
                        goToMainActivity();
                    } else {
                        Log.d(TAG, "Login fallido: usuario null");
                    }
                } else {
                    Log.d(TAG, "Login cancelado por el usuario");
                }
            }
    );
}
