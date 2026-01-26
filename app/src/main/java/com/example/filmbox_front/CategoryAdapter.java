package com.example.filmbox_front;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private final List<Category> items = new ArrayList<>();

    public void setItems(List<Category> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = items.get(position);
        System.out.println("IMG URL = " + c.image_url);

        h.name.setText(c.title);

        Glide.with(h.itemView.getContext())
                .load(c.image_url)
                .centerCrop()
                .into(h.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgCategory);
            name = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
