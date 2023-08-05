package com.arena.esportes.providers.photos.api;

import com.arena.esportes.providers.photos.PhotoItem;

import java.util.ArrayList;

public interface PhotosCallback {

    void completed(ArrayList<PhotoItem> photos, boolean canLoadMore);
    void failed();
}
