package bgu.spl.net.api.bidi.Messages;

public class Ack extends Message {

    private Opcode resolvedOpcode;

    byte[][] messageElements;

    public Ack(Opcode resolvedOpcode , byte[][] numberOfElements) {
        this.opcode = Opcode.ACK;
        this.resolvedOpcode = resolvedOpcode;
        this.messageElements = numberOfElements;
    }

    public Opcode getResolvedOpcode() {
        return resolvedOpcode;
    }

    public byte[][] getMessageElements() {
        return messageElements;
    }

    @Override
    public byte[] convertMessageToBytes() {
        //ack opcode
        byte[] ackOpcode = this.shortToBytes(this.opcode.getCode());
        //resolved opcode
        byte[] resolvedOpcode = this.shortToBytes(this.resolvedOpcode.getCode());
        int numberOfBytes = 0;

        if(this.messageElements != null){
            //if this ack code contains more than "ACK + number" --> add it to the ack message
            for(int i = 0; i < messageElements.length;i++){
                //for each element in the additional message elements --> add it to the output array
                numberOfBytes += this.messageElements[i].length;
            }
        }
        byte[] output = new byte[ackOpcode.length + resolvedOpcode.length + numberOfBytes];
        int index = 0;
        index = insertArray(ackOpcode,output,index);
        index = insertArray(resolvedOpcode,output,index);
        if(this.messageElements != null){
            for(int i = 0; i < messageElements.length;i++){
                index = insertArray(this.messageElements[i],output,index);
            }
        }
        return output;
    }
    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        return null;
    }
}
