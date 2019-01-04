package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Message Of type USERLIST of Client-To-Server communication, when a user wants to see all the client that are registered to the server
 */
public class UserList extends Message {

    /**
     * Default Constructor.
     */
    public UserList() {
        this.opcode = Opcode.USERLIST;
    }


    /**
     * Convert all the data of this UserList message to a byte array.
     * @return      Byte array represent this UserList message in the right order according to the server protocol
     */
    @Override
    public byte[] convertMessageToBytes() {
        return new byte[0];
    }

    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        if(messageElements.length != 2){
            //UserList Ack Message must have number of users and a user list as additional parameters.
            throw new IllegalArgumentException("UserList_Message-generateAckMessage: expected 3 elements , got : "+messageElements.length);
        }
        else{
            //converting the number of of users to bytes array.
            byte[] numOfUsersBytes = this.shortToBytes((short)messageElements[0]);
            List<String> list = (List<String>)messageElements[1];
            byte[][] elements = new byte[1 + (2 * list.size())][];
            int index = 0;
            elements[index] = numOfUsersBytes;
            index++;
            for (String user : list) {
                //converting each name in the list to bytes array and add it to elements.
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
