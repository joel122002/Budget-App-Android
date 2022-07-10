package com.cr7.budgetapp;

public class Item {
    private int id;
    private int date;
    private String objectId;
    private String item;
    private int price;

    public Item(int date, String objectId, String item, int price) {
        this.date = date;
        this.objectId = objectId;
        this.item = item;
        this.price = price;
    }

    public Item(int id, int date, String objectId, String item, int price) {
        this.id = id;
        this.date = date;
        this.objectId = objectId;
        this.item = item;
        this.price = price;
    }

    public Item(int date, String item, int price) {
        this.date = date;
        this.item = item;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
