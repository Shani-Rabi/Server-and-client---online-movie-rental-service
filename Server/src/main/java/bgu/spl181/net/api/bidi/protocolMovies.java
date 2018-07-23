package bgu.spl181.net.api.bidi;

/**
 * This class represents the movies' protocol, handles the specific requests related to movies
 */
public class protocolMovies extends Protocolimpl {

    private SharedDataMovies sharedDataMovies;

    String loggedUser;
    boolean isAdmin = false;

    /**
     * @param sharedDataMovies db of movies
     * @param sharedDataUsers db of users
     */
    public protocolMovies(SharedDataMovies sharedDataMovies, SharedDataUsers sharedDataUsers) {
        super(sharedDataUsers);
        this.sharedDataMovies = sharedDataMovies;

    }

    /**
     * @param user
     * @return true check if the user is registered
     */
    public boolean isRegistered(String user) {
        sharedDataMovies.getSharedDataLockUsers().readLock().lock();
        boolean isRegisterd = sharedDataMovies.isRegistered(user);
        sharedDataMovies.getSharedDataLockUsers().readLock().unlock();
        return isRegisterd;

    }

    /**
     * @param newUser
     * adding a new user that registered
     */
    public void addUser(BBUser newUser) {
        sharedDataMovies.getSharedDataLockUsers().writeLock().lock();
        sharedDataMovies.addUser(newUser); // @@ check that add user changed the json
        sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
    }

    /**
     * @param user
     * @param password
     * @return true if it's the user's password
     */
    @Override
    public boolean isGoodPassword(String user, String password) {
        sharedDataMovies.getSharedDataLockUsers().readLock().lock();
        boolean answer = sharedDataMovies.isGoodPassword(user, password);
        sharedDataMovies.getSharedDataLockUsers().readLock().unlock();
        return answer;
    }


