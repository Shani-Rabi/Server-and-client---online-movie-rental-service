package bgu.spl181.net.api.bidi;

import java.util.ArrayList;

/**
 * this class will hold a single User in the Blockbuster Users.
 */
public class BBUser {
    private String username;
    private String type;
    private String country;
    private  String password;
    private ArrayList<movieOfUser> movies=new ArrayList<>();
    private Long balance=new Long(0);
    public BBUser(String username, String password, String country){
        this.username=username;
        this.password=password;
        this.country=country;
        this.type="normal";
    }

    public ArrayList<movieOfUser> getMovies() {
        return movies;
    }

    public String getCountry() {
        return country;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public void addmovie(movieOfUser movie){
        movies.add(movie);
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public long getBalance() {
        return balance;
    }

}
