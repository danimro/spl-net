package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;
import bgu.spl.net.api.bidi.Messages.Error;
import bgu.spl.net.srv.bidi.DataManager;

import java.util.List;

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
            User newUser = new User(this.connectionID,registerMsg.getUsername(),registerMsg.getPassword());
            this.dataManager.registerUser(newUser);
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
                this.dataManager.loginUser(toCheck);
                toCheck.setConnected(true);
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
                //if no one of the requested users were followed\unfollowed successfully --> send error
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

    }
    private void pmFunction (PM pmMsg){

    }
    private void userListFunction(UserList userlistMsg){

    }
    private void statFunction (Stat statMsg){

    }
}
