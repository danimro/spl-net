package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserList extends Message {

    public UserList() {
        this.opcode = Opcode.USERLIST;
    }


    @Override
    public byte[] convertMessageToBytes() {
        return new byte[0];
    }

    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        if(messageElements.length != 2){
            throw new IllegalArgumentException("UserList_Message-generateAckMessage: expected 3 elements , got : "+messageElements.length);
        }
        else{
            byte[] numOfUsersBytes = this.shortToBytes((short)messageElements[0]);
            List<String> list = (List<String>)messageElements[1];
            byte[][] elements = new byte[numOfUsersBytes.length+(2 * list.size())][];
            int index = 0;
            elements[index] = numOfUsersBytes;
            for (String user : list) {
                byte[] currentUser = user.getBytes(StandardCharsets.UTF_8);
                elements[index] = currentUser;
                index++;
                byte[] separator = {'\0'};
                elements[index] = separator;
                index++;
            }
            return new Ack(this.opcode, elements);

        }
    }
}
