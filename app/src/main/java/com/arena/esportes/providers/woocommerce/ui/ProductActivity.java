package com.arena.esportes.providers.woocommerce.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.arena.esportes.Config;
import com.arena.esportes.HolderActivity;
import com.arena.esportes.R;
import com.arena.esportes.attachmentviewer.model.MediaAttachment;
import com.arena.esportes.attachmentviewer.ui.AttachmentActivity;
import com.arena.esportes.comments.CommentsActivity;
import com.arena.esportes.providers.Provider;
import com.arena.esportes.providers.fav.FavDbAdapter;
import com.arena.esportes.providers.woocommerce.WooCommerceTask;
import com.arena.esportes.providers.woocommerce.adapter.ProductsAdapter;
import com.arena.esportes.providers.woocommerce.checkout.CartAssistant;
import com.arena.esportes.providers.woocommerce.checkout.PriceFormat;
import com.arena.esportes.providers.woocommerce.model.RestAPI;
import com.arena.esportes.providers.woocommerce.model.products.Attribute;
import com.arena.esportes.providers.woocommerce.model.products.Category;
import com.arena.esportes.providers.woocommerce.model.products.Image;
import com.arena.esportes.providers.woocommerce.model.products.Product;
import com.arena.esportes.providers.woocommerce.model.products.Tag;
import com.arena.esportes.providers.wordpress.ui.WordpressDetailActivity;
import com.arena.esportes.util.DetailActivity;
import com.arena.esportes.util.Helper;
import com.arena.esportes.util.InfiniteRecyclerViewAdapter;
import com.arena.esportes.util.WebHelper;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2019
 */

public class ProductActivity extends DetailActivity {

    private FavDbAdapter mDbHelper;
    private CartAssistant mCartAssistant;

    private TextView mPresentation;
    private TableLayout tableLayout;
    private Button btnCart;
    private Product product;
    private WebView htmlTextView;

    public final static String PRODUCT = "product";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Use the general detaillayout and set the viewstub for youtube
        setContentView(R.layout.activity_details);
        ViewStub stub = findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_woocommerce_product);
        View inflated = stub.inflate();

        mToolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //TextView detailsDescription = findViewById(R.id.description);
        TextView detailsSubTitle = findViewById(R.id.subtitle);
        TextView priceView = findViewById(R.id.price_text);
        TextView priceSaleView = findViewById(R.id.price_original_text);
        ImageButton btnFav = findViewById(R.id.favorite);
        thumb = findViewById(R.id.image);
        coolblue = findViewById(R.id.coolblue);
        tableLayout = findViewById(R.id.properties_grid);
        btnCart = findViewById(R.id.cart_button);
        mPresentation = findViewById(R.id.title);

        product = (Product) getIntent().getExtras().getSerializable(PRODUCT);
        // String description = Html.fromHtml(product.getDescription().replaceAll("<[^>]*>", "").trim()).toString();
        String descriptionShort = product.getShortDescription().replaceAll("<[^>]*>", "").trim();

        //detailsDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP,
        //        WebHelper.getTextViewFontSize(this));
        btnCart.bringToFront();

        mCartAssistant = new CartAssistant(this, btnCart, product);
