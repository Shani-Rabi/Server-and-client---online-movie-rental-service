package bgu.spl181.net.api.bidi;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * this class hold the state of the users in the Blockbuster
 */
public class SharedDataUsers {
    ConcurrentHashMap<String, Integer> userNameConnectionId = new ConcurrentHashMap<>();
    private final ReadWriteLock sharedDataLockMap = new ReentrantReadWriteLock();


    /**
     * @param message
     * @param connections
     * send the message to all the log in users.
     */
    public void sendToAllLogin(String message,Connections<String> connections){
        sharedDataLockMap.writeLock().lock();
        for (Map.Entry<String, Integer> currUser : userNameConnectionId.entrySet()){
            connections.send(currUser.getValue(),message);
        }
        sharedDataLockMap.writeLock().unlock();
    }

    /**
     * @param userName
     * @return the connection id of the username
     */
    public Integer getConncetionIdOf(String userName) {
        sharedDataLockMap.readLock().lock();
        for (Map.Entry<String, Integer> currUser : userNameConnectionId.entrySet()) {
            if (currUser.getKey().equalsIgnoreCase(userName)) {
                sharedDataLockMap.readLock().unlock();
                return currUser.getValue();
            }
        }
        sharedDataLockMap.readLock().unlock();
        return null;
    }

    /**
     * @param userName
     * @param connectionId
     * add a new field to the map.
     */
    public void addConnectionId(String userName, int connectionId) {
        sharedDataLockMap.writeLock().lock();
        userNameConnectionId.putIfAbsent(userName, connectionId);
        sharedDataLockMap.writeLock().unlock();
    }

    /**
     * @param userName
     * remove a field from the map.
     */
    public void removeConnectionId(String userName) {
        sharedDataLockMap.writeLock().lock();
        Map.Entry<String, Integer> userToRemove=null;
        for (Map.Entry<String, Integer> currUser : userNameConnectionId.entrySet()) {
            if (currUser.getKey().equalsIgnoreCase(userName)) {
                userToRemove =  currUser;
                break;
            }
        }
        userNameConnectionId.remove(userToRemove.getKey());
        sharedDataLockMap.writeLock().unlock();
    }




    /**
     * @param user
     * @return true if the user is log in.
     */
    public boolean isLoggedIn(String user) {
        sharedDataLockMap.readLock().lock();
        for (Map.Entry<String, Integer> currUser : userNameConnectionId.entrySet()) {
            if (currUser.getKey().equalsIgnoreCase(user)) {
                sharedDataLockMap.readLock().unlock();
                return true;
            }
        }
        sharedDataLockMap.readLock().unlock();
        return false;
    }


}


