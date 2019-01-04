package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessageProtocolImpl;
import bgu.spl.net.api.bidi.Messages.Message;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.DataManager;

public class ReactorMain {
    public static void main(String[] args) {
        DataManager dataManager = new DataManager();
        Server<Message> reactorServer = Server.reactor(Integer.parseInt(args[0]),Integer.parseInt(args[1]),
                () -> new BidiMessageProtocolImpl(dataManager),
                BidiMessageEncoderDecoder::new);

        reactorServer.serve();
    }
}
