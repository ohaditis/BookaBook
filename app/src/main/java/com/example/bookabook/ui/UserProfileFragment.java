package com.example.bookabook.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookabook.AlarmReceiver;
import com.example.bookabook.R;
import com.example.bookabook.WishlistManager;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.Store;
import com.example.bookabook.models.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileFragment extends Fragment {

    private TextInputEditText etDisplayName, etEmail;
    private Button btnUpdateProfile, btnLogout;
    private MaterialButton btnRefreshWishlist;
    private RecyclerView rvWishlist;
    private UserInventoryAdapter wishlistAdapter;
    private WishlistManager wishlistManager;
    private RadioGroup rgNotificationTime;
    private RadioButton rbNotifyNone, rbNotify8, rbNotify20;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String uid;

    private final Map<String, Book> booksMap = new HashMap<>();
    private final Map<String, Store> storesMap = new HashMap<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Notifications permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    public UserProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getUid();
        wishlistManager = new WishlistManager(requireContext());

        etDisplayName = view.findViewById(R.id.etDisplayName);
        etEmail = view.findViewById(R.id.etEmail);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnRefreshWishlist = view.findViewById(R.id.btnRefreshWishlist);
        rvWishlist = view.findViewById(R.id.rvWishlist);
        rgNotificationTime = view.findViewById(R.id.rgNotificationTime);
        rbNotifyNone = view.findViewById(R.id.rbNotifyNone);
        rbNotify8 = view.findViewById(R.id.rbNotify8);
        rbNotify20 = view.findViewById(R.id.rbNotify20);

        rvWishlist.setLayoutManager(new LinearLayoutManager(requireContext()));
        wishlistAdapter = new UserInventoryAdapter();
        rvWishlist.setAdapter(wishlistAdapter);

        wishlistAdapter.setOnWishlistClickListener((item, position) -> {
            wishlistManager.toggleWishlist(item.getStoreId(), item.getIsbn());
            loadWishlistData(); // Refresh list after toggle
        });

        loadUserData();
        loadWishlistData();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnRefreshWishlist.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.action_refresh, Toast.LENGTH_SHORT).show();
            loadWishlistData();
        });

        return view;
    }

    private void loadUserData() {
        if (uid == null) return;

        database.getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        etDisplayName.setText(profile.getDisplayName());
                        etEmail.setText(profile.getEmail());
                        setNotificationUI(profile.getNotificationTime());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setNotificationUI(String time) {
        if (time == null || time.equals("None")) {
            rbNotifyNone.setChecked(true);
        } else if (time.equals("08:00")) {
            rbNotify8.setChecked(true);
        } else if (time.equals("20:00")) {
            rbNotify20.setChecked(true);
        }
    }

    private void loadWishlistData() {
        database.getReference("stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storesMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Store store = ds.getValue(Store.class);
                    if (store != null) storesMap.put(store.getStoreId(), store);
                }
                loadBooksAndInventory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadBooksAndInventory() {
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
        database.getReference("inventory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                List<InventoryItem> wishlistedItems = new ArrayList<>();
                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : storeSnapshot.getChildren()) {
                        InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                        if (item != null && wishlistManager.isWishlisted(item.getStoreId(), item.getIsbn())) {
                            wishlistedItems.add(item);
                        }
                    }
                }
                wishlistAdapter.setData(wishlistedItems, booksMap, storesMap, requireContext());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfile() {
        String displayName = etDisplayName.getText().toString().trim();
        String notificationTime = "None";
        int checkedId = rgNotificationTime.getCheckedRadioButtonId();
        if (checkedId == R.id.rbNotify8) notificationTime = "08:00";
        else if (checkedId == R.id.rbNotify20) notificationTime = "20:00";

        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Display name required");
            return;
        }

        if (!notificationTime.equals("None") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("notificationTime", notificationTime);

        String finalNotificationTime = notificationTime;
        database.getReference("users").child(uid).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    scheduleAlarm(finalNotificationTime);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void scheduleAlarm(String time) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (time.equals("None")) {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            return;
        }

        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}