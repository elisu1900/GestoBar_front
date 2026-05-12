package com.elias.gestobar.model.dto;

import java.math.BigDecimal;

public class ProductRequestDto {

    private Integer    categoryId;
    private String     name;
    private BigDecimal sellPrice;
    private BigDecimal costPrice;
    private Boolean    isActive;

    public ProductRequestDto() {}

    public ProductRequestDto(String name, BigDecimal sellPrice, BigDecimal costPrice, Boolean isActive) {
        this.categoryId = 1;
        this.name       = name;
        this.sellPrice  = sellPrice;
        this.costPrice  = costPrice;
        this.isActive   = isActive;
    }

    public Integer    getCategoryId() { return categoryId; }
    public String     getName()       { return name; }
    public BigDecimal getSellPrice()  { return sellPrice; }
    public BigDecimal getCostPrice()  { return costPrice; }
    public Boolean    getIsActive()   { return isActive; }

    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public void setName(String name)              { this.name = name; }
    public void setSellPrice(BigDecimal v)        { this.sellPrice = v; }
    public void setCostPrice(BigDecimal v)        { this.costPrice = v; }
    public void setIsActive(Boolean v)            { this.isActive = v; }
}
