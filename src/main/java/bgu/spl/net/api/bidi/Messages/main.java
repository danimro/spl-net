package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class main {
    public static void main(String[] args) {
        User user1 = new User("a1","b",1);
        User user2 = new User("a2","b",2);
        User user3 = new User("a3","b",3);
        Vector<User> users = new Vector<>();
        users.add(user2);
        users.add(user3);
        users.add(user1);
        Collections.sort(users);
        for (User user:users) {
            System.out.println(user);
        }

    }
}
