package com.example.bookabook.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserHomeFragment extends Fragment {

    private RecyclerView rvUserInventory;
    private UserInventoryAdapter adapter;
    private FirebaseDatabase database;

    private final List<InventoryItem> allInventoryItems = new ArrayList<>();
    private final Map<String, Book> booksMap = new HashMap<>();
    private final Map<String, Store> storesMap = new HashMap<>();

    public UserHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_home, container, false);

        database = FirebaseDatabase.getInstance();

        rvUserInventory = view.findViewById(R.id.rvUserInventory);
        rvUserInventory.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserInventoryAdapter();
        rvUserInventory.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    private void loadData() {
        database.getReference("stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storesMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Store store = ds.getValue(Store.class);
                    if (store != null) {
                        storesMap.put(store.getStoreId(), store);
                    }
                }
                loadBooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadBooks() {
        database.getReference("books").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booksMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book book = ds.getValue(Book.class);
                    if (book != null) {
                        booksMap.put(book.getIsbn(), book);
                    }
                }
                loadInventory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadInventory() {
        database.getReference("inventory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allInventoryItems.clear();

                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : storeSnapshot.getChildren()) {
                        InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                        if (item != null) {
                            allInventoryItems.add(item);
                        }
                    }
                }

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
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load inventory", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}