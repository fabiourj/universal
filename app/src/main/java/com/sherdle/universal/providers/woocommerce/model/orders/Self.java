package com.sherdle.universal.providers.woocommerce.model.orders;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Septian A. Fujianto on 11/14/2016.
 */

public class Self {

    @SerializedName("href")
    @Expose
    private String href;

    /**
     * @return The href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href The href
     */
    public void setHref(String href) {
        this.href = href;
    }
}