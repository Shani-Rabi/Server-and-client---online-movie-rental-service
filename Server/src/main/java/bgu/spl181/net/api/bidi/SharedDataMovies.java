package bgu.spl181.net.api.bidi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * this class hold the shared data of the movies state in the Blockbuster.
 */
public class SharedDataMovies {
    ArrayList<BBUser> users;
    ArrayList<BBMovies> movies;
    private int maxid=0;

    private final ReadWriteLock sharedDataLockMovies = new ReentrantReadWriteLock();
    private final ReadWriteLock sharedDataLockUsers = new ReentrantReadWriteLock();

    public ReadWriteLock getSharedDataLockMovies() {
        return sharedDataLockMovies;
    }

    public ReadWriteLock getSharedDataLockUsers() {
        return sharedDataLockUsers;
    }


    /**
     * initial the list according to the initial jsons
     */
    public SharedDataMovies() {
        String USERS_JSON_PATH = "Database" + File.separator + "Users.json";
        String MOVIES_JSON_PATH = "Database" + File.separator + "Movies.json";
        try (FileReader usersFileReader = new FileReader(USERS_JSON_PATH);
             FileReader moviesFileReader = new FileReader(MOVIES_JSON_PATH)) {
            helpMovies tempmovies = new Gson().fromJson(moviesFileReader, helpMovies.class);
            helpUsers tempusers = new Gson().fromJson(usersFileReader, helpUsers.class);
            users = tempusers.getUsers();
            movies = tempmovies.getMovies();
           for(BBMovies cuurMovie:movies){
               if(cuurMovie.getId()>maxid)
                   maxid=(int)cuurMovie.getId();
           }
           maxid=maxid+1;
        } catch (IOException e) {
            System.out.println("Didn't find json files");
        }
    }

    /**
     * update the json according to the lists.
     */
    public void updateUsers() {
        String USERS_JSON_PATH = "database/Users.json";
        helpUsers tempusers = new helpUsers();
        tempusers.setUsers(users);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        Gson gson = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create();

        try (FileWriter fileWriter = new FileWriter(USERS_JSON_PATH)) {
            gson.toJson(tempusers, fileWriter);
        } catch (IOException EX) {

        }
    }

    /**
     * update the json according to the lists.
     */
    public void updateMovies() {
        String MOVIES_JSON_PATH = "database/Movies.json";
        helpMovies tempmovies = new helpMovies();
        tempmovies.setMovies(movies);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        Gson gson = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create();

        try (FileWriter fileWriter = new FileWriter(MOVIES_JSON_PATH)) {
            gson.toJson(tempmovies, fileWriter);
        } catch (IOException EX) {

        }
    }


