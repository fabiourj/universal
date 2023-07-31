package com.sherdle.universal.providers.woocommerce;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sherdle.universal.providers.woocommerce.interceptor.OAuthInterceptor;
import com.sherdle.universal.providers.woocommerce.model.RestAPI;
import com.sherdle.universal.providers.woocommerce.model.orders.Order;
import com.sherdle.universal.providers.woocommerce.model.products.Category;
import com.sherdle.universal.providers.woocommerce.model.products.Product;
import com.sherdle.universal.providers.woocommerce.model.products.Review;
import com.sherdle.universal.providers.woocommerce.model.users.User;
import com.sherdle.universal.providers.woocommerce.ui.WooCommerceDebugDialog;
import com.sherdle.universal.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.helper.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2021
 */
public class WooCommerceTask<T> extends AsyncTask<Void, Void, ArrayList<T>> {

    private final Callback callback;
    private final String url;
    private final Class type;
    private final RestAPI api;

    private static OkHttpClient client;

    private static final int CATEGORIES = 10;
    private static final String PARAM_PER_PAGE = "?per_page=20";
    private static final String PARAM_PUBLISHED = "&status=publish";

    public static class WooCommerceBuilder {
        private final RestAPI restAPI;

        public WooCommerceBuilder(Context context){
            this.restAPI = new RestAPI(context);
        }

        public WooCommerceTask<Product> getProducts(Callback<Product> callback, int page, WooCommerceProductFilter filter) {
            String url = restAPI.getHost() + restAPI.getPath();
            url += "products";
            url += PARAM_PER_PAGE + PARAM_PUBLISHED + "&page=" + page + filter.getQuery();
            return new WooCommerceTask<>(Product.class, callback, url, restAPI);
        }

        public WooCommerceTask<Product> getProductsForQuery(
                Callback<Product> callback, String query, int page, WooCommerceProductFilter filter) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "products";
            url += PARAM_PER_PAGE + PARAM_PUBLISHED + "&page=" + page + "&search=" + query + filter.getQuery();
            return new WooCommerceTask<>(Product.class, callback, url, restAPI);
        }

        public WooCommerceTask<Category> getCategories(Callback<Category> callback, int page) {
            return getCategories(callback, page, -1);
        }

        public WooCommerceTask<Category> getCategories(Callback<Category> callback, int page, long parent) {
            assert page >= 1;

            String url = restAPI.getHost() + restAPI.getPath();
            url += "products/categories";
            url += "?per_page=" + CATEGORIES + "&orderby=count&order=desc&page=" + page;
            url += (parent >= 0) ? ("&parent=" + parent) : "";
            return new WooCommerceTask<>(Category.class, callback, url, restAPI);
        }

        public WooCommerceTask<Product> getProductsForIds(
                Callback<Product> callback, List<Long> ids, int page) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "products";
            url += "?page=" + page + PARAM_PUBLISHED + "&include=" + TextUtils.join(",", ids);
            return new WooCommerceTask<>(Product.class, callback, url, restAPI);
        }

        public WooCommerceTask<Product> getVariationsForProduct(
                Callback<Product> callback, Long product) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "products/" + product + "/variations";
            url += "?per_page=" + 10;
            return new WooCommerceTask<>(Product.class, callback, url, restAPI);
        }

        public WooCommerceTask<Order> getOrders(
                Callback<Order> callback, int customer, int page) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "orders?customer="+ customer +"&page=" + page;
            return new WooCommerceTask<>(Order.class, callback, url, restAPI);
        }

        public WooCommerceTask<User> getUsers(
                Callback<User> callback, String email) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "customers?email="+ email;
            return new WooCommerceTask<>(User.class, callback, url, restAPI);
        }

        public WooCommerceTask<Review> getReviews(
                Callback<Review> callback, long productId) {

            String url = restAPI.getHost() + restAPI.getPath();
            url += "products/reviews?product="+ productId+ "&status=approved";
            return new WooCommerceTask<>(Review.class, callback, url, restAPI);
        }
    }

    private WooCommerceTask(Class type, Callback callback, String url, RestAPI api) {
        this.type = type;
        this.callback = callback;
        this.url = url;
        this.api = api;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ArrayList<T> doInBackground(Void... params) {
        JSONArray Jobject = null;
        String result = null;
        try {
            result = getResponse(url);
            Jobject = new JSONArray(result);
        } catch (IOException | JSONException e) {
            Log.printStackTrace(e);
        }

        if (Jobject == null) {
            WooCommerceDebugDialog.showDialogIfAuthFailed(result, api.getContext());
            return null;
        }

        ArrayList<T> resultList = new ArrayList<>();
        for (int i = 0; i < Jobject.length(); i++) {
            try {
                if (type.equals(Product.class)) {
                    Product product = new Gson().fromJson(Jobject.getJSONObject(i).toString(), Product.class);

                    //Products that aren't purchase-able, like external and group products aren't supported
                    if (!product.getPurchasable() && StringUtil.isBlank(product.getExternalUrl())) continue;

                    resultList.add((T) product);
                } else if (type.equals(Category.class)) {
                    Category category = new Gson().fromJson(Jobject.getJSONObject(i).toString(), Category.class);
                    if (category.getSlug().equals("uncategorized") && category.getCount() == 0) continue;
                    resultList.add((T) category);
                } else if (type.equals(Order.class)) {
                    Order order = new Gson().fromJson(Jobject.getJSONObject(i).toString(), Order.class);
                    resultList.add((T) order);
                } else if (type.equals(User.class)) {
                    User user = new Gson().fromJson(Jobject.getJSONObject(i).toString(), User.class);
                    resultList.add((T) user);
                } else if (type.equals(Review.class)) {
                    Review review = new Gson().fromJson(Jobject.getJSONObject(i).toString(), Review.class);
                    resultList.add((T) review);
                }
            } catch (JSONException e) {
                Log.printStackTrace(e);
            }
        }

        return resultList;
    }

    private String getResponse(String url) throws IOException, JSONException {
        client = getRestAPIClient(api);

        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String totalPages = response.header("x-wp-totalpages");
            return response.body().string();
        }
    }

    public static OkHttpClient getRestAPIClient(RestAPI api){
        if (client == null) {
            OAuthInterceptor oauth1Woocommerce = new OAuthInterceptor.Builder()
                    .consumerKey(api.getCustomerKey())
                    .consumerSecret(api.getCustomerSecret())
                    .build();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(oauth1Woocommerce)
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(25, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            if (Log.LOG) {
            /*
             * In order to start logging these requests. Add the following to build.gradle
             * compile 'com.squareup.okhttp3:logging-interceptor:3.3.1'
             * And uncomment the lines below
             */

                //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                //logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                //builder.addInterceptor(logging);
            }

            client = builder.build();
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPostExecute(ArrayList<T> result) {
        if(result != null) {
            callback.success(result);
        }else{
            callback.failed();
        }
    }

    public interface Callback<T>{
        void success(ArrayList<T> productList);
        void failed();
    }

}
