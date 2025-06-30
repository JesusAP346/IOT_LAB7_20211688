package com.example.lab6_20211688;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20211688.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fragmento inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Linea1Fragment())
                .commit();

        // Navegación inferior
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_linea1) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Linea1Fragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_limabus) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LimaPassFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_resumen) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ResumenFragment())
                        .commit();
                return true;
            } else if (id == R.id.lab7) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Laboratorio7Fragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_logout) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        });
                return true;
            }

            return false;
        });
    }
}
