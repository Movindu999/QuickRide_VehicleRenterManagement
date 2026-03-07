package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class CustomerLoginActivity extends AppCompatActivity {

    Button btnLogin;
    TextView tvSignUpHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        btnLogin = findViewById(R.id.btnLogin);
        tvSignUpHere = findViewById(R.id.tvSignUpHere);

        // Temporary login action - directly open dashboard
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(CustomerLoginActivity.this, CustomerDashboardActivity.class)));

        // Temporary signup click
        tvSignUpHere.setOnClickListener(v ->
                startActivity(new Intent(CustomerLoginActivity.this, CustomerDashboardActivity.class)));
    }
}