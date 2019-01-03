package bgu.spl.net.api.bidi.Messages;

public class Logout extends Message{

    public Logout() {
        this.opcode = Opcode.LOGOUT;
    }

    @Override
    public byte[] convertMessageToBytes() {
        return this.shortToBytes(this.opcode.getCode());
    }

    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        return new Ack(this.opcode,new byte[0][0]);
    }
}
