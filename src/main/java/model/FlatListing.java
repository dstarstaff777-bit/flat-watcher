package model;

public class FlatListing {
    private String title;
    private int price;
    private int rooms;
    private String district;
    private String url;

    public FlatListing(String title, int price, int rooms, String district, String url) {
        this.title = title;
        this.price = price;
        this.rooms = rooms;
        this.district = district;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public int getRooms() {
        return rooms;
    }

    public String getDistrict() {
        return district;
    }

    public String getUrl() {
        return url;
    }

}
