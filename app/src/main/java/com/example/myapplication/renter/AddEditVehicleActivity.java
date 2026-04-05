package com.example.myapplication.renter;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditVehicleActivity extends AppCompatActivity {

    // Cloudinary configuration
    private static final String CLOUDINARY_CLOUD_NAME = "dyatd1re4";
    private static final String CLOUDINARY_UPLOAD_PRESET = "ml_default";

    private String vehicleType;
    private String vehicleId;
    private EditText etVehicleNumber;
    private AutoCompleteTextView spnVehicleType;
    private EditText etPricePerDay;
    private EditText etDriverFeePerDay;
    private LinearLayout containerVehicleImages;
    private Button btnAddVehicle;
    private Button btnUpdateVehicle;
    private Button btnRemoveVehicle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Uri selectedImageUri;
    private String existingImageUrl;
    private ImageButton btnAddImage;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    renderImagePreview();
                }
            });

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_vehicle);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        vehicleType = getIntent().getStringExtra("vehicleType");
        boolean isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        vehicleId = getIntent().getStringExtra("vehicleId");

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setTitle(isEditMode ? "Update Vehicle" : "Add Vehicle");
        topAppBar.setNavigationOnClickListener(v -> finish());

        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        spnVehicleType = findViewById(R.id.spnVehicleType);
        etPricePerDay = findViewById(R.id.etPricePerDay);
        etDriverFeePerDay = findRuntimeEditText("etDriverFeePerDay");
        containerVehicleImages = findViewById(R.id.containerVehicleImages);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnUpdateVehicle = findViewById(R.id.btnUpdateVehicle);
        btnRemoveVehicle = findViewById(R.id.btnRemoveVehicle);
        btnAddImage = findViewById(R.id.btnAddImage);

        setupVehicleTypeSpinner();
        renderImagePreview();

        btnAddImage.setOnClickListener(v -> openImagePicker());

        if (isEditMode) {
            btnAddVehicle.setVisibility(View.GONE);
            btnUpdateVehicle.setVisibility(View.VISIBLE);
            btnRemoveVehicle.setVisibility(View.VISIBLE);
            loadVehicleData();
            btnUpdateVehicle.setOnClickListener(v -> upsertVehicle(true));
            btnRemoveVehicle.setOnClickListener(v -> removeVehicle());
        } else {
            btnAddVehicle.setVisibility(View.VISIBLE);
            btnUpdateVehicle.setVisibility(View.GONE);
            btnRemoveVehicle.setVisibility(View.GONE);
            btnAddVehicle.setOnClickListener(v -> upsertVehicle(false));
        }
    }

    private void setupVehicleTypeSpinner() {
        String[] vehicleTypes = new String[]{"Car", "Van", "Motorbike", "Bus", "Tuktuk"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );
        spnVehicleType.setAdapter(adapter);

        if (vehicleType != null) {
            spnVehicleType.setText(vehicleType, false);
        } else {
            spnVehicleType.setText(vehicleTypes[0], false);
        }
    }

    private void upsertVehicle(boolean isUpdate) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String vehicleNumber = safeText(etVehicleNumber);
        String vehicleTypeSelected = safeText(spnVehicleType);
        String pricePerDayText = safeText(etPricePerDay);
        String driverFeePerDayText = safeText(etDriverFeePerDay);

        if (vehicleNumber.isEmpty() || vehicleTypeSelected.isEmpty() || pricePerDayText.isEmpty() || driverFeePerDayText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerDay;
        double driverFeePerDay;
        try {
            pricePerDay = Double.parseDouble(pricePerDayText);
            driverFeePerDay = Double.parseDouble(driverFeePerDayText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid prices", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        String renterId = mAuth.getCurrentUser().getUid();
        DocumentReference docRef;

        if (isUpdate) {
            if (TextUtils.isEmpty(vehicleId)) {
                Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
                setLoadingState(false);
                return;
            }
            docRef = db.collection("Vehicles").document(vehicleId);
        } else {
            docRef = db.collection("Vehicles").document();
            vehicleId = docRef.getId();
        }

        if (selectedImageUri != null) {
            uploadImageToCloudinary(selectedImageUri, url -> {
                existingImageUrl = url;
                writeVehicleDocument(docRef, renterId, vehicleNumber, vehicleTypeSelected, pricePerDay, driverFeePerDay, isUpdate);
            }, errorMessage -> {
                setLoadingState(false);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            });
            return;
        }

        writeVehicleDocument(docRef, renterId, vehicleNumber, vehicleTypeSelected, pricePerDay, driverFeePerDay, isUpdate);
    }

    private interface UrlCallback {
        void onSuccess(String url);
    }

    private interface ErrorCallback {
        void onError(String message);
    }

    private void uploadImageToCloudinary(Uri imageUri, UrlCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                byte[] imageBytes = readImageBytes(imageUri);
                if (imageBytes == null || imageBytes.length == 0) {
                    runOnUiThread(() -> onError.onError("Image read failed"));
                    return;
                }

                String mimeType = getContentResolver().getType(imageUri);
                if (TextUtils.isEmpty(mimeType)) {
                    mimeType = "image/jpeg";
                }

                String boundary = "----QuickRideBoundary" + System.currentTimeMillis();
                URL url = new URL("https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("User-Agent", "QuickRide Android");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.write(("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.write(CLOUDINARY_UPLOAD_PRESET.getBytes(StandardCharsets.UTF_8));
                    outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

                    outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"vehicle_image\"\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.write(imageBytes);
                    outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

                    outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                InputStream responseStream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
                String responseBody = readFully(responseStream);

                if (responseCode < 200 || responseCode >= 300) {
                    runOnUiThread(() -> onError.onError("Image upload failed: HTTP " + responseCode));
                    return;
                }

                JSONObject jsonObject = new JSONObject(responseBody);
                String secureUrl = jsonObject.optString("secure_url", null);
                if (TextUtils.isEmpty(secureUrl)) {
                    secureUrl = jsonObject.optString("url", null);
                }

                if (TextUtils.isEmpty(secureUrl)) {
                    runOnUiThread(() -> onError.onError("Image upload failed: no URL returned"));
                    return;
                }

                String finalSecureUrl = secureUrl;
                runOnUiThread(() -> onSuccess.onSuccess(finalSecureUrl));
            } catch (Exception e) {
                runOnUiThread(() -> onError.onError("Image upload failed: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private byte[] readImageBytes(Uri uri) throws IOException {
        if (uri == null) {
            return null;
        }

        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    private String readFully(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8 * 1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, charsRead);
            }
            return builder.toString();
        }
    }

    private void writeVehicleDocument(
            DocumentReference docRef,
            String renterId,
            String vehicleNumber,
            String vehicleTypeSelected,
            double pricePerDay,
            double driverFeePerDay,
            boolean isUpdate
    ) {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("renterId", renterId);
        vehicleData.put("vehicleNumber", vehicleNumber);
        vehicleData.put("vehicleType", vehicleTypeSelected);
        vehicleData.put("pricePerDay", pricePerDay);
        vehicleData.put("driverFeePerDay", driverFeePerDay);
        vehicleData.put("updatedAt", Timestamp.now());

        if (!TextUtils.isEmpty(existingImageUrl)) {
            vehicleData.put("imageUrl", existingImageUrl);
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(existingImageUrl);
            vehicleData.put("imageUrls", imageUrls);
        }

        if (!isUpdate) {
            vehicleData.put("createdAt", Timestamp.now());
        }

        docRef.set(vehicleData)
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    Toast.makeText(this, isUpdate ? "Vehicle updated successfully" : "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Failed to save vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadVehicleData() {
        if (TextUtils.isEmpty(vehicleId)) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Vehicles").document(vehicleId)
                .get()
                .addOnSuccessListener(this::bindVehicleData)
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Failed to load vehicle: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void bindVehicleData(DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etVehicleNumber.setText(documentSnapshot.getString("vehicleNumber"));
        String type = documentSnapshot.getString("vehicleType");
        if (!TextUtils.isEmpty(type)) {
            spnVehicleType.setText(type, false);
        }

        Double price = documentSnapshot.getDouble("pricePerDay");
        if (price != null) {
            if (price == Math.floor(price)) {
                etPricePerDay.setText(String.valueOf(price.intValue()));
            } else {
                etPricePerDay.setText(String.valueOf(price));
            }
        }

        Double driverFee = documentSnapshot.getDouble("driverFeePerDay");
        if (driverFee != null) {
            if (driverFee == Math.floor(driverFee)) {
                etDriverFeePerDay.setText(String.valueOf(driverFee.intValue()));
            } else {
                etDriverFeePerDay.setText(String.valueOf(driverFee));
            }
        }

        existingImageUrl = documentSnapshot.getString("imageUrl");
        if (TextUtils.isEmpty(existingImageUrl)) {
            List<String> imageUrls = extractStringList(documentSnapshot.get("imageUrls"));
            if (!imageUrls.isEmpty()) {
                existingImageUrl = imageUrls.get(0);
            }
        }
        renderImagePreview();
    }

    private void removeVehicle() {
        if (TextUtils.isEmpty(vehicleId)) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        db.collection("Vehicles").document(vehicleId)
                .delete()
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Vehicle removed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Failed to remove vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderImagePreview() {
        containerVehicleImages.removeAllViews();

        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(180)
        );
        cardParams.bottomMargin = dpToPx(8);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(8));
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(v -> openImagePicker());

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setClickable(true);
        imageView.setFocusable(true);
        imageView.setOnClickListener(v -> openImagePicker());

        if (selectedImageUri != null) {
            Glide.with(this).load(selectedImageUri).into(imageView);
            cardView.addView(imageView);
            containerVehicleImages.addView(cardView);
            return;
        }

        if (!TextUtils.isEmpty(existingImageUrl)) {
            loadStoredImage(imageView, existingImageUrl);
            cardView.addView(imageView);
            containerVehicleImages.addView(cardView);
            return;
        }

        LinearLayout placeholderLayout = new LinearLayout(this);
        placeholderLayout.setOrientation(LinearLayout.VERTICAL);
        placeholderLayout.setGravity(Gravity.CENTER);
        placeholderLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        placeholderLayout.setBackgroundColor(0xFFBDBDBD);
        placeholderLayout.setClickable(true);
        placeholderLayout.setFocusable(true);
        placeholderLayout.setOnClickListener(v -> openImagePicker());

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.ic_menu_gallery);
        icon.setColorFilter(0xFFFFFFFF);
        icon.setClickable(true);
        icon.setFocusable(true);
        icon.setOnClickListener(v -> openImagePicker());

        TextView label = new TextView(this);
        label.setText(getString(R.string.tap_to_add_image));
        label.setTextColor(0xFFFFFFFF);
        label.setPadding(0, dpToPx(8), 0, 0);
        label.setClickable(true);
        label.setFocusable(true);
        label.setOnClickListener(v -> openImagePicker());

        placeholderLayout.addView(icon);
        placeholderLayout.addView(label);
        cardView.addView(placeholderLayout);
        containerVehicleImages.addView(cardView);
    }

    private void loadStoredImage(ImageView imageView, String source) {
        if (TextUtils.isEmpty(source)) {
            return;
        }

        try {
            if (source.startsWith("http://") || source.startsWith("https://") || source.startsWith("content://")) {
                Glide.with(this).load(Uri.parse(source)).into(imageView);
            } else {
                Glide.with(this).load(new File(source)).into(imageView);
            }
        } catch (Exception e) {
            Glide.with(this).load(android.R.drawable.ic_menu_report_image).into(imageView);
        }
    }

    private void setLoadingState(boolean isLoading) {
        btnAddVehicle.setEnabled(!isLoading);
        btnUpdateVehicle.setEnabled(!isLoading);
        btnRemoveVehicle.setEnabled(!isLoading);
        btnAddImage.setEnabled(!isLoading);
        etVehicleNumber.setEnabled(!isLoading);
        spnVehicleType.setEnabled(!isLoading);
        etPricePerDay.setEnabled(!isLoading);
        if (etDriverFeePerDay != null) {
            etDriverFeePerDay.setEnabled(!isLoading);
        }
    }

    private String safeText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private List<String> extractStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (!(value instanceof List<?>)) {
            return result;
        }
        for (Object item : (List<?>) value) {
            if (item instanceof String) {
                result.add((String) item);
            }
        }
        return result;
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        ));
    }

    private EditText findRuntimeEditText(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        return id != 0 ? findViewById(id) : null;
    }
}
