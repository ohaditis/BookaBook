package com.example.bookabook.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookabook.R;
import com.example.bookabook.WishlistManager;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.Store;
import com.example.bookabook.models.enums.InventoryStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserInventoryAdapter extends RecyclerView.Adapter<UserInventoryAdapter.ViewHolder> {

    private List<InventoryItem> items = new ArrayList<>();
    private WishlistManager wishlistManager;
    private Map<String, Book> booksMap;
    private Map<String, Store> storesMap;
    private OnWishlistClickListener wishlistClickListener;

    public interface OnWishlistClickListener {
        void onWishlistClick(InventoryItem item, int position);
    }

    public void setOnWishlistClickListener(OnWishlistClickListener listener) {
        this.wishlistClickListener = listener;
    }

    public void setData(List<InventoryItem> items, Map<String, Book> booksMap, Map<String, Store> storesMap, Context context) {
        this.items = items;
        this.booksMap = booksMap;
        this.storesMap = storesMap;
        if (this.wishlistManager == null) {
            this.wishlistManager = new WishlistManager(context);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        Book book = booksMap != null ? booksMap.get(item.getIsbn()) : null;
        Store store = storesMap != null ? storesMap.get(item.getStoreId()) : null;

        if (book != null
                && book.getCoverImageUrl() != null
                && !book.getCoverImageUrl().trim().isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(book.getCoverImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.ivBookCover);

        } else {
            holder.ivBookCover.setImageResource(R.drawable.ic_launcher_background);
        }
        holder.tvBookTitle.setText(book != null ? book.getTitle() : "Unknown Book");
        holder.tvStoreName.setText(store != null ? store.getStoreName() : "Unknown Store");
        holder.tvPrice.setText("Price: " + (item.getStorePrice() != null ? item.getStorePrice() : 0.0) + "₪");
        
        boolean inStock = item.getStatus() == InventoryStatus.IN_STOCK;
        holder.tvStatus.setText(inStock ? "In Stock" : "Out of Stock");
        holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(
                inStock ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        
        holder.tvStockCount.setText("Units available: " + (item.getStockCount() != null ? item.getStockCount() : 0));

        // Wishlist logic
        if (wishlistManager != null) {
            boolean isItemWishlisted = wishlistManager.isWishlisted(item.getStoreId(), item.getIsbn());
            if (isItemWishlisted) {
                holder.ivWishlist.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.ivWishlist.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }

        holder.ivWishlist.setOnClickListener(v -> {
            if (wishlistClickListener != null) {
                wishlistClickListener.onWishlistClick(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvStoreName, tvPrice, tvStatus, tvStockCount;
        ImageView ivBookCover, ivWishlist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStockCount = itemView.findViewById(R.id.tvStockCount);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            ivWishlist = itemView.findViewById(R.id.ivWishlist);
        }
    }
}
