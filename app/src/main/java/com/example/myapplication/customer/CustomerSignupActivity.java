package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerSignupActivity extends AppCompatActivity {

    private Button btnSignIn;
    private EditText etFirstName, etLastName, etAge, etNicLicense, etContact, etEmail, etAddress, etPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        etNicLicense = findViewById(R.id.etNicLicense);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String nicLicense = etNicLicense.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserDataToFirestore(firstName, lastName, age, nicLicense, contact, email, address);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Signup Failed";
                        Toast.makeText(CustomerSignupActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String fName, String lName, String age, String nic, String phone, String email, String addr) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Could not load user account", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> customer = new HashMap<>();
        customer.put("firstName", fName);
        customer.put("lastName", lName);
        customer.put("age", age);
        customer.put("nicLicense", nic);
        customer.put("contactNumber", phone);
        customer.put("email", email);
        customer.put("address", addr);
        customer.put("role", "customer");
        customer.put("userId", userId);

        db.collection("Users").document(userId)
                .set(customer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CustomerSignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CustomerSignupActivity.this, CustomerDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CustomerSignupActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}