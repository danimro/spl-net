package bgu.spl.net.api.bidi.Messages;

public class Error extends Message {


    private Opcode errorMessageOpcode;

    public Error(Opcode errorMessageOpcode) {
        this.opcode = Opcode.ERROR;
        this.errorMessageOpcode = errorMessageOpcode;
    }

    @Override
    public byte[] convertMessageToBytes() {
        byte[] opcode = this.shortToBytes(this.opcode.getCode());
        byte[] errorOpcode = this.shortToBytes(this.errorMessageOpcode.getCode());
        byte[] output = new byte[opcode.length + errorOpcode.length];
        int index = 0;
        index = insertArray(opcode,output,index);
        index = insertArray(errorOpcode,output,index);
        return output;
    }
}
