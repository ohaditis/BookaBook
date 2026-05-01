package com.example.bookabook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookabook.R;
import com.example.bookabook.models.Store;
import com.example.bookabook.models.StoreUser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StoreProfileFragment extends Fragment {

    private TextInputEditText etStoreName, etStoreAddress, etStoreCity, etStoreDescription, etStoreUserPhone;
    private Button btnUpdateStoreProfile, btnLogoutStore;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String uid;
    private String currentStoreId;

    public StoreProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_profile, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getUid();

        etStoreName = view.findViewById(R.id.etStoreName);
        etStoreAddress = view.findViewById(R.id.etStoreAddress);
        etStoreCity = view.findViewById(R.id.etStoreCity);
        etStoreDescription = view.findViewById(R.id.etStoreDescription);
        etStoreUserPhone = view.findViewById(R.id.etStoreUserPhone);
        btnUpdateStoreProfile = view.findViewById(R.id.btnUpdateStoreProfile);
        btnLogoutStore = view.findViewById(R.id.btnLogoutStore);

        loadStoreUserData();

        btnUpdateStoreProfile.setOnClickListener(v -> updateStoreProfile());
        btnLogoutStore.setOnClickListener(v -> logout());

        return view;
    }

    private void loadStoreUserData() {
        if (uid == null) return;

        database.getReference("storeUsers").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    StoreUser storeUser = snapshot.getValue(StoreUser.class);
                    if (storeUser != null) {
                        etStoreUserPhone.setText(storeUser.getPhone());
                        currentStoreId = storeUser.getStoreId();
                        if (currentStoreId != null) {
                            loadStoreData(currentStoreId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadStoreData(String storeId) {
        database.getReference("stores").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    Store store = snapshot.getValue(Store.class);
                    if (store != null) {
                        etStoreName.setText(store.getStoreName());
                        etStoreAddress.setText(store.getAddress());
                        etStoreCity.setText(store.getCity());
                        etStoreDescription.setText(store.getDescription());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateStoreProfile() {
        String name = etStoreName.getText().toString().trim();
        String address = etStoreAddress.getText().toString().trim();
        String city = etStoreCity.getText().toString().trim();
        String description = etStoreDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(city)) {
            Toast.makeText(requireContext(), "Name, Address and City are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStoreId == null) return;

        database.getReference("stores").child(currentStoreId).child("storeName").setValue(name);
        database.getReference("stores").child(currentStoreId).child("address").setValue(address);
        database.getReference("stores").child(currentStoreId).child("city").setValue(city);
        database.getReference("stores").child(currentStoreId).child("description").setValue(description)
                .addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Store profile updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}