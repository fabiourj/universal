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

public class TumblrClient implements PhotoProvider {

    private String[] params;
    private PhotosCallback callback;
    private WeakReference<Context> contextReference;

    private int totalPosts;
    private int currentPage;
    
    private static final String perpage = "50";

    public TumblrClient(String[] params, Context context, PhotosCallback callback){
        this.params = params;
        this.callback = callback;
        this.contextReference = new WeakReference<>(context);
    }

    @Override
    public void requestPhotos(int page) {
        this.currentPage = page;
        new TumblrTask().execute();
    }

    private class TumblrTask extends AsyncTask<Void, Void, ArrayList<PhotoItem>> {

        @Override
        protected ArrayList<PhotoItem> doInBackground(Void... voids) {
            String username = params[0];
            String geturl = "https://api.tumblr.com/v2/blog/"+username+".tumblr.com/posts?api_key="
                    + contextReference.get().getString(R.string.tumblr_key) +"&type=photo&limit=" + perpage + "&offset=";
            
            geturl = geturl + (currentPage - 1) * Integer.parseInt(perpage);
            currentPage = currentPage + 1;

            String jsonString = Helper.getDataFromUrl(geturl);

            Log.v("INFO", "Tumblr JSON: " + jsonString);
            JSONObject json= null;
            // try parse the string to a JSON object
            try {
                json = new JSONObject(jsonString).getJSONObject("response");
            } catch (JSONException e) {
                Log.printStackTrace(e);
            }

            ArrayList<PhotoItem> images = null;

            try {
                // Checking for SUCCESS TAG
                totalPosts = json.getInt("total_posts");

                if (0 < totalPosts) {
                    // products found
                    // Getting Array of Products
                    JSONArray products;

                    products = json.getJSONArray("posts");
                    images = new ArrayList<PhotoItem>();

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("id");
                        String link = c.getString("post_url");
                        String summary = c.getString("summary");
                        JSONArray photos = c.has("photos") ? c.getJSONArray("photos") : null;

                        String url = null;
                        if (photos != null && photos.length() > 0)
                            url = ((JSONObject) photos.get(0)).getJSONObject("original_size").getString("url");

                        // adding items to arraylist
                        if (url != null){
                            PhotoItem item = new PhotoItem(id, link, url, summary);
                            images.add(item);
                        }
                    }
                } else {
                    Log.v("INFO", "No items found");
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
                boolean canLoadMore = currentPage * Integer.parseInt(perpage) < totalPosts;
                callback.completed(results, canLoadMore);
            } else {
                callback.failed();
            }

        }
    }
}
