package com.example.bookabook.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookabook.R;
import com.example.bookabook.models.Book;
import com.example.bookabook.models.InventoryItem;
import com.example.bookabook.models.enums.InventoryStatus;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> items = new ArrayList<>();
    private Map<String, Book> booksMap = new HashMap<>();
    private String storeId;

    public void setData(List<InventoryItem> items, Map<String, Book> booksMap, String storeId) {
        this.items = items;
        this.booksMap = booksMap;
        this.storeId = storeId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        Book book = booksMap.get(item.getIsbn());

        holder.tvBookTitle.setText(book != null ? book.getTitle() : "Unknown Title");
        holder.tvIsbn.setText("ISBN: " + item.getIsbn());

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


        // Remove listeners before setting initial values
        holder.swAvailable.setOnCheckedChangeListener(null);
        holder.etStockCount.removeTextChangedListener(holder.textWatcher);
        holder.etPrice.removeTextChangedListener(holder.textWatcher);

        holder.swAvailable.setChecked(item.getStatus() == InventoryStatus.IN_STOCK);
        holder.etStockCount.setText(String.valueOf(item.getStockCount()));
        holder.etPrice.setText(String.valueOf(item.getStorePrice()));
        holder.btnUpdate.setVisibility(View.GONE);

        // Add listeners back
        holder.swAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> holder.btnUpdate.setVisibility(View.VISIBLE));
        holder.etStockCount.addTextChangedListener(holder.textWatcher);
        holder.etPrice.addTextChangedListener(holder.textWatcher);

        holder.btnUpdate.setOnClickListener(v -> {
            int newStock = 0;
            double newPrice = 0.0;
            try {
                newStock = Integer.parseInt(holder.etStockCount.getText().toString());
                newPrice = Double.parseDouble(holder.etPrice.getText().toString());
            } catch (NumberFormatException ignored) {}

            InventoryStatus newStatus = holder.swAvailable.isChecked() ? InventoryStatus.IN_STOCK : InventoryStatus.OUT_OF_STOCK;

            // Update local object
            item.setStockCount(newStock);
            item.setStorePrice(newPrice);
            item.setStatus(newStatus);

            // Update Firebase
            FirebaseDatabase.getInstance().getReference("inventory")
                    .child(storeId)
                    .child(item.getIsbn())
                    .setValue(item)
                    .addOnSuccessListener(aVoid -> holder.btnUpdate.setVisibility(View.GONE));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvIsbn;
        ImageView ivBookCover;
        SwitchMaterial swAvailable;
        EditText etStockCount, etPrice;
        Button btnUpdate;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvIsbn = itemView.findViewById(R.id.tvIsbn);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            swAvailable = itemView.findViewById(R.id.swAvailable);
            etStockCount = itemView.findViewById(R.id.etStockCount);
            etPrice = itemView.findViewById(R.id.etPrice);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);

            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnUpdate.setVisibility(View.VISIBLE);
                }
                @Override
                public void afterTextChanged(Editable s) {}
            };
        }
    }
}