package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.time.LocalDateTime;

public class EchoProtocol implements BidiMessagingProtocol<String> {

    private Connections<String> connections;
    private int connectionID;

    public EchoProtocol() {
        this.shouldTerminate = false;
    }


    private boolean shouldTerminate = false;

    @Override
    public void process(String msg) {
        shouldTerminate = "bye".equals(msg);
        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        this.connections.send(this.connectionID,createEcho(msg));
    }

    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = connections;
        this.connectionID = connectionId;
    }
}
