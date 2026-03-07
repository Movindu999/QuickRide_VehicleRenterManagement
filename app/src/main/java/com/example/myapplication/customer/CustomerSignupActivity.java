package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class CustomerSignupActivity extends AppCompatActivity {

    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

        btnSignIn = findViewById(R.id.btnSignIn);

        // Temporary action - directly open dashboard
        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(CustomerSignupActivity.this, CustomerDashboardActivity.class)));
    }
}