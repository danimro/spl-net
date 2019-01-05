package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessageProtocolImpl;
import bgu.spl.net.api.bidi.Messages.Message;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.DataManager;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReactorMain {
    public static void main(String[] args) {
        DataManager dataManager = new DataManager();
        ReadWriteLock logOrSendLock = new ReentrantReadWriteLock(true);
        ReadWriteLock registerOrUserList = new ReentrantReadWriteLock(true);
        Server<Message> reactorServer = Server.reactor(Integer.parseInt(args[0]),Integer.parseInt(args[1]),
                () -> new BidiMessageProtocolImpl(dataManager,logOrSendLock,registerOrUserList),
                BidiMessageEncoderDecoder::new);

        reactorServer.serve();
    }
}
