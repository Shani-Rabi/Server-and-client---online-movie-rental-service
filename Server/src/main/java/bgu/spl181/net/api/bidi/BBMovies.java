package bgu.spl181.net.api.bidi;

import java.util.ArrayList;

/**
 * this class will hold a single movie in the Blocbuster .
 */
public class BBMovies {
    private long id;
    private String name;
    private long price;
    private ArrayList<String> bannedCountries;
    private long availableAmount;
    private long totalAmount;

    public BBMovies(String name, long availableAmount, long price,long id, ArrayList <String> bannedCountries) {
        this.name = name;
        this.availableAmount = availableAmount;
        this.totalAmount = availableAmount;
        this.id=id;
        this.price=price;
        this.bannedCountries=bannedCountries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public long getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(long availableAmount) {
        this.availableAmount = availableAmount;
    }

    public long getPrice() {
        return price;
    }

    public ArrayList<String> getBannedCountries() {
        return bannedCountries;
    }

    public void setBannedCountries(ArrayList<String> bannedCountries) {
        this.bannedCountries = bannedCountries;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }
}
