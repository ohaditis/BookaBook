package com.example.bookabook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class StoreHomeFragment extends Fragment {

    private TextView tvStoreName;
    private TextView tvStoreAddress;
    private TextView tvStorePhone;
    private RecyclerView rvInventory;
    private FloatingActionButton fabAddBook;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private InventoryAdapter adapter;
    private String currentStoreId;

    public StoreHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_home, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvStoreAddress = view.findViewById(R.id.tvStoreAddress);
        tvStorePhone = view.findViewById(R.id.tvStorePhone);
        rvInventory = view.findViewById(R.id.rvInventory);
        fabAddBook = view.findViewById(R.id.fabAddBook);

        rvInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InventoryAdapter();
        rvInventory.setAdapter(adapter);

        fabAddBook.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddInventoryItemActivity.class)));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStoreData();
    }

    private void loadStoreData() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        database.getReference("storeUsers")
                .child(uid)
                .get()
                .addOnSuccessListener(storeUserSnapshot -> {
                    if (isAdded()) {
                        StoreUser storeUser = storeUserSnapshot.getValue(StoreUser.class);
                        if (storeUser != null && storeUser.getStoreId() != null) {
                            currentStoreId = storeUser.getStoreId();
                            loadStoreById(currentStoreId);
                            listenForInventoryChanges(currentStoreId);
                        }
                    }
                });
    }

    private void loadStoreById(String storeId) {
        database.getReference("stores").child(storeId).get()
                .addOnSuccessListener(snapshot -> {
                    if (isAdded()) {
                        Store store = snapshot.getValue(Store.class);
                        if (store != null) {
                            tvStoreName.setText(store.getStoreName());
                            tvStoreAddress.setText(store.getAddress());
                            tvStorePhone.setText(store.getPhone());
                        }
                    }
                });
    }

    private void listenForInventoryChanges(String storeId) {
        database.getReference("inventory").child(storeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isAdded()) {
                            List<InventoryItem> items = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                                if (item != null) items.add(item);
                            }
                            loadBooksForInventory(items);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadBooksForInventory(List<InventoryItem> items) {
        database.getReference("books").get().addOnSuccessListener(snapshot -> {
            if (isAdded()) {
                Map<String, Book> booksMap = new HashMap<>();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) booksMap.put(book.getIsbn(), book);
                }
                adapter.setData(items, booksMap, currentStoreId);
            }
        });
    }
}