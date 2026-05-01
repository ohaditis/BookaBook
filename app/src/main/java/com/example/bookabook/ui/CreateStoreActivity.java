package com.example.bookabook.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.bookabook.R;
import com.example.bookabook.models.Store;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.UUID;

public class CreateStoreActivity extends AppCompatActivity {

    private TextInputEditText etStoreName, etAddress, etCity, etPhone, etDescription;
    private Button btnCreateStore, btnCancel, btnSelectStoreImage;
    private ImageView ivStoreImagePreview;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private Uri imageUri = null;

    // Handles both gallery picks AND camera captures
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri resultUri = (result.getData() != null && result.getData().getData() != null)
                            ? result.getData().getData()
                            : imageUri;

                    if (resultUri != null) {
                        imageUri = resultUri;
                        Glide.with(this)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(ivStoreImagePreview);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_store);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        etStoreName = findViewById(R.id.etStoreName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        btnCreateStore = findViewById(R.id.btnCreateStore);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectStoreImage = findViewById(R.id.btnSelectStoreImage);
        ivStoreImagePreview = findViewById(R.id.ivStoreImagePreview);

        btnCreateStore.setOnClickListener(v -> validateAndCreateStore());
        btnCancel.setOnClickListener(v -> finish());
        btnSelectStoreImage.setOnClickListener(v -> showImageSourceDialog());
    }

    private void showImageSourceDialog() {
        String[] options = {"Gallery", "Camera"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        imageUri = createImageUri();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(cameraIntent);
    }

    private Uri createImageUri() {
        File file = new File(getCacheDir(), "store_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, "com.example.bookabook.fileprovider", file);
    }

    private void validateAndCreateStore() {
        String name = etStoreName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etStoreName.setError("Store name required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address required");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            etCity.setError("City required");
            return;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 7) {
            etPhone.setError("Enter valid phone number");
            return;
        }

        String uid = auth.getUid();
        if (uid == null) return;

        String storeId = database.getReference("stores").push().getKey();
        if (storeId == null) return;

        if (imageUri != null) {
            uploadImageAndSaveStore(storeId, name, address, city, phone, description, uid);
        } else {
            // No image selected, use null (app will use default bg when displaying)
            saveStoreToDatabase(storeId, name, address, city, phone, description, uid, null);
        }
    }

    private void uploadImageAndSaveStore(String storeId, String name, String address, String city, String phone, String description, String uid) {
        StorageReference ref = storage.getReference().child("store_images/" + UUID.randomUUID().toString());
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            saveStoreToDatabase(storeId, name, address, city, phone, description, uid, uri.toString());
        })).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Optionally save without image if upload fails
            saveStoreToDatabase(storeId, name, address, city, phone, description, uid, null);
        });
    }

    private void saveStoreToDatabase(String storeId, String name, String address, String city, String phone, String description, String uid, String imageUrl) {
        Store store = new Store(
                storeId,
                name,
                address,
                city,
                phone,
                description,
                uid,
                imageUrl,
                System.currentTimeMillis()
        );

        database.getReference("stores").child(storeId).setValue(store)
                .addOnSuccessListener(unused -> {
                    database.getReference("storeUsers").child(uid).child("storeId").setValue(storeId)
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Store created successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, StoreMainActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}