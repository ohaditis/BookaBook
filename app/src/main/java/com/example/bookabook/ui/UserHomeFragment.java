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
import com.example.bookabook.WishlistManager;
import com.google.android.material.button.MaterialButton;
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
    private WishlistManager wishlistManager;
    private MaterialButton btnWishlist;

    private final List<InventoryItem> allInventoryItems = new ArrayList<>();
    private final Map<String, Book> booksMap = new HashMap<>();
    private final Map<String, Store> storesMap = new HashMap<>();

    private boolean showWishlistOnly = false;

    public UserHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_home, container, false);

        database = FirebaseDatabase.getInstance();
        wishlistManager = new WishlistManager(requireContext());

        rvUserInventory = view.findViewById(R.id.rvUserInventory);
        rvUserInventory.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnWishlist = view.findViewById(R.id.btnWishlist);

        adapter = new UserInventoryAdapter();
        rvUserInventory.setAdapter(adapter);

        adapter.setOnWishlistClickListener((item, position) -> {
            wishlistManager.toggleWishlist(item.getStoreId(), item.getIsbn());
            sortAndRefreshList();
        });

        btnWishlist.setOnClickListener(v -> {
            showWishlistOnly = !showWishlistOnly;
            updateWishlistButtonUI();
            sortAndRefreshList();
        });

        return view;
    }

    private void updateWishlistButtonUI() {
        if (showWishlistOnly) {
            btnWishlist.setText(R.string.wishlist_show_all);
            btnWishlist.setIconResource(android.R.drawable.ic_menu_agenda);
        } else {
            btnWishlist.setText(R.string.wishlist_filter);
            btnWishlist.setIconResource(android.R.drawable.btn_star_big_on);
        }
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
                sortAndRefreshList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load inventory", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sortAndRefreshList() {
        List<InventoryItem> displayList = new ArrayList<>();
        
        for (InventoryItem item : allInventoryItems) {
            boolean isWishlisted = wishlistManager.isWishlisted(item.getStoreId(), item.getIsbn());
            if (!showWishlistOnly || isWishlisted) {
                displayList.add(item);
            }
        }

        Collections.sort(displayList, (o1, o2) -> {
            boolean isO1Wishlisted = wishlistManager.isWishlisted(o1.getStoreId(), o1.getIsbn());
            boolean isO2Wishlisted = wishlistManager.isWishlisted(o2.getStoreId(), o2.getIsbn());

            if (isO1Wishlisted && !isO2Wishlisted) return -1;
            if (!isO1Wishlisted && isO2Wishlisted) return 1;

            Book b1 = booksMap.get(o1.getIsbn());
            Book b2 = booksMap.get(o2.getIsbn());

            String name1 = (b1 != null && b1.getTitle() != null) ? b1.getTitle() : "";
            String name2 = (b2 != null && b2.getTitle() != null) ? b2.getTitle() : "";

            return name1.compareToIgnoreCase(name2);
        });

        adapter.setData(displayList, booksMap, storesMap, requireContext());

        if (!displayList.isEmpty()) {
            rvUserInventory.scrollToPosition(0);
        }
    }
}