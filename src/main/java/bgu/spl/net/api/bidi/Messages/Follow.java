package bgu.spl.net.api.bidi.Messages;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Follow extends Message {

    private boolean isFollowing;

    private int numberOfUsers;

    private List<String> users;



    public Follow(byte isFollowing, int numberOfUsers, String users) {
        this.opcode = Opcode.FOLLOW;
        if(isFollowing == 0){
            this.isFollowing = true;
        }
        else{
            this.isFollowing = false;
        }
        this.numberOfUsers = numberOfUsers;
        this.users = new Vector<>();
        String[] usersArray = users.split("0");
        this.users.addAll(Arrays.asList(usersArray));

    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public List<String> getUsers() {
        return users;
    }

    @Override
    public byte[] convertMessageToBytes() {
        byte[] opcode = this.shortToBytes(this.opcode.getCode());
        byte[] isFollowingBytes = new byte[1];
        if(isFollowing){
            isFollowingBytes[0] = 0;
        }
        else{
            isFollowingBytes[0] = 1;
        }
        byte[] numberOfUsersBytes = this.shortToBytes((short)this.numberOfUsers);
        Vector<byte[]> usersNames = new Vector<>();
        int totalBytesOfUsers = 0;
        for (String currentName:this.users) {
            byte[] currentNameArr = currentName.getBytes(StandardCharsets.UTF_8);
            usersNames.add(currentNameArr);
            totalBytesOfUsers += currentNameArr.length;
        }
        byte[] output = new byte[opcode.length + isFollowingBytes.length +
                numberOfUsersBytes.length + totalBytesOfUsers + this.numberOfUsers];
        int index = 0;
        index = insertArray(opcode,output,index);
        index = insertArray(isFollowingBytes,output,index);
        index = insertArray(numberOfUsersBytes,output,index);
        for (byte[] currentUser:usersNames) {
            index = insertArray(currentUser,output,index);
            output[index] = 0;
            index++;
        }
        return output;
    }

    @Override
    public Ack generateAckMessage(Object[] messageElements) {
        if(messageElements.length != 3){
            throw new IllegalArgumentException("Follow_Message-generateAckMessage : was expecting 3 element in the array");
        }
        else{
            byte[] numberOfUsers = this.shortToBytes((short)messageElements[0]);
            String usersList = (String)messageElements[1];
            String[] users = usersList.split("0");
            int lengthOfUsers = (int)messageElements[0];
            byte[][] elements = new byte[numberOfUsers.length + (lengthOfUsers*2)][];
            int index = 0;
            elements[index] = numberOfUsers;
            index++;
            for(int i = 0; i < users.length;i++){
                byte[] toInsert = users[i].getBytes(StandardCharsets.UTF_8);
                elements[index] = toInsert;
                index++;
                byte[] separator = {0};
                elements[index] = separator;
                index++;
            }
            Ack output = new Ack(this.opcode, elements);
            return output;
        }

    }
}
