package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;
import bgu.spl.net.api.bidi.Messages.Error;
import bgu.spl.net.srv.bidi.DataManager;

import java.util.List;
import java.util.Vector;

public class BidiMessageProtocolImpl implements BidiMessagingProtocol<Message>  {

    private boolean shouldTerminate;

    private Connections<Message> connections;

    private DataManager dataManager;

    private int connectionID;

    public BidiMessageProtocolImpl(DataManager dataManager) {
        this.dataManager = dataManager;
        this.shouldTerminate = false;
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connectionID = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(Message message) {
        final Message  msg = message;
        Runnable currentProcess;
        if(msg.getOpcode() == Message.Opcode.REGISTER){
            currentProcess = () -> registerFunction((Register)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.LOGIN){
            currentProcess = () -> loginFunction((Login)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.LOGOUT){
            currentProcess = () -> logoutFunction((Logout)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.FOLLOW){
            currentProcess = () -> followFunction((Follow)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.POST){
            currentProcess = () -> postFunction((Post)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.PM){
            currentProcess = () -> pmFunction((PM)msg);
        }
        else if (msg.getOpcode() == Message.Opcode.USERLIST){
            currentProcess = () -> userListFunction((UserList)msg);
        }
        else{
            //stat message
            currentProcess = () -> statFunction((Stat)msg);
        }

        currentProcess.run();

    }

    @Override
    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }

    private void registerFunction (Register registerMsg){
        if(this.dataManager.getUserByName(registerMsg.getUsername()) != null){
            //if the user is already registered - return error message.
            this.connections.send(this.connectionID,new Error(registerMsg.getOpcode()));
        }
        else{
            this.dataManager.registerUser(registerMsg.getUsername(),registerMsg.getPassword());
            this.connections.send(this.connectionID,registerMsg.generateAckMessage(new Object[0]));
        }
    }

    private void loginFunction (Login loginMsg){
        User toCheck = this.dataManager.getUserByName(loginMsg.getUsername());
        if((toCheck == null) || (!toCheck.getPassword().equals(loginMsg.getPassword())) || (toCheck.isConnected())){
            //if the user is already registered \ password doesnt match \ is already connected --> return error message.
            this.connections.send(this.connectionID,new Error(loginMsg.getOpcode()));
        }
        else{

            synchronized (toCheck.getWaitingMessages()){
                for(Message current: toCheck.getWaitingMessages()){
                    //sending to the user all the messages that were waiting for him\her
                    this.connections.send(toCheck.getConnId(),current);
                }
                //setting the connection value to true
                toCheck.login(this.connectionID);
                this.dataManager.loginUser(toCheck);

            }
            this.connections.send(connectionID,loginMsg.generateAckMessage(new Object[0]));
        }

    }
    private void logoutFunction (Logout logoutMsg){
        if(this.dataManager.loginIsEmpty()){
            this.connections.send(this.connectionID,new Error(logoutMsg.getOpcode()));
        }
        else{

            this.dataManager.logoutUser(this.connectionID);
            this.connections.send(this.connectionID,logoutMsg.generateAckMessage(new Object[0]));
            this.connections.disconnect(this.connectionID);
        }
    }
    private void followFunction (Follow followMsg){
        User toCheck = this.dataManager.getConnectedUser(this.connectionID);
        if(toCheck == null){
            this.connections.send(this.connectionID,new Error(followMsg.getOpcode()));
        }
        else{
            List<String> successful = this.dataManager.followOrUnfollow(toCheck,followMsg.getUsers(),followMsg.isFollowing());
            if(successful.isEmpty()){
                //if no one of the requested users were followed \ unfollowed successfully --> send error
                this.connections.send(this.connectionID,new Error(followMsg.getOpcode()));
            }
            else{
                short amount = (short)successful.size();
                Object[] elementOfAckMsg = {amount,successful};
                this.connections.send(this.connectionID,followMsg.generateAckMessage(elementOfAckMsg));
            }
        }

    }
    private void postFunction (Post postMsg){

        User sender = this.dataManager.getConnectedUser(this.connectionID);
        if(sender == null){
            //the user is not logged in --> send error message
            this.connections.send(this.connectionID,new Error(postMsg.getOpcode()));
        }
        else{
            //if the user is logged in
            List<User> users = new Vector<>();
            searchingForUsersInMessage(postMsg, sender, users);
            //adding all the followers of the sender to the list
            users.addAll(sender.getFollowers());
            Notification toSend = new Notification((byte)'1',sender.getUserName(),postMsg.getContent());
            for(User currentUser:users){
                //send notification to each user
                if(currentUser.isConnected()){
                    //if the user is connected --> send the notification
                    this.dataManager.sendNotification(this.connections,currentUser.getConnId(),toSend);
                }
                else{
                    //else --> send the message to the waiting queue of that user.
                    currentUser.getWaitingMessages().add(toSend);
                }
            }
            this.connections.send(this.connectionID,postMsg.generateAckMessage(new Object[0]));
        }
    }

    private void searchingForUsersInMessage(Post postMsg, User sender, List<User> users) {
        String[] contentWords = postMsg.getContent().split(" ");
        for (String contentWord : contentWords) {
            if (contentWord.contains("@")) {
                //need to search the tagged user
                String currentUserName = contentWord.substring(contentWord.indexOf("@"));
                User currentUser = this.dataManager.getUserByName(currentUserName);
                if (currentUser != null) {
                    //only if the Current tagged user is registered
                    if (!sender.getFollowers().contains(currentUser)) {
                        //only if the current user is not following the sender already
                        users.add(currentUser);
                    }
                }
            }
        }
    }

    private void pmFunction (PM pmMsg){
        User sender = this.dataManager.getConnectedUser(this.connectionID);
        User recipient = this.dataManager.getUserByName(pmMsg.getUserName());
        if((sender == null) ||(recipient == null)){
            //the user is not logged in --> send error message
            this.connections.send(this.connectionID,new Error(pmMsg.getOpcode()));
        }
        else{
            Notification toSend = new Notification((byte)'\0',sender.getUserName(),pmMsg.getContent());
            this.dataManager.sendNotification(this.connections,recipient.getConnId(),toSend);
        }
        this.connections.send(this.connectionID, pmMsg.generateAckMessage(new Object[0]));
    }
    private void userListFunction(UserList userListMsg){
        User user = this.dataManager.getConnectedUser(this.connectionID);
        if(user == null){
            this.connections.send(this.connectionID,new Error(userListMsg.getOpcode()));
        }
        else{
            List<String> registeredUsers = this.dataManager.returnRegisteredUsers();
            Object[] elements = {(short)registeredUsers.size(),registeredUsers};
            Message toSend = userListMsg.generateAckMessage(elements);
            this.connections.send(this.connectionID,toSend);
        }
    }
    private void statFunction (Stat statMsg){
        User user = this.dataManager.getUserByName(statMsg.getUsername());
        if((user == null)|| (!user.isConnected())){
            this.connections.send(this.connectionID, new Error(statMsg.getOpcode()));
        }
        else{
            short following = (short)user.getFollowing().size();
            short followers = (short)user.getFollowers().size();
            short numberOfPosts = this.dataManager.returnNumberOfPosts(user.getUserName());
            Object[] elements = {numberOfPosts, followers, following};
            this.connections.send(this.connectionID, statMsg.generateAckMessage(elements));
        }

    }
}
