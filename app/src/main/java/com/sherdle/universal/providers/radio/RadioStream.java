package com.sherdle.universal.providers.radio;

import android.graphics.Bitmap;

public class RadioStream {
    private String url;
    private String logoUrl;
    private Bitmap logoBitmap;

    public RadioStream(String url, String[] arguments){
        this.url = url;
        this.logoUrl = (arguments.length > 1) ? arguments[1] : null;
        this.logoBitmap = null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Bitmap getLogoBitmap() {
        return logoBitmap;
    }

    public void setLogoBitmap(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;
    }


}