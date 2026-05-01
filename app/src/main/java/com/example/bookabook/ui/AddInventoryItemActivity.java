package com.example.bookabook.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.bookabook.R;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.StoreUser;
import com.example.bookabook.models.enums.InventoryStatus;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddInventoryItemActivity extends AppCompatActivity {

    private TextInputEditText etIsbn, etTitle, etAuthor, etDescription, etPublisher, etCategory, etLanguage, etPrice;
    private Button btnSaveBook, btnCancel, btnSelectImage;
    private TextView tvBookStatus, tvEditDetails;
    private ImageView ivCoverPreview;
    private LinearLayout llNewBookFields;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private boolean isExistingBook = false;
    private Book existingBook = null;
    private String lastCheckedIsbn = "";
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
                                .into(ivCoverPreview);
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
        setContentView(R.layout.activity_add_inventory_item);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        etIsbn = findViewById(R.id.etIsbn);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etDescription = findViewById(R.id.etDescription);
        etPublisher = findViewById(R.id.etPublisher);
        etCategory = findViewById(R.id.etCategory);
        etLanguage = findViewById(R.id.etLanguage);
        etPrice = findViewById(R.id.etPrice);
        btnSaveBook = findViewById(R.id.btnSaveBook);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        tvBookStatus = findViewById(R.id.tvBookStatus);
        tvEditDetails = findViewById(R.id.tvEditDetails);
        ivCoverPreview = findViewById(R.id.ivCoverPreview);
        llNewBookFields = findViewById(R.id.llNewBookFields);

        llNewBookFields.setVisibility(View.GONE);
        etLanguage.setText("he");

        etIsbn.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String isbn = etIsbn.getText().toString().trim();
                if (!TextUtils.isEmpty(isbn) && !isbn.equals(lastCheckedIsbn)) {
                    checkBookByIsbn(isbn);
                }
            }
        });

        btnSelectImage.setOnClickListener(v -> showImageSourceDialog());

        btnSaveBook.setOnClickListener(v -> saveInventoryItem());
        btnCancel.setOnClickListener(v -> finish());

        tvEditDetails.setOnClickListener(v -> {
            llNewBookFields.setVisibility(View.VISIBLE);
            tvEditDetails.setVisibility(View.GONE);
            setBookFieldsEnabled(true);
            btnSelectImage.setEnabled(true);
        });
    }

    private void checkBookByIsbn(String isbn) {
        lastCheckedIsbn = isbn;
        tvBookStatus.setText("Checking ISBN...");

        database.getReference("books").child(isbn).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        existingBook = snapshot.getValue(Book.class);
                        isExistingBook = true;

                        // Scenario A: Book Found
                        llNewBookFields.setVisibility(View.GONE);
                        ivCoverPreview.setVisibility(View.VISIBLE);
                        tvEditDetails.setVisibility(View.VISIBLE);
                        
                        fillBookFields(existingBook);
                        setBookFieldsEnabled(false);
                        btnSelectImage.setEnabled(false);
                        imageUri = null;

                        tvBookStatus.setText("Book found in database.");

                        if (existingBook != null
                                && existingBook.getCoverImageUrl() != null
                                && !existingBook.getCoverImageUrl().trim().isEmpty()) {
                            Glide.with(this)
                                    .load(existingBook.getCoverImageUrl())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(ivCoverPreview);
                        } else {
                            ivCoverPreview.setImageResource(R.drawable.ic_launcher_background);
                        }

                    } else {
                        isExistingBook = false;
                        existingBook = null;

                        // Scenario B: Book Not Found
                        llNewBookFields.setVisibility(View.VISIBLE);
                        ivCoverPreview.setVisibility(View.VISIBLE);
                        tvEditDetails.setVisibility(View.GONE);
                        
                        clearBookFields();
                        setBookFieldsEnabled(true);
                        btnSelectImage.setEnabled(true);

                        tvBookStatus.setText("Book not found. Please upload cover and fill details.");

                        showBookNotFoundDialog(isbn);
                    }
                })
                .addOnFailureListener(e -> {
                    tvBookStatus.setText("Failed to check ISBN");
                    Toast.makeText(this, "Error checking ISBN: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveInventoryItem() {
        String isbn = etIsbn.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(isbn) || TextUtils.isEmpty(priceText)) {
            Toast.makeText(this, "ISBN and Price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Scenario B validation (when llNewBookFields is visible)
        if (llNewBookFields.getVisibility() == View.VISIBLE && !isExistingBook) {
            String title = etTitle.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            
            if (imageUri == null || TextUtils.isEmpty(title) || TextUtils.isEmpty(author)) {
                Toast.makeText(this, "Cover, Title, and Author are required for new books", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        double price = Double.parseDouble(priceText);
        String uid = auth.getCurrentUser().getUid();

        database.getReference("storeUsers").child(uid).get().addOnSuccessListener(snapshot -> {
            StoreUser user = snapshot.getValue(StoreUser.class);
            if (user != null && user.getStoreId() != null) {
                // If the user expanded the fields, they might have edited them.
                // To be safe, if fields are visible, we should probably update the book record or at least use the values.
                if (isExistingBook && llNewBookFields.getVisibility() == View.GONE) {
                    saveInventory(user.getStoreId(), isbn, price, uid);
                } else {
                    // This covers both Scenario B (new book) and Scenario A with "Edit Details" active
                    if (imageUri != null) {
                        uploadImageAndSaveBook(user.getStoreId(), isbn, price, uid);
                    } else {
                        // If no new image, but we want to save book details
                        saveBookDetails(user.getStoreId(), isbn, price, uid, isExistingBook ? existingBook.getCoverImageUrl() : null);
                    }
                }
            }
        });
    }

    private void uploadImageAndSaveBook(String storeId, String isbn, double price, String uid) {
        StorageReference ref = storage.getReference().child("book_covers/" + UUID.randomUUID().toString());
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            saveBookDetails(storeId, isbn, price, uid, uri.toString());
        })).addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveBookDetails(String storeId, String isbn, double price, String uid, String coverUrl) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(etTitle.getText().toString().trim());

        String authorsStr = etAuthor.getText().toString().trim();
        if (!authorsStr.isEmpty()) {
            List<String> authorsList = Arrays.asList(authorsStr.split("\\s*,\\s*"));
            book.setAuthors(authorsList);
        }

        book.setDescription(etDescription.getText().toString().trim());
        book.setPublisher(etPublisher.getText().toString().trim());
        book.setCategory(etCategory.getText().toString().trim());
        book.setLanguage(etLanguage.getText().toString().trim());
        book.setDefaultPrice(price);
        book.setCreatedByStoreUserId(uid);
        book.setCoverImageUrl(coverUrl);
        book.setApproved(true);

        database.getReference("books").child(isbn).setValue(book)
                .addOnSuccessListener(unused -> saveInventory(storeId, isbn, price, uid));
    }

    private void saveInventory(String storeId, String isbn, double price, String uid) {
        InventoryItem item = new InventoryItem(isbn, storeId, price, InventoryStatus.OUT_OF_STOCK, 0, uid);
        database.getReference("inventory").child(storeId).child(isbn).setValue(item)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fillBookFields(Book book) {
        etTitle.setText(book.getTitle());
        
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            etAuthor.setText(String.join(", ", book.getAuthors()));
        } else {
            etAuthor.setText("");
        }

        etDescription.setText(book.getDescription());
        etPublisher.setText(book.getPublisher());
        etCategory.setText(book.getCategory());
        etLanguage.setText(book.getLanguage());
    }

    private void clearBookFields() {
        etTitle.setText("");
        etAuthor.setText("");
        etDescription.setText("");
        etPublisher.setText("");
        etCategory.setText("");
        etLanguage.setText("he");
        ivCoverPreview.setImageResource(R.drawable.ic_launcher_background);
        imageUri = null;
    }

    private void showBookNotFoundDialog(String isbn) {
        new AlertDialog.Builder(this)
                .setTitle("Book Not Found")
                .setMessage("No book with ISBN " + isbn + " was found.\nPlease scan/select a cover image and fill the book details manually.")
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
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
        // Explicit URI grants fix the Android 18 warning and the silent failure
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(cameraIntent);
    }

    private Uri createImageUri() {
        File file = new File(getCacheDir(), "cover_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                this,
                "com.example.bookabook.fileprovider",
                file
        );
    }

    private void setBookFieldsEnabled(boolean enabled) {
        etTitle.setEnabled(enabled);
        etAuthor.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        etPublisher.setEnabled(enabled);
        etCategory.setEnabled(enabled);
        etLanguage.setEnabled(enabled);
    }
}