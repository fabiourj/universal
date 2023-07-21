package com.sherdle.universal.providers.woocommerce.model.products;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Septian A. Fujianto on 10/31/2016.
 */

public class Review implements Serializable {

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("review")
    @Expose
    private String review;
    @SerializedName("reviewer")
    @Expose
    private String reviewer;
    @SerializedName("rating")
    @Expose
    private int rating;
    @SerializedName("date_created")
    @Expose
    private String dateCreated;
    @SerializedName("reviewer_avatar_urls")
    @Expose
    private ReviewAvatars avatars;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ReviewAvatars getAvatars() {
        return avatars;
    }

    public void setAvatars(ReviewAvatars avatars) {
        this.avatars = avatars;
    }
}
