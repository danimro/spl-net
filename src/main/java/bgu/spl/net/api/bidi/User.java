package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.Message;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    private int connId;

    private String userName;

    private String password;

    private List<String> following;

    private List<String> followers;

    private List<String> sentMessages;

    private volatile boolean isConnected;

    private ConcurrentLinkedQueue<Message> waitingMessages;

    public User(int connId, String userName, String password) {
        this.connId = connId;
        this.userName = userName;
        this.password = password;
        this.isConnected = false;
        this.following = new Vector<>();
        this.followers = new Vector<>();
        this.sentMessages = new Vector<>();
        this.waitingMessages = new ConcurrentLinkedQueue<>();
    }

    public int getConnId() {
        return connId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getFollowing() {
        return following;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public ConcurrentLinkedQueue<Message> getWaitingMessages() {
        return waitingMessages;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }


}
