package com.sherdle.universal.providers.woocommerce.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.providers.woocommerce.adapter.CartAdapter;
import com.sherdle.universal.providers.woocommerce.checkout.Cart;
import com.sherdle.universal.providers.woocommerce.checkout.CartAssistant;
import com.sherdle.universal.providers.woocommerce.checkout.CartProduct;
import com.sherdle.universal.providers.woocommerce.checkout.CartWithCookies;
import com.sherdle.universal.providers.woocommerce.checkout.PriceFormat;

import java.util.List;

import okhttp3.Cookie;


/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2019
 */
public class CartFragment extends Fragment implements Cart.CartListener {

    private RecyclerView recyclerView;
    private TextView textViewCheckOutPrice;
    private Button btnFinish;
    private View loadingView;
    private View view;

    private Cart cart;
    private CartAdapter productsAdapter;
    private float total;
    
    private Activity mAct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wc_cart, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        this.mAct = getActivity();

        if (mAct instanceof HolderActivity)
            ((HolderActivity) mAct).getSupportActionBar().setTitle(R.string.cart_title);

        recyclerView = view.findViewById(R.id.recycleViewCheckOut);
        textViewCheckOutPrice = view.findViewById(R.id.textViewCheckOutPrice);
        btnFinish = view.findViewById(R.id.btnFinish);
        loadingView = view.findViewById(R.id.loading_view);
        cart = Cart.getInstance(mAct);
        cart.setCartListener(this);
        productsAdapter = new CartAdapter(mAct, cart);

        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
        //        LinearLayoutManager.VERTICAL);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(productsAdapter);
        updateQuantity();

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cart.getCartProducts().size() == 0) return;

                loadingView.setVisibility(View.VISIBLE);

                //Now open a webview with parameters like:
                // URL  + "checkout/?add-to-cart=X&quantity=Y"
                CartWithCookies cookieCart = new CartWithCookies(mAct, new CartWithCookies.AllProductsAddedCallback(){
                    @Override
                    public void success(final List<Cookie> cookies) {
                        mAct.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                CheckoutActivity.startActivity(mAct, cookies);
                                mAct.finish();
                            }
                        });
                    }

                    @Override
                    public void failure() {
                        mAct.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                loadingView.setVisibility(View.GONE);
                                Toast.makeText(mAct, R.string.cart_failed, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                cookieCart.addProductsToCart(cart.getCartProducts());
            }
        });
    }

    private void updateQuantity(){
        if (cart.getCartProducts().size() == 0)
            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        else
            view.findViewById(R.id.empty_view).setVisibility(View.GONE);

        total = 0;
        for (CartProduct item : cart.getCartProducts()){
            total += CartAssistant.getPrice(item.getProduct(), item.getVariation()) * item.getQuantity() ;
        }
        textViewCheckOutPrice.setText(PriceFormat.formatPrice(total));
    }

    @Override
    public void onCartUpdated() {
        updateQuantity();
    }
}
