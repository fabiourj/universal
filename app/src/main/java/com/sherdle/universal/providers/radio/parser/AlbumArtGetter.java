package com.sherdle.universal.providers.radio.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.Log;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * This class is used to get Album Art of a song based on search query. Uses the spotify API.
 */
public class AlbumArtGetter {

    public static String CACHE = "CACHE";
    public String lastQuery = "";

    public static void getImageForQuery(final String query, final AlbumCallback callback, final Context context){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... unused) {

                Bitmap cache = getInstance().retrieveBitmapFromCache(query);
                if (cache != null) {
                    callback.finished(cache);
                    return CACHE;
                }

                if (!query.equals("null+null") && !URLEncoder.encode(query).equals("null+null")) {
                    if (URLEncoder.encode(query).equals(getInstance().lastQuery)) {
                        return null;
                    }
                    JSONObject o = Helper.getJSONObjectFromUrl("https://itunes.apple.com/search?term=" + URLEncoder.encode(query) + "&media=music&limit=1");

                    try {
                        if (o != null
                                && o.has("results")
                                && o.getJSONArray("results").length() > 0) {
                            JSONObject track = o.getJSONArray("results").getJSONObject(0);
                            String url = track.getString("artworkUrl100");
                            return url.replace("100x100bb.jpg", "500x500bb.jpg");
                        } else {
                            Log.v("INFO", "No items in Album Art Request");
                        }
                    } catch (JSONException e) {
                        Log.printStackTrace(e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String imageurl){
                if (imageurl == null) {
                    callback.finished(null);
                    return;
                }

                if (imageurl.equals(CACHE)) return;

                Picasso.get()
                        .load(imageurl)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                getInstance().saveBitmapToCache(bitmap, query);
                                callback.finished(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                callback.finished(null);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });

            }
        }.execute();
    }

    public interface AlbumCallback {
        void finished(Bitmap b);
    }

    //-- Caching
    private static AlbumArtGetter instance;
    private final LruCache lru;

    private AlbumArtGetter() {
         lru = new LruCache(1024);
    }

    public static AlbumArtGetter getInstance() {
        if (instance == null) {
            instance = new AlbumArtGetter();
        }
        return instance;
    }

    public void saveBitmapToCache(Bitmap bitmap, String imageQuery){
        try {
            AlbumArtGetter.getInstance().lru.set(imageQuery, bitmap);
        }catch (Exception e){}
    }

    public Bitmap retrieveBitmapFromCache(String imageQuery){
        try {
            Bitmap bitmap = AlbumArtGetter.getInstance().lru.get(imageQuery);
            return bitmap;

        }catch (Exception e){}
        return null;
    }

    public void cleanBitmap() {
        try {
            AlbumArtGetter.getInstance().lru.clear();
        } catch (Exception e){}
    }

}
