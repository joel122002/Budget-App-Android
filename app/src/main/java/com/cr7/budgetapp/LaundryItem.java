package com.cr7.budgetapp;

public class LaundryItem {
    private int id;
    private int date;
    private String objectId;
    private int laundry;

    public LaundryItem(int date, String objectId, int laundry) {
        this.date = date;
        this.objectId = objectId;
        this.laundry = laundry;
    }

    public LaundryItem(int id, int date, String objectId, int laundry) {
        this.id = id;
        this.date = date;
        this.objectId = objectId;
        this.laundry = laundry;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setLaundry(int laundry) {
        this.laundry = laundry;
    }

    public int getId() {
        return id;
    }

    public int getDate() {
        return date;
    }

    public String getObjectId() {
        return objectId;
    }

    public int getLaundry() {
        return laundry;
    }
}
