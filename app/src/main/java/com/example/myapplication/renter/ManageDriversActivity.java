package com.example.myapplication.renter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageDriversActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etAge, etEmail, etContact, etLicense, etIdNumber, etPassword, etRePassword;
    private MaterialButton btnAddDriver;
    private RecyclerView rvDrivers;
    private MaterialToolbar topAppBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FirebaseApp secondaryApp;
    private FirebaseAuth secondaryAuth;

    private DriverAdapter adapter;
    private List<DocumentSnapshot> driverList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drivers);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        try {
            secondaryApp = FirebaseApp.getInstance("SecondaryApp");
        } catch (IllegalStateException e) {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            secondaryApp = FirebaseApp.initializeApp(getApplicationContext(), options, "SecondaryApp");
        }
        secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        etFirstName = findViewById(R.id.etDriverFirstName);
        etLastName = findViewById(R.id.etDriverLastName);
        etAge = findViewById(R.id.etDriverAge);
        etEmail = findViewById(R.id.etDriverEmail);
        etContact = findViewById(R.id.etDriverContact);
        etLicense = findViewById(R.id.etDriverLicense);
        etIdNumber = findViewById(R.id.etDriverIdNumber);
        etPassword = findViewById(R.id.etDriverPassword);
        etRePassword = findViewById(R.id.etDriverRePassword);

        btnAddDriver = findViewById(R.id.btnAddDriver);
        rvDrivers = findViewById(R.id.rvDrivers);

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> finish());

        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        driverList = new ArrayList<>();

        btnAddDriver.setOnClickListener(v -> addDriver());

        loadDrivers();
    }

    private void addDriver() {
        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString().trim() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString().trim() : "";
        String age = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String license = etLicense.getText() != null ? etLicense.getText().toString().trim() : "";
        String idNumber = etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String rePassword = etRePassword.getText() != null ? etRePassword.getText().toString().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || email.isEmpty() || contact.isEmpty() || license.isEmpty() || idNumber.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) return;
        String renterId = mAuth.getCurrentUser().getUid();

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String driverUid = task.getResult().getUser().getUid();

                        Map<String, Object> driverData = new HashMap<>();
                        driverData.put("renterId", renterId);
                        driverData.put("firstName", firstName);
                        driverData.put("lastName", lastName);
                        driverData.put("age", age);
                        driverData.put("email", email);
                        driverData.put("contactNumber", contact);
                        driverData.put("licenseNumber", license);
                        driverData.put("idNumber", idNumber);
                        driverData.put("role", "driver");

                        db.collection("Drivers").document(driverUid).set(driverData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Driver Registered Successfully in Auth & Database", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                    secondaryAuth.signOut();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error saving driver data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadDrivers() {
        if (mAuth.getCurrentUser() == null) return;
        String renterId = mAuth.getCurrentUser().getUid();

        db.collection("Drivers")
                .whereEqualTo("renterId", renterId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        driverList = value.getDocuments();
                        adapter = new DriverAdapter(driverList, this::showDeleteConfirmation);
                        rvDrivers.setAdapter(adapter);
                    }
                });
    }

    private void showDeleteConfirmation(DocumentSnapshot doc) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Driver")
                .setMessage("Are you sure you want to remove this driver?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDriver(doc))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteDriver(DocumentSnapshot doc) {
        db.collection("Drivers").document(doc.getId()).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Driver Removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting driver", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etFirstName.setText("");
        etLastName.setText("");
        etAge.setText("");
        etEmail.setText("");
        etContact.setText("");
        etLicense.setText("");
        etIdNumber.setText("");
        etPassword.setText("");
        etRePassword.setText("");
    }
}