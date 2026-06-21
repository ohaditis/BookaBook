package com.example.bookabook.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

public class StoreRegisterActivity extends AppCompatActivity {

    private TextInputEditText etPhoneLocal, etPhoneLocalConfirm, etPassword;
    private Button btnRegisterStoreUser, btnCancel;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        etPhoneLocal = findViewById(R.id.etPhoneLocal);
        etPhoneLocalConfirm = findViewById(R.id.etPhoneLocalConfirm);
        etPassword = findViewById(R.id.etPassword);
        btnRegisterStoreUser = findViewById(R.id.btnRegisterStoreUser);
        btnCancel = findViewById(R.id.btnCancel);

        btnRegisterStoreUser.setOnClickListener(v -> registerStoreUser());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void registerStoreUser() {
        String phoneInput = etPhoneLocal.getText().toString().trim();
        String phoneConfirm = etPhoneLocalConfirm.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput)) {
            etPhoneLocal.setError("Phone number is required");
            return;
        }

        if (!phoneInput.equals(phoneConfirm)) {
            etPhoneLocalConfirm.setError(getString(R.string.error_phones_dont_match));
            return;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // Parse with "IL" (Israel) as default region since country code is +972
            PhoneNumber numberProto = phoneUtil.parse(phoneInput, "IL");
            if (!phoneUtil.isValidNumber(numberProto)) {
                etPhoneLocal.setError("Enter a valid phone number");
                return;
            }
            // Format to E.164 (e.g., +9725XXXXXXXX)
            String formattedPhone = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password required");
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            String dummyEmail = formattedPhone + "@bookabook.store";

            auth.createUserWithEmailAndPassword(dummyEmail, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();
                            StoreUser storeUser = new StoreUser(uid, dummyEmail, formattedPhone, false);

                            database.getReference("storeUsers")
                                    .child(uid)
                                    .setValue(storeUser)
                                    .addOnSuccessListener(n -> AlertDialogSuccess(this))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save store user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (NumberParseException e) {
            etPhoneLocal.setError("Invalid phone number format");
        }
    }

    private void AlertDialogSuccess(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Registration Successful")
                .setMessage("User created successfully. Waiting for admin approval.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    auth.signOut();
                    startActivity(new Intent(StoreRegisterActivity.this, StoreLoginActivity.class));
                    finish();
                })
                .show();
    }
}