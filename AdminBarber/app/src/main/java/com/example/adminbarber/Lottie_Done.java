package com.example.adminbarber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Lottie_Done extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie__done);

        new Handler().postDelayed(
                () -> startActivity(new Intent(Lottie_Done.this, MainActivity.class))
                , 4000);
    }
}
