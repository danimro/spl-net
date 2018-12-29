package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.User;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private ConcurrentHashMap<String, User> namesToRegisteredUsers;

    private ConcurrentHashMap<Integer, User> namesToLoginUsers;

    public DataManager() {
        this.namesToRegisteredUsers = new ConcurrentHashMap<>();
        this.namesToLoginUsers = new ConcurrentHashMap<>();
    }

    public User getUserByName(String name){
        return this.namesToRegisteredUsers.get(name);
    }

    public void registerUser(User newUser){
        this.namesToRegisteredUsers.put(newUser.getUserName(), newUser);
    }

    //todo check if can use ReadWriteLock instead of synchronized
    public synchronized void loginUser(User toLogin){
        this.namesToLoginUsers.put(toLogin.getConnId(),toLogin);
    }

    public synchronized void logoutUser(int connId){
        this.namesToLoginUsers.remove(connId);
    }

    public synchronized boolean loginIsEmpty(){
        return this.namesToLoginUsers.isEmpty();
    }

    public User getConnectedUser(int connId){
        return this.namesToLoginUsers.get(connId);
    }

    public List<String> followOrUnfollow(User toCheck,List<String> users,boolean follow){
        List<String> successful = new Vector<>();
        if(follow){
            for(String currentUser:users){
                if(this.namesToRegisteredUsers.contains(currentUser)){
                    if(!toCheck.getFollowing().contains(currentUser)){
                        toCheck.getFollowing().add(currentUser);
                        User toEdit = this.namesToRegisteredUsers.get(currentUser);
                        toEdit.getFollowers().add(toCheck.getUserName());
                        successful.add(currentUser);
                    }
                }
            }
        }
        else{
            //unfollow
            for(String currentUser:users){
                if(this.namesToRegisteredUsers.contains(currentUser)){
                    if(toCheck.getFollowing().contains(currentUser)){
                        toCheck.getFollowing().remove(currentUser);
                        User toEdit = this.namesToRegisteredUsers.get(currentUser);
                        toEdit.getFollowers().remove(toCheck.getUserName());
                        successful.add(currentUser);
                    }
                }
            }
        }
        return successful;

    }








}
