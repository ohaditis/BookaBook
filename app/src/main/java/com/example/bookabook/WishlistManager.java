package com.example.bookabook;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class WishlistManager {
    private static final String PREFS_NAME = "bookabook_prefs";
    private static final String KEY_WISHLIST = "user_wishlist";
    private final SharedPreferences prefs;

    public WishlistManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String generateKey(String storeId, String isbn) {
        return storeId + "_" + isbn;
    }

    // בדיקה האם פריט נמצא ב-Wishlist
    public boolean isWishlisted(String storeId, String isbn) {
        Set<String> wishlist = prefs.getStringSet(KEY_WISHLIST, new HashSet<>());
        return wishlist.contains(generateKey(storeId, isbn));
    }

    // הוספה או הסרה מה-Wishlist
    public void toggleWishlist(String storeId, String isbn) {
        Set<String> wishlist = new HashSet<>(prefs.getStringSet(KEY_WISHLIST, new HashSet<>()));
        String itemKey = generateKey(storeId, isbn);

        if (wishlist.contains(itemKey)) {
            wishlist.remove(itemKey);
        } else {
            wishlist.add(itemKey);
        }
        
        prefs.edit().putStringSet(KEY_WISHLIST, wishlist).apply();
    }
}
