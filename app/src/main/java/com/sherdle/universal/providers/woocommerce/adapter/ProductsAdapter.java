package com.sherdle.universal.providers.woocommerce.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sherdle.universal.R;
import com.sherdle.universal.providers.woocommerce.checkout.CartAssistant;
import com.sherdle.universal.providers.woocommerce.checkout.PriceFormat;
import com.sherdle.universal.providers.woocommerce.model.ViewItem;
import com.sherdle.universal.providers.woocommerce.model.products.Product;
import com.sherdle.universal.providers.woocommerce.ui.ProductActivity;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends InfiniteRecyclerViewAdapter {
    private final Activity mContext;
    private final List<Product> productList;
    private final List<ViewItem> headersList;
    private float itemWidth;

    private final static int TYPE_PRODUCT = 0;
    private final static int TYPE_CUSTOM= 1;

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productPriceRegular, saleLabel;
        ImageView productImage;
        ImageView overflow;

        View view;

        ProductViewHolder(View view) {
            super(view);

            this.view = view;
            productName = view.findViewById(R.id.productName);
            productPrice = view.findViewById(R.id.productPrice);
            productPriceRegular = view.findViewById(R.id.productPriceRegular);
            productImage = view.findViewById(R.id.productImage);
            overflow = view.findViewById(R.id.overflow);
            saleLabel = view.findViewById(R.id.sale_label);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;

        HeaderViewHolder(View view) {
            super(view);
            this.layout = (LinearLayout) view;
        }
    }

    public ProductsAdapter(Activity mContext, List<Product> productsList, LoadMoreListener listener) {
        super(mContext, listener);
        this.mContext = mContext;
        this.productList = productsList;
        this.headersList = new ArrayList<>();
    }

    @Override
    protected int getViewType(int position) {
        if (position < headersList.size())
            return TYPE_CUSTOM;
        else
            return TYPE_PRODUCT;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PRODUCT) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_wc_product_card, parent, false);
            return new ProductViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_wc_header, parent, false);
            RecyclerView.ViewHolder holder =
                    new HeaderViewHolder(itemView);
            requestFullSpan(holder);
            return holder;
        }
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProductViewHolder) {
            ProductViewHolder holder = (ProductViewHolder) viewHolder;

            final Product product = productList.get(position - headersList.size());

            final String name = product.getName();
            final String image = product.getImages().size() > 0 ? product.getImages().get(0).getSrc() : "";

            holder.productName.setText(name);

            if (product.getOnSale()) {
                holder.productPriceRegular.setVisibility(View.VISIBLE);
                holder.saleLabel.setVisibility(View.VISIBLE);
                holder.productPriceRegular.setText(PriceFormat.formatPrice(product.getRegularPrice()));
                holder.productPriceRegular.setPaintFlags(holder.productPriceRegular.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.productPrice.setText(PriceFormat.formatPrice(product.getSalePrice()));
            } else {
                holder.productPriceRegular.setVisibility(View.GONE);
                holder.saleLabel.setVisibility(View.GONE);
                holder.productPrice.setText(PriceFormat.formatPrice(product.getPrice()));
            }

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CartAssistant(mContext, view, product).addProductToCart(null);

                }
            });
            Picasso.get().load(image).into(holder.productImage);
            holder.productImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ProductActivity.class);
                    intent.putExtra(ProductActivity.PRODUCT, product);
                    mContext.startActivity(intent);
                }
            });

            if (itemWidth > 0) {
                holder.view.getLayoutParams().width = (int) itemWidth;
            }
        } else if (viewHolder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) viewHolder).layout.removeAllViews();

            if (headersList.get(position).view.getParent() != null) {
                ((ViewGroup) headersList.get(position).view.getParent()).removeView(headersList.get(position).view);
            }

            ((HeaderViewHolder) viewHolder).layout.addView(headersList.get(position).view);
            requestFullSpan(viewHolder);
        }
    }

    public void setItemWidth(float width){
        this.itemWidth = width;
    }

    /**
     * Ensures that HeaderViewHolder still receives a full span, without refreshing the list
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
        return headersList.size() + productList.size();
    }

    public void addHeaderToIndex(View view, int index) {
        this.headersList.add(index, new ViewItem(view));
        notifyDataSetChanged();
    }

    public void replaceHeaderAtIndex(View view, int index){
        this.headersList.set(index, new ViewItem(view));
        notifyDataSetChanged();
    }

    public void clearHeaders(){
        if (headersList.size() > 0) {
            this.headersList.clear();
            notifyDataSetChanged();
        }
    }

}
