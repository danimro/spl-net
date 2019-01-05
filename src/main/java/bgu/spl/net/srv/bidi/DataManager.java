package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Messages.Message;
import bgu.spl.net.api.bidi.Messages.Notification;
import bgu.spl.net.api.bidi.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataManager {

    private AtomicInteger numberOfUsers;

    private ConcurrentHashMap<String, User> namesToRegisteredUsers;

    private ConcurrentHashMap<Integer, User> namesToLoginUsers;

    private List<Notification> messageHistory;

    private ReadWriteLock sendOrLogLock;

    private Lock sendLock;

    private Lock logLock;

    private ReadWriteLock registerOrUserListLock;

    private Lock userListLock;

    private Lock registerLock;

    public DataManager() {
        this.namesToRegisteredUsers = new ConcurrentHashMap<>();
        this.namesToLoginUsers = new ConcurrentHashMap<>();
        this.sendOrLogLock = new ReentrantReadWriteLock(true);
        this.sendLock = this.sendOrLogLock.readLock();
        this.logLock = this.sendOrLogLock.writeLock();
        this.registerOrUserListLock = new ReentrantReadWriteLock(true);
        this.userListLock = this.registerOrUserListLock.readLock();
        this.registerLock = this.registerOrUserListLock.writeLock();
        this.numberOfUsers = new AtomicInteger(0);
        this.messageHistory = new Vector<>();
    }

    public User getUserByName(String name){
        return this.namesToRegisteredUsers.get(name);
    }

    public void registerUser(String userName, String password){
        this.registerLock.lock();
        int userNumber = this.generateUserNumber();
        User newUser = new User(userName,password,userNumber);
        this.namesToRegisteredUsers.put(userName, newUser);
        this.registerLock.unlock();
    }


    public void loginUser(User toLogin){
        this.logLock.lock();
        this.namesToLoginUsers.put(toLogin.getConnId(),toLogin);
        this.logLock.unlock();
    }

    public void logoutUser(int connId){
        this.logLock.lock();
        this.namesToLoginUsers.get(connId).logout();
        this.namesToLoginUsers.remove(connId);
        this.logLock.unlock();
    }

    public boolean loginIsEmpty(){
        return this.namesToLoginUsers.isEmpty();
    }

    public User getConnectedUser(int connId){
        return this.namesToLoginUsers.get(connId);
    }

    public List<String> followOrUnfollow(User toCheck,List<String> users,boolean follow){
        List<String> successful = new Vector<>();
        if(follow){
            for(String currentUser:users){
                User current = this.namesToRegisteredUsers.get(currentUser);
                if(current != null){
                    synchronized (toCheck){
                        if(!toCheck.getFollowing().contains(current)){
                            toCheck.getFollowing().add(current);
                            current.getFollowers().add(this.namesToRegisteredUsers.get(toCheck.getUserName()));
                            successful.add(currentUser);
                        }
                    }
                }
            }
        }
        else{
            //unfollow
            for(String currentUser:users){
                User current = this.namesToRegisteredUsers.get(currentUser);
                if(current != null){
                    synchronized (toCheck){
                        if(toCheck.getFollowing().contains(current)){
                            toCheck.getFollowing().remove(current);
                            current.getFollowers().remove(toCheck);
                            successful.add(currentUser);
                        }
                    }

                }
            }
        }
        return successful;

    }

    public void addToHistory(Notification toSave){
        this.messageHistory.add(toSave);
    }

    public void sendNotification(Connections<Message> connections, int connectionID, Notification toSend){
        this.sendLock.lock();
        connections.send(connectionID,toSend);
        this.sendLock.unlock();
    }

    private int generateUserNumber(){
        return this.numberOfUsers.getAndIncrement();
    }

    public List<String> returnRegisteredUsers(){
        this.userListLock.lock();
        List<User> users = new Vector<>(this.namesToRegisteredUsers.values());
        Collections.sort(users);
        List<String> registeredUsers = new Vector<>();
        for (User user:users) {
            registeredUsers.add(user.getUserName());
        }
        this.userListLock.unlock();
        return registeredUsers;
    }

    public short returnNumberOfPosts(String postingUser){
        short output = 0;
        for(Notification msg:messageHistory){
            if((msg.getPrivateMessageOrPublicPost() == 1) && (msg.getPostingUser().equals(postingUser))){
                output++;
            }
        }
        return output;
    }

}
