package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Firestore Import

public class CustomerLoginActivity extends AppCompatActivity {

    Button btnLogin;
    TextView tvSignUpHere;
    EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore Import

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Firestore Import

        btnLogin = findViewById(R.id.btnLogin);
        tvSignUpHere = findViewById(R.id.tvSignUpHere);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignUpHere.setOnClickListener(v ->
                startActivity(new Intent(CustomerLoginActivity.this, CustomerSignupActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login Failed";
                        Toast.makeText(CustomerLoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("customer".equals(role)) {
                            Toast.makeText(CustomerLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CustomerLoginActivity.this, CustomerDashboardActivity.class));
                            finish();
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this, "Access Denied: Not a Customer account", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Error verifying role", Toast.LENGTH_SHORT).show();
                });
    }
}