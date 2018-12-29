package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Messages.*;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;


public class BidiMessageEncoderDecoder implements MessageEncoderDecoder<Message> {

    private byte[] opcodeBytes;

    private int opcodeInsertedLength;

    private byte[] field1;

    private byte[] field2;

    private int field1Index;

    private int field2Index;

    private int zeroCounter;

    private byte followByte;


    private Message.Opcode currentOpcode;

    public BidiMessageEncoderDecoder() {
        this.opcodeBytes = new byte[2];
        this.opcodeInsertedLength = 0;
        this.currentOpcode = null;
        this.field1 = new byte[10];
        this.field2 = new byte[10];
        this.field1Index = 0;
        this.field2Index = 0;
        this.zeroCounter = 0;
        this.followByte = 0;

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
                    generalVariablesReset();
                    return new Logout();
                }
                else{
                    generalVariablesReset();
                    return new UserList();
                }
            }
            return null;
        }
        else{
            return readingMessage(nextByte);
        }
    }

    private Message readingMessage(byte nextByte) {
        Message output;
        if(this.currentOpcode == Message.Opcode.REGISTER){
            output = readingRegisterOrLoginMessage(Message.Opcode.REGISTER,nextByte);
        }
        else if(this.currentOpcode == Message.Opcode.LOGIN){
            output = readingRegisterOrLoginMessage(Message.Opcode.LOGIN,nextByte);
        }
        else if(this.currentOpcode == Message.Opcode.FOLLOW){
            output = readingFollowMessage(nextByte);
        }
        else if(this.currentOpcode == Message.Opcode.POST){
            output = readingPostMessage(nextByte);
        }
        else if(this.currentOpcode == Message.Opcode.PM){
            output = readingPMMessage(nextByte);
        }
        else if(this.currentOpcode == Message.Opcode.STAT){
            output = readingStatMessage(nextByte);
        }
        else{
            output = null;
        }
        return output;
    }

    private Message readingStatMessage(byte nextByte) {
        // field1 = username
        Message output;
        insertByteToField1(nextByte);
        if(nextByte == '\0'){
            checkReduceField1();
            String username = new String(this.field1, StandardCharsets.UTF_8);
            output = new Stat(username);
            this.generalVariablesReset();
        }
        else{
            return null;
        }
        return output;
    }


    private Message readingPMMessage(byte nextByte) {
        //field1 = username   | field2 = content

        Message output;
        if(this.zeroCounter == 0){
            //inserting to username
            insertByteToField1(nextByte);
            if(nextByte == '\0'){
                this.zeroCounter++;
            }
            return null;
        }
        else{
            //inserting content
            insertByteToField2(nextByte);
            if(nextByte == '\0'){
                output = generatePMMessage();
                this.generalVariablesReset();
            }
            else{
                return null;
            }
        }
        return output;
    }


    private Message generatePMMessage() {
        Message output;//finished reading
        checkReduceField1();
        checkReduceField2();
        String username = new String(this.field1, StandardCharsets.UTF_8);
        String content = new String(this.field2,StandardCharsets.UTF_8);
        output = new PM(username,content);
        return output;
    }

    private Message readingPostMessage(byte nextByte) {
        //field1 = content
        Message output;
        this.insertByteToField1(nextByte);
        if(nextByte == '\0'){
            //finished reading the message
            checkReduceField1();
            String content = new String(this.field1, StandardCharsets.UTF_8);
            output = new Post(content);
            this.generalVariablesReset();
        }
        else{
            return null;
        }
        return output;
    }



    private Message readingFollowMessage(byte nextByte){
        //field1 = numberOfUsers  | field2 = usernameList | followbyte = follow\unfollow | zerocounter = bytesCounter
        this.field1 = new byte[2];
        if(this.zeroCounter == 0){
            this.followByte = nextByte;
            this.zeroCounter++;
            return null;
        }
        else if((this.zeroCounter > 0) && (this.zeroCounter < 3)){
            insertToNumberOfUsers(nextByte);
            return null;
        }
        else{
            int numberOfUsers = Message.bytesToShort(this.field1);
            insertByteToField2(nextByte);
            if(nextByte == '\0'){
                this.zeroCounter++;
                //to reduce the first three bytes of the follow\unfollow and numberOfUsers bytes
                if(this.zeroCounter-3 == numberOfUsers){
                    return generateFollowMessage(numberOfUsers);
                }
                else{
                    return null;
                }
            }
            else{
                return null;
            }
        }
    }

    private void insertToNumberOfUsers(byte nextByte){
        if(this.zeroCounter == 1){
            this.field1[0] = nextByte;
            this.zeroCounter++;
        }
        else{
            this.field1[1] = nextByte;
            this.zeroCounter++;
        }
    }

    private Message generateFollowMessage(int numberOfUsers) {
        Message output;//collected all the necessary users --> convert them to string and generate a FollowMessage
        checkReduceField2();
        Vector<String> allUsers = new Vector<>();
        int start = 0;
        for(int i = 0; i < field2Index; i++){
            if(field2[i] == '\0'){
                //finished a single user;
                byte[] userByte = Arrays.copyOfRange(field2,start,i);
                String user = new String (userByte,StandardCharsets.UTF_8);
                allUsers.add(user);
                start = i + 1;
            }

        }
        output = new Follow(this.followByte, (short)numberOfUsers, allUsers);
        generalVariablesReset();
        return output;
    }

    private Message readingRegisterOrLoginMessage(Message.Opcode outputOpcode, byte nextByte) {
        //Field1 = username | Field2 = password
        if(this.zeroCounter == 0){
            insertByteToField1(nextByte);
            //the next byte going to be to the userName
            if(nextByte == '\0'){
                checkReduceField1();
                this.zeroCounter++;
                return null;
            }
            return null;
        }
        else{
            insertByteToField2(nextByte);
            if(nextByte == '\0'){
                checkReduceField2();
                //creating the Register or Login Message
                return generateRegisterOrLoginMessage(outputOpcode);
            }
            return null;
        }
    }

    private Message generateRegisterOrLoginMessage(Message.Opcode outputOpcode) {
        Message output;
        String username = new String(this.field1, StandardCharsets.UTF_8);
        String password = new String(this.field2, StandardCharsets.UTF_8);
        if(outputOpcode == Message.Opcode.REGISTER){
             output = new Register(username, password);
        }
        else{
            output = new Login(username, password);
        }
        generalVariablesReset();
        return output;
    }

    private void generalVariablesReset() {
        this.opcodeBytes = new byte[2];
        this.currentOpcode = null;
        this.field1 = new byte[10];
        this.field2 = new byte[10];
        this.field1Index = 0;
        this.field2Index = 0;
        this.zeroCounter = 0;
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
        for(int i = 0; i < size; i++){
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

    private void insertByteToField1(byte nextByte) {
        this.field1[this.field1Index] = nextByte;
        this.field1Index++;
        if(this.field1Index == this.field1.length){
            this.field1 = extendArray(this.field1);
        }
    }

    private void insertByteToField2(byte nextByte) {
        this.field2[this.field2Index] = nextByte;
        this.field2Index++;
        if(this.field2Index == this.field2.length){
            this.field2 = extendArray(this.field2);
        }
    }

    private void checkReduceField1() {
        if(this.field1Index!=this.field1.length){
            this.field1 = reduceToGivenSize(this.field1,this.field1Index);
        }
    }

    private void checkReduceField2() {
        if(this.field2Index!=this.field2.length){
            this.field2 = reduceToGivenSize(this.field2,this.field2Index);
        }
    }


}
