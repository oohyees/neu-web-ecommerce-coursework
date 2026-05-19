package com.example.ecommerce.model;
import java.time.LocalDateTime;
public class ProductReview {
    private Long id; private Long userId; private Long productId; private Integer rating; private String content; private String imageUrl; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public Integer getRating(){return rating;} public void setRating(Integer rating){this.rating=rating;}
    public String getContent(){return content;} public void setContent(String content){this.content=content;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
