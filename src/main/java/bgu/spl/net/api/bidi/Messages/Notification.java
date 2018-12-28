package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;

public class Notification extends Message {

    private char privateMessageOrPublicPost;

    private String postingUser;

    private String content;

    public Notification(char privateMessageOrPublicPost, String postingUser, String content) {
        this.opcode = Opcode.NOTIFICATION;
        this.privateMessageOrPublicPost = privateMessageOrPublicPost;
        this.postingUser = postingUser;
        this.content = content;

    }

    @Override
    public byte[] convertMessageToBytes() {
        byte[] opcode = this.shortToBytes(this.opcode.getCode());
        int toConvert = (int)this.privateMessageOrPublicPost;
        byte privateMessageOrPublicPostBytes = (byte)toConvert;
        byte[] postingUserBytes = this.postingUser.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = this.content.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[opcode.length + 1 + postingUserBytes.length + contentBytes.length + 2];
        int index = 0;
        index = insertArray(opcode,output,index);
        output[index] = privateMessageOrPublicPostBytes;
        index++;
        index = insertArray(postingUserBytes,output,index);
        output[index] = 0;
        index++;
        index = insertArray(contentBytes,output,index);
        output[index] = 0;
        return output;
    }
}