    /**
     * @param cutMessage - the command after the word "REQUEST"
     * @param originalMessage
     * mapping every command to its relevant handle function
     */
    public void handleRequest(String cutMessage, String originalMessage) {
        loggedUser = getUserName();
        if (loggedUser != null) // only if the user logged in he can do something
        {
            sharedDataMovies.getSharedDataLockUsers().readLock().lock();
            isAdmin = sharedDataMovies.isAdmin(loggedUser); // checking if the user is admin
            sharedDataMovies.getSharedDataLockUsers().readLock().unlock();
        }

        if (cutMessage.startsWith("balance info")) {
            cutMessage = cutTheFirstWord(cutTheFirstWord(cutMessage));
            handleBalanceInfo(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("balance add")) {
            cutMessage = cutTheFirstWord(cutTheFirstWord(cutMessage));
            handleBalanceAdd(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("info")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleInfo(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("rent")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleRent(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("return")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleReturn(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("addmovie")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleAddMovie(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("remmovie")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleRemMovie(cutMessage, originalMessage);
        } else if (cutMessage.startsWith("changeprice")) {
            cutMessage = cutTheFirstWord(cutMessage);
            handleChangePrice(cutMessage, originalMessage);
        }
    }


    /**
     * @param cutMessage
     * @param originalMessage
     * sends the user's balance
     */
    public void handleBalanceInfo(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockUsers().readLock().lock();
        if (loggedUser != null) {
            long balance = sharedDataMovies.getBalance(loggedUser);
            _connections.send(_ownerOfProtocol, "ACK balance " + balance);
        } else
            _connections.send(_ownerOfProtocol, "ERROR request balance failed");
        sharedDataMovies.getSharedDataLockUsers().readLock().unlock();

    }

    /**
     * @param cutMessage
     * @param originalMessage
     * add money to the user balace
     */
    public void handleBalanceAdd(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockUsers().writeLock().lock();
        if (loggedUser != null) {
            String stringAmountToAdd = getFirstWord(cutMessage);
            int intAmountToAdd = Integer.parseInt(stringAmountToAdd);
            long newBalance = sharedDataMovies.addToBalance(intAmountToAdd, loggedUser);
            _connections.send(_ownerOfProtocol, "ACK balance " + newBalance + " added " + intAmountToAdd);
        } else
            _connections.send(_ownerOfProtocol, "ERROR request balance failed");
        sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
    }

    /**
     * @param cutMessage
     * @param originalMessage
     * sends the movie information. If no specific movie was requested, sends all the movies
     */
    public void handleInfo(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockMovies().readLock().lock();
        String movieName;
        if (loggedUser != null) {
            movieName = getMovieNameFromString(cutMessage);
            if (movieName.isEmpty()) {
                String allMoviesNames = sharedDataMovies.getAllMovieNames();
                _connections.send(_ownerOfProtocol, "ACK info " + allMoviesNames);
            } else {
                String movieInfo = sharedDataMovies.getMovieInfo(movieName);
                if (movieInfo != null)
                    _connections.send(_ownerOfProtocol, "ACK info " + movieInfo);
                else
                    _connections.send(_ownerOfProtocol, "ERROR info failed");
            }
        } else
            _connections.send(_ownerOfProtocol, "ERROR info failed");
        sharedDataMovies.getSharedDataLockMovies().readLock().unlock();


    }

    /**
     * @param cutMessage
     * @param originalMessage
     * add the movie to the user's rented movies
     */
    public void handleRent(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockUsers().writeLock().lock();
        sharedDataMovies.getSharedDataLockMovies().writeLock().lock();
        String movieName;
        if (loggedUser != null) {
            movieName = getMovieNameFromString(cutMessage);
            long moviePrice = sharedDataMovies.getMoviePrice(movieName);
            long rentedMovie = sharedDataMovies.handleRent(loggedUser, movieName); // if the user can rent, it would return the copies left. else -1
            if (rentedMovie >= 0) {
                _connections.send(_ownerOfProtocol, "ACK rent " + '"' + movieName + '"' + " success");
                sendToAllLogin("BROADCAST movie " + '"' + movieName + '"' + " " + rentedMovie + " " + moviePrice);
                sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
                sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
                return;
            }
        }
        _connections.send(_ownerOfProtocol, "ERROR request rent failed");
        sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
        sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
    }


    /**
     * @param cutMessage
     * @param originalMessage
     * remove the movie from the user's rented movies
     */
    public void handleReturn(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockUsers().writeLock().lock();
        sharedDataMovies.getSharedDataLockMovies().writeLock().lock();
        String movieName;
        if (loggedUser != null) {
            movieName = getMovieNameFromString(cutMessage);
            long returnedMovie = sharedDataMovies.handleReturn(loggedUser, movieName); // if the user succeded return, the function would send the copies left. else -1
            long moviePrice = sharedDataMovies.getMoviePrice(movieName);
            if (returnedMovie >= 0) {

                _connections.send(_ownerOfProtocol, "ACK return " + '"' + movieName + '"' + " success");
                sendToAllLogin("BROADCAST movie " + '"' + movieName + '"' + " " + returnedMovie + " " + moviePrice);
                sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
                sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
                return;
            }
        }
        _connections.send(_ownerOfProtocol, "ERROR request return failed");
        sharedDataMovies.getSharedDataLockUsers().writeLock().unlock();
        sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();

    }

    /**
     * @param cutMessage
     * @param originalMessage
     * adds the movie to the movies db
     */
    public void handleAddMovie(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockMovies().writeLock().lock();
        boolean addedMovie;
        if (loggedUser != null && isAdmin) {
            String movieName = getMovieNameFromString(cutMessage);
            cutMessage = cutThemovieName(cutMessage);
            String amount = getFirstWord(cutMessage);
            cutMessage = cutTheFirstWord(cutMessage);
            String price = getFirstWord(cutMessage);
            cutMessage = cutTheFirstWord(cutMessage);
            String bannedCountries = cutMessage;
            addedMovie = sharedDataMovies.addMovie(movieName, amount, price, bannedCountries);
            if (addedMovie) {
                _connections.send(_ownerOfProtocol, "ACK addmovie " + '"' + movieName + '"' + " success");
                sendToAllLogin("BROADCAST movie " + '"' + movieName + '"' + " " + amount + " " + price);
                sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
                return;
            }
        }

        _connections.send(_ownerOfProtocol, "ERROR request addmovie failed");
        sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
    }

    /**
     * @param cutMessage
     * @param originalMessage
     * remove the movie from the movies db
     */
    public void handleRemMovie(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockMovies().writeLock().lock();
        boolean removedMovie;
        if (loggedUser != null && isAdmin) {
            String movieName = getMovieNameFromString(cutMessage);
            removedMovie = sharedDataMovies.remMovie(movieName);
            if (removedMovie) {
                _connections.send(_ownerOfProtocol, "ACK remmovie " + '"' + movieName + '"' + " success");
                sendToAllLogin("BROADCAST movie " + '"' + movieName + '"' + " removed");
                sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
                return;
            }

        }

        _connections.send(_ownerOfProtocol, "ERROR request remmovie failed");
        sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
    }


    /**
     * @param cutMessage
     * @param originalMessage
     * changes the movie's price
     */
    public void handleChangePrice(String cutMessage, String originalMessage) {
        sharedDataMovies.getSharedDataLockMovies().writeLock().lock();
        if (loggedUser != null && isAdmin) {
            String movieName = getMovieNameFromString(cutMessage);
            cutMessage = cutThemovieName(cutMessage);
            String price = getFirstWord(cutMessage);
            long changedPrice = sharedDataMovies.changePrice(movieName, price);
            if (changedPrice >= 0) {
                _connections.send(_ownerOfProtocol, "ACK changeprice " + '"' + movieName + '"' + " success");
                sendToAllLogin("BROADCAST movie " + '"' + movieName + '"' + " " + changedPrice + " " + price);
                sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
                return;
            }

        }

        _connections.send(_ownerOfProtocol, "ERROR request changeprice failed");
        sharedDataMovies.getSharedDataLockMovies().writeLock().unlock();
    }


    /**
     * @param message
     * @return the movie name without!!! the apostrophes
     *
     */
    public String getMovieNameFromString(String message) {
        String pelet = "";
        if (!message.isEmpty())
            message = message.substring(1);
        while (!message.isEmpty() && message.charAt(0) != '"') {
            pelet = pelet + message.charAt(0);
            message = message.substring(1);
        }
        if (!message.isEmpty())
            message = message.substring(1);
        while (!message.isEmpty() && message.charAt(0) == ' ') {
            message = message.substring(1);
        }
        return pelet;
    }

    /**
     * @param message
     * @return the message without the movie name in its begining
     */
    public String cutThemovieName(String message) {
        message = message.substring(1);
        while (!message.isEmpty() && message.charAt(0) != '"') {
            message = message.substring(1);
        }
        message = message.substring(1);
        while (!message.isEmpty() && message.charAt(0) == ' ') {
            message = message.substring(1);
        }
        return message;
    }
}
