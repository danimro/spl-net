package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;

public class BidiMessageProtocolImpl implements BidiMessagingProtocol<Message>  {

    private boolean shouldTerminate;

    //private final HashMap<Class<? extends Message>,MessageRunnable> messagesToRunnables = createMessagesToRunnables();



    //private static HashMap<Class<? extends Message>,MessageRunnable> createMessagesToRunnables(){
        //HashMap<Class<? extends Message>,MessageRunnable> temp = new HashMap<>();
        //temp.put(Register.class, new MessageRunnable() {
    //}

    @Override
    public void start(int connectionId, Connections<Message> connections) {

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
            currentProcess = () -> userlistFunction((UserList)msg);
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

    private Message registerFunction (Register registerMsg){

    }

    private Message loginFunction (Login loginMsg){

    }
    private Message logoutFunction (Logout logoutMsg){

    }
    private Message followFunction (Follow followMsg){

    }
    private Message postFunction (Post postMsg){

    }
    private Message pmFunction (PM pmMsg){

    }
    private Message userlistFunction ( UserList userlistMsg){

    }
    private Message statFunction (Stat statMsg){

    }
}
