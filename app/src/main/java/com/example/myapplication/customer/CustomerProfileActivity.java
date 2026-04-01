package com.example.myapplication.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.common.RoleSelectionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CustomerProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAge, etContact, etEmail, etAddress, etNIC, etPassword;
    private Button btnUpdate, btnLogout, btnDelete;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAddress = findViewById(R.id.etAddress);
        etNIC = findViewById(R.id.etNIC);

        btnUpdate = findViewById(R.id.btnUpdateProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnDelete = findViewById(R.id.btnDeleteAccount);

        loadUserData();

        btnUpdate.setOnClickListener(v -> updateProfileAndData());
        btnLogout.setOnClickListener(v -> logout());
        btnDelete.setOnClickListener(v -> deleteAccount());
    }

    private void loadUserData() {
        db.collection("Users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etFirstName.setText(doc.getString("firstName"));
                etLastName.setText(doc.getString("lastName"));
                etAge.setText(doc.getString("age"));
                etContact.setText(doc.getString("contactNumber"));
                etEmail.setText(doc.getString("email"));
                etAddress.setText(doc.getString("address"));
                etNIC.setText(doc.getString("nicLicense"));
                etPassword.setText(doc.getString("password"));
            }
        });
    }

    private void updateProfileAndData() {
        String newPassword = etPassword.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && !newPassword.isEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    saveAllDataToFirestore(newPassword);
                } else {
                    Toast.makeText(this, "Auth Update Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveAllDataToFirestore(String password) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("firstName", etFirstName.getText().toString());
        userUpdates.put("lastName", etLastName.getText().toString());
        userUpdates.put("age", etAge.getText().toString());
        userUpdates.put("contactNumber", etContact.getText().toString());
        userUpdates.put("address", etAddress.getText().toString());
        userUpdates.put("nicLicense", etNIC.getText().toString());
        userUpdates.put("password", password);

        db.collection("Users").document(userId).update(userUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Database Update Failed", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, RoleSelectionActivity.class));
        finishAffinity();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        db.collection("Users").document(userId).delete().addOnSuccessListener(aVoid -> {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, RoleSelectionActivity.class));
                    finishAffinity();
                }
            });
        });
    }
}