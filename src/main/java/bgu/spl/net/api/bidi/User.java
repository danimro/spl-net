package bgu.spl.net.api.bidi;
import bgu.spl.net.api.bidi.Messages.Message;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User implements Comparable<User>{


    private static final int DISCONNECTED_ID = -1;

    private int userNum;

    private int connId;

    private String userName;

    private String password;

    private Set<User> following;

    private Set<User> followers;

    private volatile boolean isConnected;

    private ConcurrentLinkedQueue<Message> waitingMessages;

    public User(String userName, String password, int userNum) {
        this.connId = DISCONNECTED_ID;
        this.userName = userName;
        this.password = password;
        this.isConnected = false;
        this.following = new HashSet<>();
        this.followers = new HashSet<>();
        this.waitingMessages = new ConcurrentLinkedQueue<>();
        this.userNum = userNum;
    }

    public int getConnId() {
        return connId;
    }

    public int getUserNum() {
        return userNum;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public ConcurrentLinkedQueue<Message> getWaitingMessages() {
        return waitingMessages;
    }

    public void logout(){
        this.isConnected = false;
        this.connId = DISCONNECTED_ID;
    }

    public void login(int connId){
        this.isConnected = true;
        this.connId = connId;
    }

    @Override
    public int compareTo(User user) {
        return Integer.compare(this.userNum, user.userNum);
    }

}
