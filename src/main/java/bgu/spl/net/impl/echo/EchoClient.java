package bgu.spl.net.impl.echo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {

    public static void main(String[] args) throws IOException {

        Scanner s = new Scanner(System.in);
        if (args.length == 0) {
            args = new String[]{"localhost", "habibi supreme"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        try (Socket sock = new Socket(args[0], 7777);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
            String input = "y";
            do{
                input = s.nextLine();
                System.out.println("sending message to server");
                out.write(input);
                out.newLine();
                out.flush();

                System.out.println("awaiting response");
                String line = in.readLine();
                System.out.println("message from server: " + line);
            }while(!input.equals("bye"));

        }
    }
}
