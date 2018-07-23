package bgu.spl181.net.api.bidi;

import bgu.spl181.net.srv.bidi.ConnectionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @param <T> this class will hold all the connections in the system.
 */
public class ConnectionsImplement<T> implements  Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> map = new ConcurrentHashMap<>(); // holds all the clients that connected to the server


    /**
     * @param connectionId
     * @param msg
     * @return true if the messege has benn sent to the specific connection id,or false if not.
     */
    public boolean send(int connectionId, T msg) {
        ConnectionHandler <T> theConnectionHandler = map.get(connectionId);
        if (theConnectionHandler != null) {
            theConnectionHandler.send(msg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param msg this method send message to all the connections in this class.
     */
    public void broadcast(T msg)
    {
    for(Map.Entry <Integer,ConnectionHandler<T>> temp: map.entrySet()){
        temp.getValue().send(msg);
    }
}

    /**
     * @param connectionId this  method remove a specific connection from the list.
     */
    public void disconnect(int connectionId){
        map.remove(connectionId);
    }

    /**
     * @param theConnection
     * @param numOfConnection
     * this  method add a specific connection to the list.
     */
    public void addConnection(ConnectionHandler<T> theConnection,Integer numOfConnection){
        map.putIfAbsent(numOfConnection,theConnection);
    }
}
