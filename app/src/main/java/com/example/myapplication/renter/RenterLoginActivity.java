package com.example.myapplication.renter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RenterLoginActivity extends AppCompatActivity {

    private static final String TAG = "RenterLoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(R.string.screen_renter_login);

        TextInputEditText etUsername = findViewById(R.id.etUsername); // Used as Email
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            Log.d(TAG, "Login button clicked — email: " + email);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            loginRenter(email, password);
        });
    }

    private void loginRenter(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth success. Checking user role...");
                        verifyRenterRole();
                    } else {
                        Log.e(TAG, "Login Failed: " + task.getException().getMessage());
                        Toast.makeText(RenterLoginActivity.this, "Login Failed. Check credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyRenterRole() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String status = documentSnapshot.getString("status");

                        if ("renter".equals(role)) {
                            if ("blocked".equals(status)) {
                                mAuth.signOut();
                                Toast.makeText(this, "Your account is blocked by Admin.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            }
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this, "Access Denied. You are not registered as a Renter.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mAuth.signOut();
                        Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data: " + e.getMessage());
                    Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDashboard() {
        Log.d(TAG, "Navigating to RenterDashboardActivity");
        Intent intent = new Intent(RenterLoginActivity.this, RenterDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}