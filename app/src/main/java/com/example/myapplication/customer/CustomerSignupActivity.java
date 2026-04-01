package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
<<<<<<< Updated upstream

public class CustomerSignupActivity extends AppCompatActivity {

    Button btnSignIn;
=======
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerSignupActivity extends AppCompatActivity {

    private Button btnSignIn;
    private EditText etFirstName, etLastName, etAge, etNicLicense, etContact, etEmail, etAddress, etPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

<<<<<<< Updated upstream
=======
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

>>>>>>> Stashed changes
        btnSignIn = findViewById(R.id.btnSignIn);

<<<<<<< Updated upstream
        // Temporary action - directly open dashboard
        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(CustomerSignupActivity.this, CustomerDashboardActivity.class)));
=======
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
                        saveUserDataToFirestore(firstName, lastName, age, nicLicense, contact, email, address, password);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Signup Failed";
                        Toast.makeText(CustomerSignupActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String fName, String lName, String age, String nic, String phone, String email, String addr, String pass) {
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> customer = new HashMap<>();
        customer.put("firstName", fName);
        customer.put("lastName", lName);
        customer.put("age", age);
        customer.put("nicLicense", nic);
        customer.put("contactNumber", phone);
        customer.put("email", email);
        customer.put("address", addr);
        customer.put("password", pass); // Now saving password to Firestore for easy loading
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
>>>>>>> Stashed changes
    }
}