package bgu.spl.net.api.bidi.Messages;

public class main {
    public static void main(String[] args) {
        String s = "g0y";
        String[] sA = s.split("0");
        for(int i =0; i < sA.length;i++){
            if(!sA[i].equals("")){
                System.out.println(sA[i]);
            }

        }
    }
}
