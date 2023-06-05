import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class CredentialsDatabase {
    private String filename;
    private Object lock = new Object();

    public CredentialsDatabase(String filename) {
        this.filename = filename;
    }

    public synchronized String validLogin(String username, String password) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 3 && fields[1].equals(username) && fields[2].equals(password)) {
                    return "Logged in successfully!!";
                }
                else if (fields.length == 3 && fields[1].equals(username) && !fields[2].equals(password)) {
                    return "ERROR 401 (UNAUTHORIZED)!!";
                }
                
            }
            return "ERROR 404 (NOT FOUND)";
        }
    }

    public synchronized boolean registerUser(String name, String username, String password) throws IOException {
    synchronized(lock) { try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length == 3 && fields[1].equals(username)) {
                // Username already taken
                return false;
            }
        }
    }}

    // Add the new user to the file
    synchronized (lock) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println(name + "," + username + "," + password);
        }
    }

    return true;
}
}