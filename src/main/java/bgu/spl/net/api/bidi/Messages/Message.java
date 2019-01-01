package bgu.spl.net.api.bidi.Messages;


public abstract class Message {

    protected Opcode opcode;

    public Opcode getOpcode() {
        return opcode;
    }

    public abstract byte[] convertMessageToBytes();

    public enum Opcode {
        REGISTER,
        LOGIN,
        LOGOUT,
        FOLLOW,
        POST,
        PM,
        USERLIST,
        STAT,
        NOTIFICATION,
        ACK,
        ERROR;

        public short getCode(){
            if(this == Opcode.REGISTER){
               return 1;
            }
            else if(this == Opcode.LOGIN){
                return 2;
            }
            else if(this == Opcode.LOGOUT){
                return 3;
            }
            else if(this == Opcode.FOLLOW){
                return 4;
            }
            else if(this == Opcode.POST){
                return 5;
            }
            else if(this == Opcode.PM){
                return 6;
            }
            else if(this == Opcode.USERLIST){
                return 7;
            }
            else if(this == Opcode.STAT){
                return 8;
            }
            else if(this == Opcode.NOTIFICATION){
                return 9;
            }
            else if(this == Opcode.ACK){
                return 10;
            }
            else{
                //error message
                return 11;
            }


        }

    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    public static short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public static Opcode convertToOpcode(short code){
        if(code == 1){
            return Opcode.REGISTER;
        }
        else if(code == 2){
            return Opcode.LOGIN;
        }
        else if(code == 3){
            return Opcode.LOGOUT;
        }
        else if(code == 4){
            return Opcode.FOLLOW;
        }
        else if(code == 5){
            return Opcode.POST;
        }
        else if(code == 6){
            return Opcode.PM;
        }
        else if(code == 7){
            return Opcode.USERLIST;
        }
        else if(code == 8){
            return Opcode.STAT;
        }
        else if(code == 9){
            return Opcode.NOTIFICATION;
        }
        else if(code == 10){
            return Opcode.ACK;
        }
        else if(code == 11){
            return Opcode.ERROR;
        }
        else {
            return null;
        }

    }

    protected int insertArray(byte[] array, byte[] output, int index) {
        for (int i = 0; i < array.length; i++) {
            output[index] = array[i];
            index++;
        }
        return index;
    }

    public Ack generateAckMessage(Object[] messageElements){
        return new Ack(this.opcode,new byte[0][0]);
    }

}
