package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;

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
            String list = (String)messageElements[1];
            String[] userList = list.split("0");
            byte[][] elements = new byte[numOfUsersBytes.length+(2*userList.length)][];
            int index = 0;
            elements[index] = numOfUsersBytes;
            for(int i = 0; i < userList.length; i++){
                byte[] currentUser = userList[i].getBytes(StandardCharsets.UTF_8);
                elements[index] = currentUser;
                index++;
                byte[] separator ={0};
                elements[index] = separator;
                index++;
            }
            return new Ack(this.opcode, elements);
        }
    }
}
