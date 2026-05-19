package com.example.ecommerce.model;
import java.math.BigDecimal;
public class Coupon {
    private Long id; private String name; private BigDecimal thresholdAmount; private BigDecimal discountAmount; private Boolean enabled;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public BigDecimal getThresholdAmount(){return thresholdAmount;} public void setThresholdAmount(BigDecimal thresholdAmount){this.thresholdAmount=thresholdAmount;}
    public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal discountAmount){this.discountAmount=discountAmount;}
    public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean enabled){this.enabled=enabled;}
}
