package com.example.adminbarber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BiometricAuth extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_auth);

        final Executor executor = Executors.newSingleThreadExecutor();

        final BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Subtitle")
                .setSubtitle("Hold your finger at least 5 second")
                .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(BiometricAuth.this, "Cancel authentication", Toast.LENGTH_SHORT).show();
                    }
                }).build();

        Button auth = findViewById(R.id.login);

        final BiometricAuth activity = this;

        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BiometricAuth.this, "Authenticated sucess", Toast.LENGTH_SHORT).show();
                                goToHome();
                            }
                        });
                    }
                });
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(BiometricAuth.this, Lottie_Done.class);
        startActivity(intent);
    }
}

