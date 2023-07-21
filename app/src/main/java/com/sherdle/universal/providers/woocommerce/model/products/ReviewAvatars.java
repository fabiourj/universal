package com.sherdle.universal.providers.woocommerce.model.products;

import android.text.Html;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Septian A. Fujianto on 10/31/2016.
 */

public class ReviewAvatars implements Serializable {

    @SerializedName("24")
    @Expose
    private String low;
    @SerializedName("48")
    @Expose
    private String medium;
    @SerializedName("96")
    @Expose
    private String high;

    public String getHighestQuality(){
        if (high != null) return high;
        if (medium != null) return medium;
        if (low != null) return low;
        return null;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }
}
