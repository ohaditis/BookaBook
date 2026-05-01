package com.example.bookabook.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookabook.R;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserHomeActivity extends AppCompatActivity {

    private RecyclerView rvUserInventory;
    private UserInventoryAdapter adapter;
    private FirebaseDatabase database;

    private List<InventoryItem> allInventoryItems = new ArrayList<>();
    private Map<String, Book> booksMap = new HashMap<>();
    private Map<String, Store> storesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        database = FirebaseDatabase.getInstance();
        rvUserInventory = findViewById(R.id.rvUserInventory);
        rvUserInventory.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new UserInventoryAdapter();
        rvUserInventory.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        // First load all stores and books, then load inventory
        database.getReference("stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storesMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Store store = ds.getValue(Store.class);
                    if (store != null) storesMap.put(store.getStoreId(), store);
                }
                loadBooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadBooks() {
        database.getReference("books").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book book = ds.getValue(Book.class);
                    if (book != null) booksMap.put(book.getIsbn(), book);
                }
                loadInventory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadInventory() {
        database.getReference("inventory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allInventoryItems.clear();
                // Inventory is stored as: inventory -> storeId -> isbn -> InventoryItem
                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : storeSnapshot.getChildren()) {
                        InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                        if (item != null) {
                            allInventoryItems.add(item);
                        }
                    }
                }

                // Sort by book name
                Collections.sort(allInventoryItems, (o1, o2) -> {
                    Book b1 = booksMap.get(o1.getIsbn());
                    Book b2 = booksMap.get(o2.getIsbn());
                    String name1 = (b1 != null && b1.getTitle() != null) ? b1.getTitle() : "";
                    String name2 = (b2 != null && b2.getTitle() != null) ? b2.getTitle() : "";
                    return name1.compareToIgnoreCase(name2);
                });

                adapter.setData(allInventoryItems, booksMap, storesMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserHomeActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
