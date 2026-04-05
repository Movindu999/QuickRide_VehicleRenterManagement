package com.example.myapplication.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        boolean isSuspended = "suspended".equalsIgnoreCase(currentStatus)
                || "blocked".equalsIgnoreCase(currentStatus);

        if (!isSuspended) {
            showSuspendDialog(doc);
        } else {
            updateCustomerStatus(doc, "active", null);
        }
    }

    private void showSuspendDialog(DocumentSnapshot doc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suspend Customer");
        builder.setMessage("Please enter the reason for suspension (Suspend Notice):");

        final EditText input = new EditText(this);
        input.setHint("e.g. Violation of terms");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Suspend", (dialog, which) -> {
            String notice = input.getText().toString().trim();
            if (notice.isEmpty()) {
                Toast.makeText(this, "A reason is required to suspend a customer.", Toast.LENGTH_SHORT).show();
            } else {
                updateCustomerStatus(doc, "suspended", notice);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateCustomerStatus(DocumentSnapshot doc, String newStatus, String notice) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        if ("suspended".equals(newStatus) && notice != null) {
            updates.put("suspendNotice", notice);
        } else {
            updates.put("suspendNotice", FieldValue.delete());
        }

        db.collection("Users").document(doc.getId())
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                "Customer status updated to " + newStatus,
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}