package com.example.myapplication.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.AdminDashboardActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Get Admin button from XML
        Button btnAdmin = findViewById(R.id.btnAdmin);

        // When Admin button clicked → open Admin Dashboard
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });
    }
}
