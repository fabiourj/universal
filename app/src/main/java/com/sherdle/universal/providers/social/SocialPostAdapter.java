package com.sherdle.universal.providers.social;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sherdle.universal.Config;
import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.attachmentviewer.model.MediaAttachment;
import com.sherdle.universal.attachmentviewer.ui.AttachmentActivity;
import com.sherdle.universal.attachmentviewer.ui.VideoPlayerActivity;
import com.sherdle.universal.comments.CommentsActivity;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.WebHelper;
import com.squareup.picasso.Picasso;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SocialPostAdapter extends InfiniteRecyclerViewAdapter{

	private Context context;
    private List<SocialPost> objects;

    public SocialPostAdapter(Context context, List<SocialPost> objects, InfiniteRecyclerViewAdapter.LoadMoreListener listener) {
        super(context, listener);
    	this.context = context;
        this.objects = objects;
    }

    @Override
    protected int getViewType(int position) {
        return 0;
    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_social_row, parent, false);
        return new InstagramPhotoViewHolder(itemView);
    }

    @Override
    protected void doBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //TODO also merge fragments, and use the provider pattern.

        if (holder instanceof InstagramPhotoViewHolder){
            final SocialPost photo = objects.get(position);
            InstagramPhotoViewHolder viewHolder = (InstagramPhotoViewHolder) holder;

            viewHolder.profileImg.setImageDrawable(null);
            Picasso.get().load(photo.profilePhotoUrl).into(viewHolder.profileImg);

            String username  = photo.username.substring(0,1).toUpperCase(Locale.getDefault()) +
                    photo.username.substring(1).toLowerCase(Locale.getDefault());
            viewHolder.userNameView.setText(username);

            viewHolder.dateView.setText(
                    DateUtils.getRelativeDateTimeString(context,photo.createdTime.getTime(),
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL));

            viewHolder.inlineImg.setImageDrawable(null);
            if (photo.imageUrls.size() > 0)
                Picasso.get().load(photo.imageUrls.get(0)).placeholder(R.drawable.placeholder).into(viewHolder.inlineImg);

            if (photo.postType == SocialPost.PostType.VIDEO){
                viewHolder.inlineImgBtn.show();
                viewHolder.inlineImgBtn.setImageResource(R.drawable.ic_action_play);
            } else if (photo.imageUrls.size() > 1) {
                viewHolder.inlineImgBtn.show();
                viewHolder.inlineImgBtn.setImageResource(R.drawable.ic_view_carousel);
            } else {
                viewHolder.inlineImgBtn.hide();
            }

            if (photo.postType == SocialPost.PostType.IMAGE){
                View.OnClickListener imageListener = new View.OnClickListener() {
                    public void onClick(View arg0) {
                        ArrayList<MediaAttachment> mediaAttachments = new ArrayList<>();
                        for (String attachment : photo.imageUrls) {
                            mediaAttachments.add(MediaAttachment.withImage(attachment));
                        }
                        AttachmentActivity.startActivity(context, mediaAttachments);
                    }
                };
                viewHolder.inlineImgBtn.setOnClickListener(imageListener);
                viewHolder.inlineImg.setOnClickListener(imageListener);
            }
            else if (photo.postType == SocialPost.PostType.VIDEO) {
                View.OnClickListener videoListener = new View.OnClickListener() {
                    public void onClick(View arg0) {
                        VideoPlayerActivity.startActivity(context, photo.videoUrl);
                    }
                };

                viewHolder.inlineImgBtn.setOnClickListener(videoListener);
                viewHolder.inlineImg.setOnClickListener(videoListener);
            } else {
                viewHolder.inlineImg.setOnClickListener(null);
            }

            viewHolder.likesCountView.setText(Helper.formatValue(photo.likesCount));

            if (photo.caption != null && !StringUtil.isBlank(photo.caption)){
                viewHolder.descriptionView.setText(photo.caption);
                viewHolder.descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, WebHelper.getTextViewFontSize(context));
                viewHolder.descriptionView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.descriptionView.setVisibility(View.GONE);
            }

            viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    // this is the text that will be shared
                    sendIntent.putExtra(Intent.EXTRA_TEXT,photo.link);

                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, context.getResources()
                            .getString(R.string.share_header)));
                }
            });

            viewHolder.openBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    HolderActivity.startWebViewActivity(context, photo.link, Config.OPEN_EXPLICIT_EXTERNAL, false, null);

                }
            });

            // Set comments
            viewHolder.commentsCountView.setText(Helper.formatValue(photo.commentsOrRetweetCount));

            if (photo.postSource != SocialPost.PostSource.Twitter) {
                viewHolder.commentsBtn.setImageResource(R.drawable.ic_comment);
                viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        // Start NewActivity.class
                        Intent commentIntent = new Intent(context, CommentsActivity.class);
                        commentIntent.putExtra(CommentsActivity.DATA_TYPE,
                                photo.postSource == SocialPost.PostSource.Instagram ?
                                        CommentsActivity.INSTAGRAM : CommentsActivity.FACEBOOK);
                        commentIntent.putExtra(CommentsActivity.DATA_PARSEABLE, photo.commentsJson);
                        commentIntent.putExtra(CommentsActivity.DATA_ID, photo.id);
                        context.startActivity(commentIntent);
                    }
                });
            } else {
                viewHolder.commentsBtn.setImageResource(R.drawable.ic_action_retweet);
            }
        }
    }

    @Override
    protected int getCount() {
        return objects.size();
    }

    private class InstagramPhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImg;
        ImageView inlineImg;
        FloatingActionButton inlineImgBtn;

        TextView userNameView;
        TextView dateView;
        TextView likesCountView;
        TextView commentsCountView;
        TextView descriptionView;

        ImageView shareBtn;
        ImageView openBtn;
        ImageView commentsBtn;

        InstagramPhotoViewHolder(View view){
            super(view);

            this.profileImg = view.findViewById(R.id.profile_image);
            this.userNameView = view.findViewById(R.id.name);
            this.dateView = view.findViewById(R.id.date);
            this.inlineImg = view.findViewById(R.id.photo);
            this.inlineImgBtn = view.findViewById(R.id.playbutton);
            this.likesCountView = view.findViewById(R.id.like_count);
            this.descriptionView = view.findViewById(R.id.message);
            this.descriptionView = view.findViewById(R.id.message);
            this.shareBtn = view.findViewById(R.id.share);
            this.openBtn = view.findViewById(R.id.open);
            this.commentsBtn = view.findViewById(R.id.comments);
            this.commentsCountView = view.findViewById(R.id.comments_count);

        }
    }
}
