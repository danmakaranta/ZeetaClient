package com.example.zeeta.data;

public class FashionPrices {
    private String styleName;
    private long price;

    public FashionPrices(String styleName, long price) {
        this.styleName = styleName;
        this.price = price;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
