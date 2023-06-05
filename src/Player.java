import java.io.*;
import java.net.*;

public class Player {
   

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

      

        // create a thread to handle incoming messages
        Thread responeThread = new Thread(() -> {
            String serverRespone;
            try {
                while ((serverRespone = in.readLine()) != null) {
                  
                    if(serverRespone.contains("_")) {
                    	  System.out.print(serverRespone);
                    }
    
                    else {
                    	 System.out.println(serverRespone);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        responeThread.start(); // start the thread

        // handling of outgoing messages
        String playerReply;
        while (socket.isConnected() && !(playerReply = reader.readLine()).equals("-")) {
            out.println(playerReply);
        }

        // close the socket and exit
        socket.close();
        System.out.println("The game is exiting... see you again soon!!");
    }
}