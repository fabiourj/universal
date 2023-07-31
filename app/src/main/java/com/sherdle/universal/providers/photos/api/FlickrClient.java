package com.sherdle.universal.providers.photos.api;

import android.content.Context;
import android.os.AsyncTask;

import com.sherdle.universal.R;
import com.sherdle.universal.providers.photos.PhotoItem;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FlickrClient implements PhotoProvider {

    private String[] params;
    private PhotosCallback callback;
    private WeakReference<Context> contextReference;

    private int totalPages;
    private int currentPage;

    public FlickrClient(String[] params, Context context, PhotosCallback callback){
        this.params = params;
        this.callback = callback;
        this.contextReference = new WeakReference<>(context);
    }

    @Override
    public void requestPhotos(int page) {
        this.currentPage = page;
        new FlickrTask().execute();
    }

    private class FlickrTask extends AsyncTask<Void, Void, ArrayList<PhotoItem>> {

        @Override
        protected ArrayList<PhotoItem> doInBackground(Void... voids) {
            String apiKey = contextReference.get().getString(R.string.flickr_key);
            String method = params[1];
            String galleryId = params[0];

            String pathMethod = !method.equals("gallery") ? "photosets" : "galleries";
            String idMethod = !method.equals("gallery") ? "photoset_id" : "gallery_id";

            String geturl = "https://api.flickr.com/services/rest/?method=flickr." + pathMethod +
                    ".getPhotos&api_key=" + apiKey +
                    "&" + idMethod + "=" +
                    galleryId + "&format=json" +
                    "&extras=path_alias,url_o,url_c,url_b,url_z" +
                    "&per_page=50&page=";

            geturl = geturl + currentPage;

            String jsonString = Helper.getDataFromUrl(geturl);

            Log.v("INFO", "Tumblr JSON: " + jsonString);
            if (jsonString.isEmpty()) return null;
            JSONObject json = null;
            // try parse the string to a JSON object
            try {
                jsonString = jsonString.replace("jsonFlickrApi(", "");
                jsonString = jsonString.substring(0, jsonString.length() - 1);
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                Log.printStackTrace(e);
            }

            ArrayList<PhotoItem> images = null;

            try {
                // Checking for SUCCESS TAG
                String parentMethod = !method.equals("gallery") ? "photoset" : "photos";
                totalPages = json.getJSONObject(parentMethod).getInt("pages");

                // products found
                // Getting Array of Products
                JSONArray products;

                products = json.getJSONObject(parentMethod).getJSONArray("photo");
                images = new ArrayList<PhotoItem>();

                // looping through All Products
                for (int i = 0; i < products.length(); i++) {
                    JSONObject c = products.getJSONObject(i);

                    // Storing each json item in variable
                    String id = c.getString("id");
                    String title = c.getString("title");
                    String link = "https://www.flickr.com/photos/" + c.getString("pathalias") + "/" + id;

                    String url = null;
                    if (c.has("url_o"))
                        url = c.getString("url_o");
                    else if (c.has("url_b"))
                        url = c.getString("url_b");
                    else if (c.has("url_c"))
                        url = c.getString("url_c");


                    String thumbUrl = null;
                    if (c.has("url_z"))
                        thumbUrl = c.getString("url_z");
                    else
                        thumbUrl = url;

                    // adding items to arraylist
                    if (url != null) {
                        PhotoItem item = new PhotoItem(id, link, url, title, thumbUrl);
                        images.add(item);
                    }
                }

            } catch (JSONException e) {
                Log.printStackTrace(e);
            } catch (NullPointerException e) {
                Log.printStackTrace(e);
            }

            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoItem> results) {
            if (results != null) {
                boolean canLoadMore = currentPage < totalPages;
                callback.completed(results, canLoadMore);
            } else {
                callback.failed();
            }

        }
    }
}
