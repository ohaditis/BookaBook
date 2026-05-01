package com.example.bookabook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookabook.R;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.Store;
import com.example.bookabook.models.StoreUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreHomeActivity extends AppCompatActivity {

    private TextView tvStoreName;
    private TextView tvStoreAddress;
    private TextView tvStorePhone;
    private RecyclerView rvInventory;
    private FloatingActionButton fabAddBook;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private InventoryAdapter adapter;
    private String currentStoreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_home);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        tvStoreName = findViewById(R.id.tvStoreName);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        tvStorePhone = findViewById(R.id.tvStorePhone);
        rvInventory = findViewById(R.id.rvInventory);
        fabAddBook = findViewById(R.id.fabAddBook);

        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter();
        rvInventory.setAdapter(adapter);

        fabAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddInventoryItemActivity.class)));

        loadStoreData();
    }

    private void loadStoreData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        database.getReference("storeUsers")
                .child(uid)
                .get()
                .addOnSuccessListener(storeUserSnapshot -> {
                    StoreUser storeUser = storeUserSnapshot.getValue(StoreUser.class);
                    if (storeUser != null && storeUser.getStoreId() != null) {
                        currentStoreId = storeUser.getStoreId();
                        loadStoreById(currentStoreId);
                        listenForInventoryChanges(currentStoreId);
                    }
                });
    }

    private void loadStoreById(String storeId) {
        database.getReference("stores").child(storeId).get()
                .addOnSuccessListener(snapshot -> {
                    Store store = snapshot.getValue(Store.class);
                    if (store != null) {
                        tvStoreName.setText(store.getStoreName());
                        tvStoreAddress.setText(store.getAddress());
                        tvStorePhone.setText(store.getPhone());
                    }
                });
    }

    private void listenForInventoryChanges(String storeId) {
        database.getReference("inventory").child(storeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<InventoryItem> items = new ArrayList<>();
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                            if (item != null) items.add(item);
                        }
                        loadBooksForInventory(items);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadBooksForInventory(List<InventoryItem> items) {
        database.getReference("books").get().addOnSuccessListener(snapshot -> {
            Map<String, Book> booksMap = new HashMap<>();
            for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                Book book = bookSnapshot.getValue(Book.class);
                if (book != null) booksMap.put(book.getIsbn(), book);
            }
            adapter.setData(items, booksMap, currentStoreId);
        });
    }
}