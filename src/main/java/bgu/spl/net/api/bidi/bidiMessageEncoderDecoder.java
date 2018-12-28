package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Messages.*;

import java.nio.charset.StandardCharsets;


public class bidiMessageEncoderDecoder implements MessageEncoderDecoder<Message> {

    private byte[] opcodeBytes;

    private int opcodeInsertedLength;

    //region Register+Login Variables
    //todo check if there is a better solution to the "dynamic array method"
    private byte[] loginOrRegisterUsername;

    private byte[] loginOrRegisterPassword;

    private int lettersInUserName;

    private int lettersInPassword;

    private int zeroCountRegisterLogin;

    //endregion Register+Login Variables

    //region Follow Variables
    private int followBytesCounter;

    private byte followByte;

    private byte[] followNumOfUsers;

    private byte[] followUsers;

    private int followUsersIndex;


    //endregion Follow Variables

    private Message.Opcode currentOpcode;

    public bidiMessageEncoderDecoder() {
        this.opcodeBytes = new byte[2];
        this.opcodeInsertedLength = 0;
        this.currentOpcode = null;
        this.loginOrRegisterUsername = new byte[10];
        this.loginOrRegisterPassword =new byte[10];
        this.zeroCountRegisterLogin = 0;
        this.lettersInPassword = 0;
        this.lettersInUserName = 0;
        this.followBytesCounter = 0;
        this.followNumOfUsers = new byte[2];
        this.followUsers = new byte[10];
        this.followUsersIndex = 0;
    }

    @Override
    public Message decodeNextByte(byte nextByte) {

        if(this.opcodeInsertedLength == 0){
            this.opcodeBytes[0] = nextByte;
            this.opcodeInsertedLength++;
            return null;
        }
        else if(this.opcodeInsertedLength == 1){
            this.opcodeBytes[1] = nextByte;
            this.opcodeInsertedLength++;
            initMessageContentAndLength();
            if((this.currentOpcode == Message.Opcode.LOGOUT) ||
               (this.currentOpcode == Message.Opcode.USERLIST)){
                if(this.currentOpcode== Message.Opcode.LOGOUT){
                    return new Logout();
                }
                else{
                    return new UserList();
                }
                generalVariablesReset();
            }
            return null;
        }
        else{
            Message output;
            if(this.currentOpcode == Message.Opcode.REGISTER){
                output = readingRegisterOrLoginMessage(Message.Opcode.REGISTER,nextByte);
            }
            else if(this.currentOpcode == Message.Opcode.LOGIN){
                output = readingRegisterOrLoginMessage(Message.Opcode.LOGIN,nextByte);
            }

            else if(this.currentOpcode== Message.Opcode.FOLLOW){
                if(this.followBytesCounter == 0){
                    this.followByte = nextByte;
                    this.followBytesCounter++;
                    return null;
                }
                else if(followBytesCounter == 1){
                    this.followNumOfUsers[0] = nextByte;
                    this.followBytesCounter++;
                    return null;
                }
                else if(followBytesCounter == 2){
                    this.followNumOfUsers[1] = nextByte;
                    this.followBytesCounter++;
                    return null;
                }
                else{
                    int numberOfUsers = Message.bytesToShort(this.followNumOfUsers);
                    if(this.followUsers.length == this.followUsersIndex){
                        this.followUsers = extendArray(this.followUsers);
                    }
                    this.followUsers[this.followUsersIndex] = nextByte;
                    this.followUsersIndex++;
                    if(nextByte == 0){
                        this.followBytesCounter++;
                        //to reduce the first three bytes of the follow\unfollow and numberOfUsers bytes
                        if(this.followBytesCounter-3 == numberOfUsers){
                            //collected all the necessary users --> convert them to string and generate a FollowMessage
                            String allUsers = new String(this.followUsers,StandardCharsets.UTF_8);
                            String[] users = allUsers.split("0");

                        }
                    }
                }
            }

            return output;
        }

    }

    private Message readingRegisterOrLoginMessage(Message.Opcode outputOpcode, byte nextByte) {
        if(zeroCountRegisterLogin == 0){
            //the next byte going to be to the userName
            if(nextByte == 0){
                if(this.lettersInUserName != this.loginOrRegisterUsername.length){
                    this.loginOrRegisterUsername = reduceToGivenSize(this.loginOrRegisterUsername,this.lettersInUserName);
                }
                this.zeroCountRegisterLogin++;
                return null;
            }
            if(this.lettersInUserName == this.loginOrRegisterUsername.length){
                this.loginOrRegisterUsername = extendArray(this.loginOrRegisterUsername);
            }
            this.lettersInUserName++;
            this.loginOrRegisterUsername[lettersInUserName] = nextByte;
            return null;
        }
        else{
            if(nextByte == 0){
                if(this.lettersInPassword != this.loginOrRegisterPassword.length){
                    this.loginOrRegisterPassword = this.reduceToGivenSize(this.loginOrRegisterPassword,this.lettersInPassword);
                }
                //creating the Register or Login Message
                return generateRegisterOrLoginMessage(outputOpcode);
            }
            if(this.lettersInPassword == this.loginOrRegisterPassword.length){
                this.loginOrRegisterPassword = extendArray(this.loginOrRegisterPassword);
            }
            this.lettersInPassword++;
            this.loginOrRegisterPassword[lettersInPassword] = nextByte;
            return null;
        }
    }

    private Message generateRegisterOrLoginMessage(Message.Opcode outputOpcode) {
        Message output;
        String username = new String(this.loginOrRegisterUsername, StandardCharsets.UTF_8);
        String password = new String(this.loginOrRegisterPassword, StandardCharsets.UTF_8);

        if(outputOpcode == Message.Opcode.REGISTER){
             output = new Register(username, password);
        }
        else{
            output = new Login(username, password);
        }
        registerOrLoginVariablesReset();
        return output;
    }

    private void registerOrLoginVariablesReset() {
        this.loginOrRegisterUsername = new byte[10];
        this.loginOrRegisterPassword = new byte[10];
        this.zeroCountRegisterLogin = 0;
        this.lettersInUserName = 0;
        this.lettersInPassword = 0;
        generalVariablesReset();
    }

    private void generalVariablesReset() {
        this.opcodeBytes = new byte[2];
        this.currentOpcode = null;
    }


    @Override
    public byte[] encode(Message message) {
        return message.convertMessageToBytes();
    }

    private void initMessageContentAndLength(){
        this.currentOpcode = Message.convertToOpcode(Message.bytesToShort(this.opcodeBytes));

    }

    private byte[] extendArray(byte[] array){
        int size = array.length;
        byte[] temp = new byte[size*2];
        for(int i=0;i<size;i++){
            temp[i] = array[i];
        }
        return temp;
    }

    private byte[] reduceToGivenSize(byte[] toReduce,int realSize){
        byte[] temp = new byte[realSize];
        for(int i = 0; i < realSize;i++){
            temp[i] = toReduce[i];
        }
        return temp;
    }

}