    /**
     * @param user
     * @return true if the username is registered
     */
    public boolean isRegistered(String user) {
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(user)) {
                return true;
            }
        }
        return false;

    }


    /**
     * @param newUser
     * add new BBuser to the list.
     */
    public void addUser(BBUser newUser) {
        users.add(newUser);
        updateUsers();
    }


    /**
     * @param user
     * @return true if username is type of admin.
     */
    public boolean isAdmin(String user) {
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(user))
                if (currUser.getType().equals("admin")) {

                    return true;
                } else {
                    return false;
                }
        }

        return false;
    }

    /**
     * @param userName
     * @param movieName
     * @return the number of available amoubt of movie name after success rent or -1 if not success
     */
    public long handleRent(String userName, String movieName) {
        boolean exists = false;
        BBUser theUser = null;
        BBMovies theMovie = null;
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                exists = true;
                theMovie = currMovie;
                break;
            }
        }
        if (!exists) {
            return -1;
        }
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(userName)) {
                theUser = currUser;
                break;
            }
        }
        if (theUser.getBalance() < theMovie.getPrice()) {
            return -1;
        }
        if (containsMovieIgnoreCase(theUser.getMovies(), movieName)) {
            return -1;
        }
        if (containsIgnoreCase(theMovie.getBannedCountries(), theUser.getCountry())) {
            return -1;
        }
        if (theMovie.getAvailableAmount() <= 0) {
            return -1;

        } else {
            theUser.setBalance(theUser.getBalance() - theMovie.getPrice());
            theMovie.setAvailableAmount(theMovie.getAvailableAmount() - 1);
            theUser.getMovies().add(new movieOfUser(movieName, theMovie.getId()));
            updateUsers();
            updateMovies();
            return theMovie.getAvailableAmount();
        }

    }

    /**
     * @param userName
     * @return the balance of username
     */
    public long getBalance(String userName) {
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(userName)) {
                return currUser.getBalance();
            }
        }
        return 0;
    }

    /**
     * @param Amount
     * @param user
     * @return the new balance of the user or -1 if the add didnt succedd
     */
    public long addToBalance(int Amount, String user) {
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(user)) {
                updateUsers();
                currUser.setBalance(currUser.getBalance() + Amount);
                return currUser.getBalance();

            }
        }
        return -1;
    }

    /**
     * @return string of all the movies in the Blockbuster
     */
    public String getAllMovieNames() {
        String Output = "";
        for (BBMovies currMovie : movies) {
            Output = Output + '"' + currMovie.getName() + '"' + " ";
        }
        if (!Output.isEmpty())
            Output = Output.substring(0, Output.length() - 1);
        return Output;
    }

    /**
     * @param movieName
     * @return the movieprice of moviename
     */
    public long getMoviePrice(String movieName) {
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                return currMovie.getPrice();
            }
        }
        return 0;
    }

    /**
     * @param userName
     * @param movieName
     * @return the new available amount of the movie or -1 if not success
     */
    public long handleReturn(String userName, String movieName) {
        BBUser theUser = null;
        BBMovies theMovie = null;

        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(userName)) {
                theUser = currUser;

            }
        }
        if (!containsMovieIgnoreCase(theUser.getMovies(), movieName)) {
            return -1;
        }
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                theMovie = currMovie;

            }
        }
        if (theMovie == null) {
            return -1;
        }

        theMovie.setAvailableAmount(theMovie.getAvailableAmount() + 1);
        removeMovieIgnoreCase(theUser.getMovies(), movieName);
        updateUsers();
        updateMovies();
        return theMovie.getAvailableAmount();

    }

    /**
     * @param movieName
     * @return a string that describe the moviename with his fields
     */
    public String getMovieInfo(String movieName) {
        String Output = "";
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                Output = Output + '"' + currMovie.getName() + '"' + " " + currMovie.getAvailableAmount() + " " + currMovie.getPrice() + " ";
                for (String temp : currMovie.getBannedCountries()) {
                    Output = Output + '"' + temp + '"' + " ";
                }
                return Output;
            }
        }
        return "";
    }


    /**
     * @param user
     * @param password
     * @return true if the password is the password of this user
     */
    public boolean isGoodPassword(String user, String password) {
        for (BBUser currUser : users) {
            if (currUser.getUsername().equalsIgnoreCase(user)) {
                if (currUser.getPassword().equals(password)) { // The only place it shouldn't be case sensitive!!!
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * @param movieName
     * @param amount
     * @param price
     * @param bannedCountries
     * @return true if the movie was added.
     */
    public boolean addMovie(String movieName, String amount, String price, String bannedCountries) {
        String currCountry;
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                return false;
            }
        }
        long amounttemp = Integer.parseInt(amount);
        long pricetemp = Integer.parseInt(price);
        if (amounttemp <= 0 || pricetemp <= 0) {
            return false;
        }
        ArrayList<String> theCountries = new ArrayList<>();
        while (bannedCountries.length() > 0) {
            currCountry = "";
            bannedCountries = bannedCountries.substring(1);
            while (bannedCountries.charAt(0) != '"') {
                currCountry = currCountry + bannedCountries.charAt(0);
                bannedCountries = bannedCountries.substring(1);
            }
            bannedCountries = bannedCountries.substring(1);
            while (bannedCountries.length() > 0 && bannedCountries.charAt(0) != '"') {
                bannedCountries = bannedCountries.substring(1);
            }
            if (currCountry != "")
                theCountries.add(currCountry);
        }
        long max=maxid;
        BBMovies newMovie = new BBMovies(movieName,amounttemp,pricetemp,max,theCountries);

        maxid = maxid + 1;
        movies.add(newMovie);
        updateMovies();
        return true;
    }

    /**
     * @param movieName
     * @return true if the movie has been removed
     */
    public boolean remMovie(String movieName) {
        BBMovies theMovie = null;
        for (BBMovies currMovie : movies) {
            if (currMovie.getName().equalsIgnoreCase(movieName)) {
                theMovie = currMovie;
                break;

            }
        }
        if (theMovie == null) {
            return false;
        } else {
            if (theMovie.getAvailableAmount() != theMovie.getTotalAmount()) {
                return false;
            } else {
                movies.remove(theMovie);
                updateMovies();
                return true;
            }
        }


    }

    /**
     * @param movieName
     * @param price
     * @return change the price of a movie.
     * return the new movie price or -1 if not success
     */
    public long changePrice(String movieName, String price) {
        BBMovies movieToChange = null;
        boolean movieExists = false;
        for (BBMovies currMovie : movies) {
            if (!movieExists && currMovie.getName().equalsIgnoreCase(movieName)) {
                movieExists = true;
                movieToChange = currMovie;
            }
        }
        if (movieExists) {
            int priceInt = Integer.parseInt(price);
            if (priceInt > 0) {
                movieToChange.setAvailableAmount(priceInt);
                long copiesLeft = movieToChange.getAvailableAmount();
                return copiesLeft;
            }
        }
        updateMovies();
        return -1;
    }


    /**
     * @param list
     * @param soughtFor
     * @return true if the string is in the list ignore case
     */
    public boolean containsIgnoreCase(List<String> list, String soughtFor) {
        for (String current : list) {
            if (current.equalsIgnoreCase(soughtFor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param list
     * @param soughtFor
     * @return true if the string is in the list ignore case.
     */
    public boolean containsMovieIgnoreCase(List<movieOfUser> list, String soughtFor) {
        for (movieOfUser current : list) {
            if (current.getName().equalsIgnoreCase(soughtFor)) {
                return true;
            }
        }
        return false;
    }




    public void removeMovieIgnoreCase(List<movieOfUser> list, String soughtFor) {
        movieOfUser toRemove = null;
        for (movieOfUser current : list) {
            if (current.getName().equalsIgnoreCase(soughtFor)) {
                toRemove = current;
                break;
            }
        }
        if (toRemove != null)
            list.remove(toRemove);
    }

}