/////////////////////////////////////////////////////////////////////////////////////////////////////       
/////////////ADMOB BANNER////////////   Helper.admobLoader(this, findViewById(R.id.adView));
/////////////////////////////////////////////////////////////////////////////////////////////////////

        mPresentation.setText(product.getName());
        //detailsDescription.setText(description);
        configureContentWebView();
        if (!descriptionShort.isEmpty())
            detailsSubTitle.setText(descriptionShort);
        else
            detailsSubTitle.setVisibility(View.GONE);

        //Price
        if (product.getOnSale()){
            priceSaleView.setVisibility(View.VISIBLE);
            priceSaleView.setText(PriceFormat.formatPrice(product.getRegularPrice()));
            priceSaleView.setPaintFlags(priceSaleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            priceView.setText(PriceFormat.formatPrice(product.getSalePrice()));
        } else {
            priceView.setText(PriceFormat.formatPrice(product.getPrice()));
        }

        String imageUrl = product.getImages().get(0).getSrc();
        Picasso.get().load(imageUrl).into(thumb);
        setUpHeader(imageUrl);

        // Listening to button event
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCartAssistant.addProductToCart(null);
            }
        });

        // Listening to button event
        btnFav.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                mDbHelper = new FavDbAdapter(ProductActivity.this);
                mDbHelper.open();

                if (mDbHelper.checkEvent(product.getName(), product, Provider.WOOCOMMERCE)) {
                    // Item is new
                    mDbHelper.addFavorite(product.getName(), product, Provider.WOOCOMMERCE);
                    Toast toast = Toast
                            .makeText(ProductActivity.this, getResources()
                                            .getString(R.string.favorite_success),
                                    Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(
                            ProductActivity.this,
                            getResources().getString(
                                    R.string.favorite_duplicate),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<MediaAttachment> list = new ArrayList<>();
                for (Image image : product.getImages()){
                    list.add(new MediaAttachment(image.getSrc(), MediaAttachment.MIME_PATTERN_IMAGE, null, image.getName()));
                }
                AttachmentActivity.startActivity(ProductActivity.this, list);
            }

        });

        loadProductProperties();
        loadRating();
        loadRelated();
    }

    private void configureContentWebView(){

        htmlTextView = findViewById(R.id.htmlTextView);
        htmlTextView.getSettings().setJavaScriptEnabled(true);
        htmlTextView.setBackgroundColor(Color.TRANSPARENT);
        htmlTextView.getSettings().setDefaultFontSize(14);
        htmlTextView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        htmlTextView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null
                        && (url.endsWith(".png") || url
                        .endsWith(".jpg") || url
                        .endsWith(".jpeg"))) {

                    AttachmentActivity.startActivity(ProductActivity.this, MediaAttachment.withImage(
                            url
                    ));

                    return true;
                } else if (url != null
                        && (url.startsWith("http://") || url
                        .startsWith("https://"))) {
                    HolderActivity.startWebViewActivity(ProductActivity.this, url, Config.OPEN_INLINE_EXTERNAL, false, null);
                    return true;
                } else {
                    Uri uri = Uri.parse(url);
                    Intent ViewIntent = new Intent(Intent.ACTION_VIEW, uri);

                    // Verify it resolves
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> activities = packageManager
                            .queryIntentActivities(ViewIntent, 0);
                    boolean isIntentSafe = activities.size() > 0;

                    // Start an activity if it's safe
                    if (isIntentSafe) {
                        startActivity(ViewIntent);
                    }
                    return true;
                }
            }
        });

        Document doc = Jsoup.parse(product.getDescription());
        String html = WebHelper.docToBetterHTML(doc, this);

        htmlTextView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
        htmlTextView.setVisibility(View.VISIBLE);
    }

    public void loadProductProperties(){
        //Categories
        if (product.getCategories().size() > 0) {
            ArrayList<String> categories = new ArrayList<>();
            for (Category category : product.getCategories()) {
                categories.add(category.getName());
            }
            addProperty(getString(R.string.category), TextUtils.join("\n", categories));
        }

        //Tags
        if (product.getTags().size() > 0) {
            ArrayList<String> tags = new ArrayList<>();
            for (Tag tag : product.getTags()) {
                tags.add(tag.getName());
            }
            addProperty(getString(R.string.tag), TextUtils.join("\n", tags));
        }

        //Attributes
        if (product.getAttributes().size() > 0) {
            for (Attribute attribute : product.getAttributes()) {
                addProperty(attribute.getName(), attribute.getOptions() == null ?
                        attribute.getOption() :
                        TextUtils.join("\n", attribute.getOptions()));
            }
        }

        //Weight
        if (!product.getWeight().isEmpty()) {
            addProperty(getString(R.string.weight), product.getWeight() + RestAPI.getUnitWeight());
        }

        //Dimensions
        if (!product.getDimensions().getHeight().isEmpty()) {
            String dimens = product.getDimensions().getLength() + " x " +
                    product.getDimensions().getWidth();
            dimens += !product.getDimensions().getHeight().isEmpty() ? " x " + product.getDimensions().getHeight() : "" ;
            dimens += " " + RestAPI.getUnitSize();

            addProperty(getString(R.string.dimensions), dimens);
        }

        //SKU
        addProperty(getString(R.string.id), Long.toString(product.getId()));
        addProperty(getString(R.string.sku), product.getSku());
    }

    private void addProperty(String key, String value) {
        View view = LayoutInflater.from(this).inflate(R.layout.activity_woocommerce_product_row, null);
        ((TextView) view.findViewById(R.id.key)).setText(key);
        ((TextView) view.findViewById(R.id.value)).setText(value);
        tableLayout.addView(view);
    }

    private void loadRating(){
        //Rating header
        if (product.getRatingCount() > 0) {
            RatingBar bar = ((RatingBar) findViewById(R.id.rating));
            bar.setRating(
                    Float.parseFloat(product.getAverageRating()));
            ((TextView) findViewById(R.id.rating_count)).setText(
                    String.format(getString(R.string.reviews), product.getRatingCount()));
        } else {
            ((TextView) findViewById(R.id.rating_count)).setText(
                    getString(R.string.no_reviews));
        }

        if (product.getRatingCount() > 0) {
            //Rating section
            RatingBar bar = findViewById(R.id.ratingTwo);
            TextView ratingValue = findViewById(R.id.ratingValue);
            TextView ratingCounter = findViewById(R.id.ratingCounter);

            bar.setRating(
                    Float.parseFloat(product.getAverageRating()));
            ratingValue.setText(String.format(Locale.US, "%.1f", Float.parseFloat(product.getAverageRating())));
            ratingCounter.setText(String.format(getString(R.string.reviews_description), product.getRatingCount()));

            findViewById(R.id.rating_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openRatings();
                }
            });
        } else {
            findViewById(R.id.rating_container_detail).setVisibility(View.GONE);
        }

    }

    private void openRatings(){
        Intent commentIntent = new Intent(this, CommentsActivity.class);
        commentIntent.putExtra(CommentsActivity.DATA_TYPE,
                CommentsActivity.WOOCOMMERCE_REVIEWS);
        commentIntent.putExtra(CommentsActivity.DATA_ID, Long.toString(product.getId()));
        startActivity(commentIntent);
    }

    private void loadRelated(){
        final RecyclerView relatedList = findViewById(R.id.related_list);

        final ArrayList<Product> productList = new ArrayList<>();
        final ProductsAdapter productsAdapter = new ProductsAdapter(ProductActivity.this, productList, null);
        productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        productsAdapter.setItemWidth(getResources().getDimension(R.dimen.woocommerce_related_product_width));
        relatedList.setAdapter(productsAdapter);
        relatedList.setLayoutManager(new LinearLayoutManager(ProductActivity.this, LinearLayoutManager.HORIZONTAL, false));

        WooCommerceTask.Callback<Product> callback = new WooCommerceTask.Callback<Product>() {
            @Override
            public void success(ArrayList<Product> result) {
                ViewGroup relatedView = findViewById(R.id.related_view);
                relatedView.setVisibility(View.VISIBLE);

                productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
                productList.addAll(result);
                productsAdapter.notifyDataSetChanged();
            }

            @Override
            public void failed() {
                relatedList.setVisibility(View.GONE);
            }
        };

        new WooCommerceTask.WooCommerceBuilder(this)
                .getProductsForIds(callback, product.getRelatedIds(), 1).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);

                sendIntent.putExtra(Intent.EXTRA_TEXT, product.getPermalink());
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, product.getName());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources()
                        .getString(R.string.share_header)));

                return true;
            case R.id.menu_view:
                HolderActivity.startWebViewActivity(ProductActivity.this, product.getPermalink(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.woocommerce_detail_menu, menu);
        onMenuItemsSet(menu);
        return true;
    }

}