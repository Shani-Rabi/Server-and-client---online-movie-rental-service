package bgu.spl181.net.api.bidi;


/**
 * represents the test protocol implementation
 */
public abstract class Protocolimpl implements BidiMessagingProtocol<String> {
    protected Connections<String> _connections;
    protected int _ownerOfProtocol;
    private boolean _shouldTerminate = false;
    private boolean isClientLogedIn = false;
    private String LogUser = null;
    private SharedDataUsers sharedDataUsers;

    public Protocolimpl(SharedDataUsers sharedDataUsers) {
        this.sharedDataUsers = sharedDataUsers;
    }


    /**
     * @param connectionId - the client id that is connected
     * @param connections - the connections of the server
     */
    public void start(int connectionId, Connections<String> connections) {
        _connections = connections;
        _ownerOfProtocol = connectionId;
    }

    /**
     * @param message - the message the client sent
     * call to the relevant function to handle the function
     */
    public void process(String message) {
        String originalMessage = message;
        String firstWord;
        firstWord = getFirstWord(message).toUpperCase();
        message=cutTheFirstWord(message);
        if (firstWord.equals("REGISTER"))
           handleRegister(message);
        else if (firstWord.equals("LOGIN")) {
            handleLogin(message);
        }
        else if (firstWord.equals("SIGNOUT"))
            handleSignout();
        else if (firstWord.equals("REQUEST"))
            handleRequest(message, originalMessage);




    }

    /**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate() {
        return _shouldTerminate;

    }

    /**
     * @param inputString - some string
     * @return the message without its first word
     */
    public String cutTheFirstWord(String inputString) {
        while (inputString.length() > 0 && inputString.charAt(0) != ' ') {
            inputString = inputString.substring(1); // removes the first letter
        }
        while (inputString.length() > 0 && inputString.charAt(0) == ' ')
            inputString = inputString.substring(1);

        return inputString; // returns the first word in the string;
    }

    /**
     * @param inputString - some string
     * @return the first word in the string
     */
    public String getFirstWord(String inputString) {
        String firstWord = "";
        while (inputString.length() > 0 && inputString.charAt(0) != ' ') {
            firstWord = firstWord + inputString.charAt(0);
            inputString = inputString.substring(1); // removes the first letter
        }
        while (inputString.length() > 0 && inputString.charAt(0) == ' ')// remove spaces
            inputString = inputString.substring(1);

        return firstWord; // returns the first word in the string

    }

    public abstract boolean isRegistered(String user) ;

    public boolean isLoggedIn(String user){
        return sharedDataUsers.isLoggedIn(user);
    }

    public abstract void addUser(BBUser newUser);


    public abstract boolean isGoodPassword(String user, String password) ;


    public void sendToAllLogin(String message){
        sharedDataUsers.sendToAllLogin(message,_connections);
    }


    public void addConnectionId(String userName, int connectionId) {
        sharedDataUsers.addConnectionId(userName, connectionId);
    }

    public void removeConnectionId(String userName) {
        sharedDataUsers.removeConnectionId(userName);
    }

    public int getConnectionIdOf(String userName) {
        return sharedDataUsers.getConncetionIdOf(userName);
    }


    /**
     * @param message
     * this method get a message of register.
     * try to register .
     * if success send a ACK message.
     * if not send a ERROR message.
     */
    public void handleRegister(String message) {
        BBUser newUser;
        if (!message.isEmpty()) {//if message empty -false
            String userToRegister = getFirstWord(message);
            message=cutTheFirstWord(message);
            if (!isRegistered(userToRegister) && !message.isEmpty()) {
                String userPassword = getFirstWord(message);
                message=cutTheFirstWord(message);
                if (!message.isEmpty() && message.startsWith("country=")) {
                    while (message.charAt(0) != '"')
                        message = message.substring(1);
                    message = message.substring(1);
                    String userCountry = "";
                    while (message.length()>0&&message.charAt(0) != '"') {
                        userCountry = userCountry + message.charAt(0);
                        message = message.substring(1);
                    }
                    message = message.substring(1);
                    if (message.isEmpty()) {
                        newUser = new BBUser(userToRegister, userPassword, userCountry);
                        addUser(newUser);
                        _connections.send(_ownerOfProtocol, "ACK registration succeeded");
                    } else {

                        _connections.send(_ownerOfProtocol,  "ERROR registration failed");
                    }
                } else {
                    if (message.isEmpty()) {
                        newUser = new BBUser(userToRegister, userPassword, null);
                        addUser(newUser);
                        _connections.send(_ownerOfProtocol, "ACK registration succeeded");
                    } else {
                        _connections.send(_ownerOfProtocol,  "ERROR registration failed");
                    }
                }

            } else {
                _connections.send(_ownerOfProtocol, "ERROR registration failed");
            }

        } else {
            _connections.send(_ownerOfProtocol, "ERROR registration failed");
        }
    }


    /**
     * @param message
     *    this method get a message of login.
     * try to register .
     * if success send a ACK message.
     * if not send a ERROR message.
     */
    public void handleLogin(String message) {
        String Output;
        if (!message.isEmpty()) {//if message empty -false
            String userToLogin = getFirstWord(message);
            message=cutTheFirstWord(message);
            if (!isClientLogedIn && !message.isEmpty() && !sharedDataUsers.isLoggedIn(userToLogin)) {
                String thePassword = getFirstWord(message);
                message=cutTheFirstWord(message);
                if (isGoodPassword(userToLogin, thePassword)) {
                    isClientLogedIn = true;
                    LogUser = userToLogin;
                    addConnectionId(userToLogin,_ownerOfProtocol);
                    _connections.send(_ownerOfProtocol, "ACK login succeeded");
                } else {
                    _connections.send(_ownerOfProtocol, "ERROR login failed");
                }

            } else {
                _connections.send(_ownerOfProtocol, "ERROR login failed");
            }

        } else {
            _connections.send(_ownerOfProtocol, "ERROR login failed");
        }


    }

    /**
     *    this method get a message of logout.
     * try to register .
     * if success send a ACK message.
     * if not send a ERROR message.
     */
    public void handleSignout() {
            if (isClientLogedIn) {
                isClientLogedIn = false;
                _shouldTerminate = true;
                removeConnectionId(LogUser);
                LogUser = null;
                _connections.send(_ownerOfProtocol,  "ACK signout succeeded");
                _connections.disconnect(_ownerOfProtocol);
            } else {
                _connections.send(_ownerOfProtocol, "ACK signout failed");
            }

    }

    public abstract void handleRequest(String message, String OriginalMessage);

    /**
     * @return the user name that the client logged with. if it didn't logged with any one it returns null
     */
    protected String getUserName() {
        return LogUser;
    }

    /**
     * @return the connections of the server
     */
    protected Connections<String> getConnections() {
        return _connections;
    }
}

