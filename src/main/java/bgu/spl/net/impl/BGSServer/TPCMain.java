package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.Server;

import java.util.function.Supplier;

public class TPCMain {
    public static void main(String[] args) {
        Server<String> threadPerClientServer = Server.threadPerClient(Integer.parseInt(args[0]), new Supplier<BidiMessagingProtocol<String>>() {
                    @Override
                    public BidiMessagingProtocol<String> get() {
                        return new EchoProtocol();
                    }
                },
                LineMessageEncoderDecoder::new);

        threadPerClientServer.serve();
    }
}
