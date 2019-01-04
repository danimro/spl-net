package bgu.spl.net.api.bidi.Messages;

/**
 * Message Of type LOGOUT of Client-To-Server communication, when a user wants to logout from the server
 */
public class Logout extends Message{

    /**
     * Default Constructor
     */
    public Logout() {
        this.opcode = Opcode.LOGOUT;
    }

    /**
     * Convert all the data of this Logout message to a byte array.
     * @return      Byte array represent this Logout message in the right order according to the server protocol
     */
    @Override
    public byte[] convertMessageToBytes() {
        return this.shortToBytes(this.opcode.getCode());
    }

    /**
     * Generate matching Ack Message to this Logout Message Message according the Message data and server protocol.
     * @param messageElements               Object array of additional elements to the Ack message
     * @return              Ack message matching this Logout Message data of this message according to the server protocol.
     */
    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        return new Ack(this.opcode,new byte[0][0]);
    }
}
