package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;

public class Stat extends Message{

    private String username;

    public Stat(String username) {
        this.opcode = Opcode.STAT;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public byte[] convertMessageToBytes() {
        byte[] opcode = this.shortToBytes(this.opcode.getCode());
        byte[] userNameBytes = this.username.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[opcode.length + userNameBytes.length + 1];
        int index = 0;
        index = insertArray(opcode,output,index);
        index = insertArray(userNameBytes,output,index);
        output[index] = 0;
        return output;
    }

    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        if(messageElements.length != 3){
            throw new IllegalArgumentException("Stat_Message-generateAck: expected 3 elements got : "+messageElements.length);
        }
        else{
            byte[] numberOfPosts = this.shortToBytes((short)messageElements[0]);
            byte[] numberOfFollowers = this.shortToBytes((short)messageElements[1]);
            byte[] numberOfFollowing = this.shortToBytes((short)messageElements[2]);
            byte[][] elements = new byte[3][];
            elements[0] = numberOfPosts;
            elements[1] = numberOfFollowers;
            elements[2] = numberOfFollowing;
            return new Ack(this.opcode,elements);
        }
    }
}
