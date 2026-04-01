package com.example.myapplication.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageRentersActivity extends AppCompatActivity {

    private EditText etOrg, etOwner, etLoc, etEmail, etPass;
    private Button btnAdd;
    private RecyclerView rvRenters;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RenterAdapter adapter;
    private List<DocumentSnapshot> renterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_renters);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etOrg = findViewById(R.id.etOrgName);
        etOwner = findViewById(R.id.etOwnerName);
        etLoc = findViewById(R.id.etLocation);
        etEmail = findViewById(R.id.etRenterEmail);
        etPass = findViewById(R.id.etRenterPassword);
        btnAdd = findViewById(R.id.btnAddRenter);
        rvRenters = findViewById(R.id.rvRenters);

        rvRenters.setLayoutManager(new LinearLayoutManager(this));
        renterList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> registerRenter());

        loadRenters();
    }

    private void registerRenter() {
        String org = etOrg.getText().toString().trim();
        String owner = etOwner.getText().toString().trim();
        String loc = etLoc.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (org.isEmpty() || owner.isEmpty() || loc.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    saveRenterToFirestore(uid, org, owner, loc, email);
                }
            } else {
                Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRenterToFirestore(String uid, String org, String owner, String loc, String email) {
        Map<String, Object> renter = new HashMap<>();
        renter.put("orgName", org);
        renter.put("ownerName", owner);
        renter.put("location", loc);
        renter.put("email", email);
        renter.put("role", "renter");
        renter.put("status", "active");

        db.collection("Users").document(uid).set(renter)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Renter Registered Successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRenters() {
        db.collection("Users")
                .whereEqualTo("role", "renter")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        renterList = value.getDocuments();
                        adapter = new RenterAdapter(renterList, this::showDeleteConfirmation);
                        rvRenters.setAdapter(adapter);
                    }
                });
    }

    private void showDeleteConfirmation(DocumentSnapshot doc) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Renter")
                .setMessage("Are you sure you want to delete this renter? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> deleteRenter(doc))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteRenter(DocumentSnapshot doc) {
        db.collection("Users").document(doc.getId()).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Renter Removed Successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Delete Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etOrg.setText("");
        etOwner.setText("");
        etLoc.setText("");
        etEmail.setText("");
        etPass.setText("");
    }
}