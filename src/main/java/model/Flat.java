package model;

public class Flat {
    public String id;
    public String title;
    public String price;
    public String url;
    public String rooms;
    public String area;
    public String metro;


    public Flat(String id, String title, String price, String url, String rooms, String area, String metro) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.url = url;
        this.rooms = rooms;
        this.area = area;
        this.metro = metro;
    }
}