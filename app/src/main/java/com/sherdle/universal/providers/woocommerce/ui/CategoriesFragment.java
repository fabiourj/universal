package com.sherdle.universal.providers.woocommerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sherdle.universal.HolderActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.providers.Provider;
import com.sherdle.universal.providers.overview.OverviewAdapter;
import com.sherdle.universal.providers.woocommerce.WooCommerceTask;
import com.sherdle.universal.providers.woocommerce.adapter.CategoryAdapter;
import com.sherdle.universal.providers.woocommerce.model.CredentialStorage;
import com.sherdle.universal.providers.woocommerce.model.orders.Order;
import com.sherdle.universal.providers.woocommerce.model.products.Category;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2019
 */
public class CategoriesFragment extends Fragment implements WooCommerceTask.Callback<Category>, InfiniteRecyclerViewAdapter.LoadMoreListener, CategoryAdapter.OnCategoryClick {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private Activity mAct;

    private int page = 1;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_refresh, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.list);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, this, this);
        categoryAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        recyclerView.setAdapter(categoryAdapter);

        mAct = getActivity();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mAct);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshItems();
        loadHeader();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    private void loadHeader() {
        LayoutInflater layoutInflater = LayoutInflater.from(mAct);

        //TODO sale as header
        /*ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_order_header, null);
        TextView text = headerView.findViewById(R.id.order_header_text);
        View signOut = headerView.findViewById(R.id.user_sign_out_button);

        text.setText(String.format(getString(R.string.greeting), CredentialStorage.getName(mAct)));
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        categoryAdapter.setHeader(headerView);*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void success(ArrayList<Category> result) {
        if (result.size() > 0) {
            categoryList.addAll(result);
            categoryAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
        } else {
            categoryAdapter.setHasMore(false);
            categoryAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void failed() {
        categoryAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshItems() {
        // Load items
        page = 1;
        categoryList.clear();
        categoryAdapter.setHasMore(true);
        categoryAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        requestItems();
    }

    @Override
    public void onMoreRequested() {
        page = page + 1;
        requestItems();
    }

    private void requestItems() {
        WooCommerceTask.WooCommerceBuilder builder = new WooCommerceTask.WooCommerceBuilder(mAct);
        builder.getCategories(this, page).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.woocommerce_menu, menu);
        menu.findItem(R.id.menu_search).setVisible(false);
        ThemeUtils.tintAllIcons(menu, mAct);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_cart:
                HolderActivity.startActivity(getActivity(), CartFragment.class);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCategorySelected(Category category) {
        HolderActivity.startActivity(getActivity(), WooCommerceFragment.class, "woocommerce", new String[]{Long.toString(category.getId())});
    }
}