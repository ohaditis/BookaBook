package com.example.bookabook;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.bookabook.models.InventoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Set;

public class AlarmReceiver extends BroadcastReceiver { // .//
    private static final String PREFS_NAME = "bookabook_prefs";
    private static final String KEY_LAST_CHECK = "last_db_check_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            pendingResult.finish();
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCheckTime = prefs.getLong(KEY_LAST_CHECK, 0);
        long currentTime = System.currentTimeMillis();

        Set<String> wishlistKeys = prefs.getStringSet("user_wishlist", null);

        if (wishlistKeys == null || wishlistKeys.isEmpty()) {
            pendingResult.finish();
            return;
        }

        FirebaseDatabase.getInstance().getReference("inventory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean foundUpdate = false;
                int updatedCount = 0;

                for (String key : wishlistKeys) {
                    // key format: storeId_isbn
                    String[] parts = key.split("_");
                    if (parts.length == 2) {
                        String storeId = parts[0];
                        String isbn = parts[1];

                        DataSnapshot itemSnap = snapshot.child(storeId).child(isbn);
                        if (itemSnap.exists()) {
                            InventoryItem item = itemSnap.getValue(InventoryItem.class);
                            if (item != null && item.getLastUpdated() > lastCheckTime) {
                                foundUpdate = true;
                                updatedCount++;
                            }
                        }
                    }
                }

                if (foundUpdate) {
                    sendNotification(context, updatedCount);
                }

                prefs.edit().putLong(KEY_LAST_CHECK, currentTime).apply();
                pendingResult.finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pendingResult.finish();
            }
        });
    }

    private void sendNotification(Context context, int count) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "db_updates_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId, "Book Updates", NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);

        String message = count == 1 ? "One item in your wishlist has been updated!" : count + " items in your wishlist have been updated!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Wishlist Update")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(2, builder.build());
    }
}
