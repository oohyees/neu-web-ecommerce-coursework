package com.example.ecommerce.model;
public class ProductSpec {
    private Long id; private Long productId; private String specName; private String specValue;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public String getSpecName(){return specName;} public void setSpecName(String specName){this.specName=specName;}
    public String getSpecValue(){return specValue;} public void setSpecValue(String specValue){this.specValue=specValue;}
}
