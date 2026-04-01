package com.example.myapplication.admin;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ManageCustomersActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private FirebaseFirestore db;
    private CustomerAdapter adapter;
    private List<DocumentSnapshot> customerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customers);

        db = FirebaseFirestore.getInstance();
        rvCustomers = findViewById(R.id.rvCustomers);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        customerList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Customers");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        loadCustomers();
    }

    private void loadCustomers() {
        // Listening for real-time updates
        db.collection("Users")
                .whereEqualTo("role", "customer")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        customerList = value.getDocuments();
                        adapter = new CustomerAdapter(customerList, this::toggleBlockStatus);
                        rvCustomers.setAdapter(adapter);
                    }
                });
    }

    private void toggleBlockStatus(DocumentSnapshot doc) {
        String currentStatus = doc.getString("status");
        if (currentStatus == null) currentStatus = "active";

        String newStatus = "blocked".equals(currentStatus) ? "active" : "blocked";

        db.collection("Users").document(doc.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "User status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}