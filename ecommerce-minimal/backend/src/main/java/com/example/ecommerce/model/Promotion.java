package com.example.ecommerce.model;
import java.time.LocalDateTime;
public class Promotion {
    private Long id; private Long productId; private String title; private String promotionType; private java.math.BigDecimal promotionPrice; private Integer promotionStock; private LocalDateTime startAt; private LocalDateTime endAt; private Boolean enabled;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public String getPromotionType(){return promotionType;} public void setPromotionType(String promotionType){this.promotionType=promotionType;}
    public java.math.BigDecimal getPromotionPrice(){return promotionPrice;} public void setPromotionPrice(java.math.BigDecimal promotionPrice){this.promotionPrice=promotionPrice;}
    public Integer getPromotionStock(){return promotionStock;} public void setPromotionStock(Integer promotionStock){this.promotionStock=promotionStock;}
    public LocalDateTime getStartAt(){return startAt;} public void setStartAt(LocalDateTime startAt){this.startAt=startAt;}
    public LocalDateTime getEndAt(){return endAt;} public void setEndAt(LocalDateTime endAt){this.endAt=endAt;}
    public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean enabled){this.enabled=enabled;}
}
