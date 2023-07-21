package com.sherdle.universal.providers.rss;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sherdle.universal.R;
import com.sherdle.universal.providers.rss.ui.RssDetailActivity;
import com.sherdle.universal.providers.rss.ui.RssFragment;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.ViewModeUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class RssAdapter extends InfiniteRecyclerViewAdapter {

    private List<RSSItem> objects;
    private Context context;

    private static int COMPACT = 0;
    private static int NORMAL = 1;

    private ViewModeUtils viewModeUtils;

    public RssAdapter(Context context,
                       List<RSSItem> list) {
        super(context, null);
        this.context = context;
        this.objects = list;

        this.viewModeUtils = new ViewModeUtils(context, RssFragment.class);
    }

    @Override
    public int getViewType(int position) {
        if (viewModeUtils.getViewMode() == ViewModeUtils.NORMAL)
            return NORMAL;
        else
            return COMPACT;
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (COMPACT == viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_rss_row, parent, false);
            return new RssViewHolder((itemView));
        } else if (viewType == NORMAL) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_row, parent, false);
            return new RssLargeViewHolder(itemView);
        }
        return null;
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof RssViewHolder){
            RssViewHolder holder = (RssViewHolder) viewHolder;

            String html = objects.get(position).getRowDescription();

            holder.listTitle.setText(objects.get(position).getTitle());
            holder.listPubdate.setText(objects.get(position).getPubdate());
            holder.listDescription.setText(html);

            holder.listThumb.setImageDrawable(null);
            String thumburl = objects.get(position).getThumburl();

            loadImageIntoView(thumburl, holder.listThumb);
            setOnClickListener(holder.itemView, position);
        } else {
            RssLargeViewHolder itemHolder = (RssLargeViewHolder) viewHolder;

            itemHolder.headlineView.setText(objects.get(position).getTitle());
            itemHolder.reportedDateView.setText(objects.get(position).getPubdate());

            itemHolder.imageView.setImageBitmap(null);
            String thumburl = objects.get(position).getThumburl();

            loadImageIntoView(thumburl, itemHolder.imageView);
            setOnClickListener(itemHolder.itemView, position);

        }
    }

    private void setOnClickListener(View view, final int position){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,
                        RssDetailActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtra(RssDetailActivity.EXTRA_RSSITEM, objects.get(position));

                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    private void loadImageIntoView(String thumburl, final ImageView listThumb){
        if (thumburl != null && !thumburl.equals("")) {
            //setting the image
            final ImageView imageView = listThumb; // The view Picasso is loading an image into
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    /* Save the bitmap or do something with it here */

                    if (10 > bitmap.getWidth() || 10 > bitmap.getHeight()) {
                        // handle scaling
                        listThumb.setVisibility(View.GONE);
                    } else {
                        listThumb.setVisibility(View.VISIBLE);
                        listThumb.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            imageView.setTag(target);

            Picasso.get()
                    .load(thumburl)
                    .into(target);
        } else {
            listThumb.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class RssViewHolder extends RecyclerView.ViewHolder {
        TextView listTitle;
        TextView listPubdate;
        TextView listDescription;
        ImageView listThumb;

        RssViewHolder(View view){
            super(view);
            this.listTitle = view.findViewById(R.id.listtitle);
            this.listPubdate = view.findViewById(R.id.listpubdate);
            this.listDescription = view.findViewById(R.id.shortdescription);
            this.listThumb = view.findViewById(R.id.listthumb);
        }
    }

    private static class RssLargeViewHolder extends RecyclerView.ViewHolder {
        TextView headlineView;
        TextView reportedDateView;
        ImageView imageView;

        RssLargeViewHolder(View view){
            super(view);

            this.headlineView = view.findViewById(R.id.title);
            this.reportedDateView = view.findViewById(R.id.date);
            this.imageView = view.findViewById(R.id.thumbImage);

        }
    }
}
