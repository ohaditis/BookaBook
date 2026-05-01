package com.example.bookabook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookabook.R;
import com.example.bookabook.models.StoreUser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class StoreLoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etPassword;
    private Button btnStoreLogin, btnGoToStoreRegister;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnStoreLogin = findViewById(R.id.btnStoreLogin);
        btnGoToStoreRegister = findViewById(R.id.btnGoToStoreRegister);

        btnStoreLogin.setOnClickListener(v -> loginStoreUser());

        btnGoToStoreRegister.setOnClickListener(v ->
                startActivity(new Intent(StoreLoginActivity.this, StoreRegisterActivity.class)));
    }

    private void loginStoreUser() {
        String phoneInput = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput)) {
            etPhone.setError("Phone number is required");
            return;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber numberProto = phoneUtil.parse(phoneInput, "IL");
            if (!phoneUtil.isValidNumber(numberProto)) {
                etPhone.setError("Enter a valid phone number");
                return;
            }
            String formattedPhone = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return;
            }

            // Generate the dummy email using the standardized E.164 format
            String dummyEmail = formattedPhone + "@bookabook.store";

            auth.signInWithEmailAndPassword(dummyEmail, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();
                            checkIfStoreUserApproved(uid);
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(StoreLoginActivity.this, "Login failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (NumberParseException e) {
            etPhone.setError("Invalid phone number format");
        }
    }

    private void checkIfStoreUserApproved(String uid) {
        database.getReference("storeUsers")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        auth.signOut();
                        Toast.makeText(StoreLoginActivity.this, "Not a store user account.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    StoreUser storeUser = snapshot.getValue(StoreUser.class);
                    if (storeUser == null || !storeUser.isActive()) {
                        auth.signOut();
                        startActivity(new Intent(StoreLoginActivity.this, PendingApprovalActivity.class));
                        finish();
                        return;
                    }

                    if (storeUser.getStoreId() == null || storeUser.getStoreId().isEmpty()) {
                        startActivity(new Intent(StoreLoginActivity.this, CreateStoreActivity.class));
                    } else {
                        startActivity(new Intent(StoreLoginActivity.this, StoreMainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    auth.signOut();
                    Toast.makeText(StoreLoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
