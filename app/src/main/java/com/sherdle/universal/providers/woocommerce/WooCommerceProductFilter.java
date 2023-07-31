package com.sherdle.universal.providers.woocommerce;

public class WooCommerceProductFilter {
    private double minPrice;
    private double maxPrice;

    private boolean onlyFeatured;
    private boolean onlySale;
    private boolean onlyInStock;

    private String orderBy;
    private String order;

    private long category;
    private String categoryName;

    public WooCommerceProductFilter(){
    }

    public WooCommerceProductFilter minPrice(double minPrice){
        this.minPrice = minPrice;
        return this;
    }

    public WooCommerceProductFilter maxPrice(double maxPrice){
        this.maxPrice = maxPrice;
        return this;
    }

    public WooCommerceProductFilter onlySale(boolean onlySale){
        this.onlySale = onlySale;
        return this;
    }

    public WooCommerceProductFilter onlyFeatured(boolean onlyFeatured){
        this.onlyFeatured = onlyFeatured;
        return this;
    }

    public WooCommerceProductFilter onlyInStock(boolean onlyInStock){
        this.onlyInStock = onlyInStock;
        return this;
    }

    public WooCommerceProductFilter orderBy(String orderBy){
        this.orderBy = orderBy;
        return this;
    }

    public WooCommerceProductFilter order(String order){
        this.order = order;
        return this;
    }

    public WooCommerceProductFilter category(long category){
        return this.category(category, null);
    }

    public WooCommerceProductFilter category(long category, String categoryName){
        this.category = category;
        this.categoryName = categoryName;
        return this;
    }

    public String getQuery(){
        StringBuilder query = new StringBuilder();
        if (minPrice != 0)
            query.append("&min_price=" + minPrice);
        if (maxPrice != 0)
            query.append("&max_price=" + maxPrice);
        if (onlyFeatured)
            query.append("&featured=" + true);
        if (onlySale)
            query.append("&on_sale=" + true);
        if (onlyInStock)
            query.append("&stock_status=" + "instock");
        if (orderBy != null)
            query.append("&orderby=" + orderBy);
        if (order != null)
            query.append("&order=" + order);
        if (category != 0)
            query.append("&category=" + category);
        return query.toString();
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public boolean isOnlyFeatured() {
        return onlyFeatured;
    }

    public boolean isOnlySale() {
        return onlySale;
    }

    public boolean isOnlyInStock() {
        return onlyInStock;
    }

    public String getOrderBy() { return orderBy; }

    public String getOrder() { return order; }

    public long getCategory() { return category; }

    public String getCategoryName() { return categoryName; }

    public void clearFilters() {
        this.minPrice = 0;
        this.maxPrice = 0;
        this.orderBy = null;
        this.order = null;
        this.onlyFeatured = false;
        this.onlyInStock = false;
        this.onlySale = false;
        this.category = 0;
        this.categoryName = null;
    }

}
