import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 1234;
    private static final String CREDENTIALS_FILE = "credentials.txt";
    private static final String SCORES_FILE = "scores.txt";
    private static final String PHRASES_FILE = "phrases.txt";
 

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        // Load credentials from file
        CredentialsDatabase credentials = new CredentialsDatabase(CREDENTIALS_FILE);
        
        // Load scores from file
        ScoresDatabase scores = new ScoresDatabase(SCORES_FILE);
     
        // Load phrases from file
        PhrasesDatabase  phrases= new PhrasesDatabase(PHRASES_FILE);

     // An infinite loop to continuously listen for incoming client connections
        while (true) {
            // Accepting an incoming connection request and creating a new socket object to communicate with the client
            Socket clientSocket = serverSocket.accept();
            
            //Printing to the console the address of client socket to indicate having a new connection
            System.out.println("New connection from " + clientSocket.getRemoteSocketAddress());

            // Creating a new PlayerHandler object with the client socket, credentials, scores, and phrases passed in as arguments
            PlayerHandler playerHandler = new PlayerHandler(clientSocket, credentials, scores, phrases);

            // Create a new thread with the PlayerHandler object as the target since it implements runnable and doesn't extend thread directly
            Thread thread = new Thread(playerHandler);

            // Start the new thread, which calls the run() method in PlayerHandler to handle the client request
            thread.start();
        }
    }
}
