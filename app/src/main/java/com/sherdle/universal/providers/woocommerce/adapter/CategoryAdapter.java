package com.sherdle.universal.providers.woocommerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sherdle.universal.R;
import com.sherdle.universal.providers.woocommerce.model.products.Category;
import com.sherdle.universal.providers.woocommerce.model.products.Image;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends InfiniteRecyclerViewAdapter {
    private final Context mContext;
    private final List<Category> categoryList;
    private View headerView;

    private float itemWidth;

    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_HEADER = 1;

    private final OnCategoryClick callback;

    private class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView categoryCount, categoryName;
        ImageView categoryImage;

        View view;

        OrderViewHolder(View view) {
            super(view);

            this.view = view;
            categoryName = view.findViewById(R.id.title);
            categoryCount = view.findViewById(R.id.subTitle);
            categoryImage = view.findViewById(R.id.image);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View view) {
            super(view);
        }
    }

    public CategoryAdapter(Context mContext, List<Category> categoryList, LoadMoreListener listener, OnCategoryClick callback) {
        super(mContext, listener);
        this.mContext = mContext;
        this.categoryList = categoryList;
        this.callback = callback;
    }

    @Override
    protected int getViewType(int position) {
        if (headerView != null && position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_wc_category, parent, false);
            return new OrderViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            RecyclerView.ViewHolder holder = new HeaderViewHolder(headerView);
            requestFullSpan(holder);
            return holder;
        }
        return null;
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.onCategorySelected(categoryList.get(viewHolder.getAdapterPosition()));
            }
        });

        if (viewHolder instanceof OrderViewHolder) {
            OrderViewHolder holder = (OrderViewHolder) viewHolder;

            final Category category = categoryList.get(position - ((headerView == null) ? 0 : 1));

            final String description = String.format(mContext.getString(R.string.category_size), category.getCount());
            Image image = category.getImage();

            holder.categoryName.setText(category.getName());
            Picasso.get().load(image.getSrc()).into(holder.categoryImage);
            holder.categoryCount.setText(description);

            if (itemWidth > 0) {
                holder.view.getLayoutParams().width = (int) itemWidth;
            }
        } else if (viewHolder instanceof HeaderViewHolder) {
            //Layout is already loaded
            requestFullSpan(viewHolder);
        }
    }

    public void setItemWidth(float width) {
        this.itemWidth = width;
    }

    /**
     * Ensures that HeaderViewHolder still receives a full span, without refreshing the list
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof HeaderViewHolder)
            requestFullSpan(holder);
    }

    @Override
    public int getCount() {
        return categoryList.size() + ((headerView == null) ? 0 : 1);
    }

    public void setHeader(View headerView) {
        this.headerView = headerView;
        notifyDataSetChanged();
    }

    public interface OnCategoryClick{
        void onCategorySelected(Category category);
    }

}

