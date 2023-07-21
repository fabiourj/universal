package com.sherdle.universal.providers.woocommerce.checkout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.sherdle.universal.Config;
import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.providers.woocommerce.WooCommerceTask;
import com.sherdle.universal.providers.woocommerce.model.products.Attribute;
import com.sherdle.universal.providers.woocommerce.model.products.Product;
import com.sherdle.universal.providers.woocommerce.ui.CartFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartAssistant {

    private static final boolean LEGACY_VARIATIONS_DIALOG = false;

    private final Cart mCart;
    private final Activity mContext;
    private final View mCartButton;
    private final Product mProduct;

    private ArrayList<Product> variations;
    private AlertDialog dialog;

    /**
     * An assistant for handling the process between a press on the buy button and
     * adding to cart. Keeping mind stock checking, variations and informing the user.
     * @param context Context
     * @param cartButton Button that has been pressed
     * @param product Product to add
     */
    public CartAssistant(Activity context, View cartButton, Product product) {
        this.mCart = Cart.getInstance(context);
        this.mContext = context;
        this.mCartButton = cartButton;
        this.mProduct = product;
    }

    public void addProductToCart(Product variation) {
        if (mProduct.getExternalUrl() != null && !mProduct.getExternalUrl().isEmpty()) {
            HolderActivity.startWebViewActivity(mContext, mProduct.getExternalUrl(), Config.OPEN_EXPLICIT_EXTERNAL, false, null);
        } else if (mProduct.getType().equals("variable") && variation == null) {
            retrieveVariations();
        } else {
            boolean success = mCart.addProductToCart(mProduct, variation);
            int resID = success ? R.string.cart_success : R.string.out_of_stock;
            Snackbar bar = Snackbar.make(mCartButton, resID, Snackbar.LENGTH_LONG)
                    .setAction(R.string.view_cart, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HolderActivity.startActivity(mContext, CartFragment.class);
                        }
                    });
            bar.show();
            ((TextView) bar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).
                    setTextColor(mContext.getResources().getColor(R.color.white));
        }
    }

    private void retrieveVariations() {
        if (variations == null) {
            final ProgressDialog progress = ProgressDialog.show(mContext,
                    mContext.getResources().getString(R.string.loading),
                    mContext.getResources().getString(R.string.loading), true);

            WooCommerceTask.Callback<Product> callback =
                    new WooCommerceTask.Callback<Product>() {
                @Override
                public void success(ArrayList<Product> productList) {
                    progress.dismiss();
                    variations = productList;
                    selectVariation();
                }

                @Override
                public void failed() {
                    progress.dismiss();
                    Toast.makeText(mContext, R.string.varations_missing, Toast.LENGTH_SHORT).show();
                }
            };

            new WooCommerceTask.WooCommerceBuilder(mContext)
                    .getVariationsForProduct(callback, mProduct.getId()).execute();

        } else {
            selectVariation();
        }
    }

    private void selectVariation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        HashMap<Integer,String> valueForAttribute = new HashMap<>();

        if (variations.size() == 0) {
            builder.setMessage(R.string.out_of_stock);
        } else if (!LEGACY_VARIATIONS_DIALOG) {

            LayoutInflater inflater = mContext.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.fragment_wc_variation_dialog, null);
            builder.setView(dialogView);

            LinearLayout chipGroupHolder = dialogView.findViewById(R.id.chipgroup_holder);
            TextView result = dialogView.findViewById(R.id.chipgroup_result);
            Button addToCart = dialogView.findViewById(R.id.cart_button);

            for (Attribute attribute : mProduct.getAttributes()) {
                if (attribute.getOptions() != null) {
                    View chipGroupView = inflater.inflate(R.layout.fragment_wc_variation_group, null);

                    TextView chipGroupTitle = chipGroupView.findViewById(R.id.chipgroup_header);
                    chipGroupTitle.setText(attribute.getName());
                    ChipGroup chipGroup = chipGroupView.findViewById(R.id.chipgroup);
                    chipGroup.setSelectionRequired(true);
                    chipGroup.setSingleSelection(true);

                    for (String option : attribute.getOptions()) {
                        Chip chip = new Chip(new ContextThemeWrapper(mContext, R.style.Theme_MaterialComponents_Light));
                        chip.setText(option);
                        chip.setTag(attribute.getId());
                        chip.setCheckable(true);

                        chipGroup.addView(chip);
                    }

                    chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(ChipGroup group, int checkedId) {
                            Chip checked = group.findViewById(checkedId);
                            valueForAttribute.put((Integer) checked.getTag(), checked.getText().toString());


                            //If all variations are selected
                            if (valueForAttribute.size() == mProduct.getAttributes().size()) {

                                Product match = null;

                                //Foreach variation on the list, check if the attributes match all
                                for (Product variation : variations) {
                                    boolean isMatch = true;
                                    for (Attribute attribute : variation.getAttributes()) {
                                        String option = valueForAttribute.get(attribute.getId());

                                        if (!attribute.getOption().equals(option)) {
                                            isMatch = false;
                                        }
                                    }

                                    if (isMatch) {
                                        match = variation;
                                    }
                                }

                                //If a variation has been found, display it
                                if (match != null) {
                                    String description = getVariationDescription(match);
                                    description += " (" + PriceFormat.formatPrice(getPrice(mProduct, match)) + ")";

                                    result.setText(description);
                                    addToCart.setVisibility(View.VISIBLE);
                                    Product finalMatch = match;
                                    addToCart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            addProductToCart(finalMatch);
                                            dialog.cancel();
                                        }
                                    });
                                } else {
                                    result.setText(R.string.varations_unavailable);
                                    addToCart.setVisibility(View.GONE);
                                }
                            }
                        }
                    });

                    chipGroupHolder.addView(chipGroupView);
                }
            }
        } else {

            ArrayList<String> items = new ArrayList<>();
            for (Product variation : variations) {
                String item = getVariationDescription(variation);
                item += " (" + PriceFormat.formatPrice(getPrice(mProduct, variation)) + ")";
                items.add(item);
            }

            String[] arr = items.toArray(new String[0]);
            builder.setItems(arr, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    addProductToCart(variations.get(i));
                }
            });
        }

        builder.setTitle(R.string.varations);
        dialog = builder.create();
        dialog.show();
    }

    public static String getVariationDescription(Product variation){
        List<String> attributes = new ArrayList<String>();
        for (Attribute attribute : variation.getAttributes()){
            attributes.add(attribute.getName() + ": " + attribute.getOption());
        }
        return TextUtils.join(", ", attributes);
    }

    public static float getPrice(Product product, Product variation){
        if (variation == null || variation.getPrice() == 0){
            return product.getPrice();
        } else {
            if (variation.getOnSale())
                return variation.getSalePrice();
            else
                return variation.getPrice();
        }
    }
}
