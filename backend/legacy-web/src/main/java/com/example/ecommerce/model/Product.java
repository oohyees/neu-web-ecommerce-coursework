package com.example.ecommerce.model;

import java.math.BigDecimal;

public class Product {
    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Boolean isOnSale;
    private String imageUrl;
    private String detailHtml;
    private String paramsText;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Boolean getIsOnSale() { return isOnSale; }
    public void setIsOnSale(Boolean isOnSale) { this.isOnSale = isOnSale; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDetailHtml() { return detailHtml; }
    public void setDetailHtml(String detailHtml) { this.detailHtml = detailHtml; }
    public String getParamsText() { return paramsText; }
    public void setParamsText(String paramsText) { this.paramsText = paramsText; }
}
