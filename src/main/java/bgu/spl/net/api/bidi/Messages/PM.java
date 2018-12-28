package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;

public class PM extends Message {

    private String userName;

    private String content;

    public PM(String userName, String content) {
        this.opcode = Opcode.PM;
        this.userName = userName;
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public byte[] convertMessageToBytes() {
        byte[] opcode = this.shortToBytes(this.opcode.getCode());
        byte[] usernameBytes = this.userName.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = this.content.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[opcode.length+usernameBytes.length+contentBytes.length+2];
        int index = 0;
        index = insertArray(opcode,output,index);
        index = insertArray(usernameBytes,output,index);
        output[index] = 0;
        index++;
        index = insertArray(contentBytes,output,index);
        output[index] = 0;
        return output;

    }
}
