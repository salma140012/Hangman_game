import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoresDatabase {
	
	 private static String filename;
	
	 
	 public ScoresDatabase(String filename) {
	        ScoresDatabase.filename = filename;
	    }
	 
	 public void setNewPlayerScore(String username) throws IOException {
		        PrintWriter write = new PrintWriter(new FileWriter(filename, true));
	            write.println(username + ",0");
	            write.close();
	            }
	 public String getPlayerScore(String username) throws IOException {
		 String score = "";
		 try {
			BufferedReader read = new BufferedReader(new FileReader(filename));
			 String line;
			 
			  while ((line = read.readLine()) != null) {
	                String[] parts = line.split(",");
	                if (parts[0].equals(username)) {
	                    score=parts[1];
	                }
	              
	            }
			  read.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		 
		 return score;
	 }

	public static String updatePlayerScore(String username, int score) {
	        try {
	        	File inputFile = new File("scores.txt");
	            File tempFile = new File("temp.txt");

	            BufferedReader bfR = new BufferedReader(new FileReader(inputFile));
	            PrintWriter write = new PrintWriter(new FileWriter(tempFile));

	            String line;
	            while ((line = bfR.readLine()) != null) {
	                String[] parts = line.split(",");
	                if (parts[0].equals(username)) {
	                    line = username + "," + score;
	                }
	                write.println(line);
	            }

	            bfR.close();
	            write.close();

	            inputFile.delete();
	            tempFile.renameTo(inputFile);
	            
	          
	           
	        } catch (IOException e) {
	            System.out.println("An error occurred while updating the score: " + e.getMessage());
	        }
	        return "Your account's score is updated to " + score + ".";
	    }

	 public static String writeIndividualScoreHistory(String username, String gameType, String result, int score) {
	        String filename = username + ".txt";
	        try {
	        	
	            PrintWriter writer = new PrintWriter(new FileWriter(filename, true));
	            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	            writer.println("time:"+timestamp + ",type:" + gameType + "," + result + ",Score:" + score);
	            writer.close();
	        } catch(IOException ex) {
	            System.out.println("Error writing to file '" + filename + "'");
	            ex.printStackTrace();
	        }
	        return "This game has been added to your history.";
	    }
	 public static List<String> getScoreHistory(String username) {
	        List<String> history = new ArrayList<>();
	        String filename = username + ".txt";
	        try {
	            BufferedReader bfR = new BufferedReader(new FileReader(filename));
	            String line;
	            while ((line = bfR.readLine()) != null) {
	                history.add(line);
	            }
	            bfR.close();
	        } catch(IOException ex) {
	            System.out.println("Error reading from file '" + filename + "'");
	            ex.printStackTrace();
	        }
	        return history;
	    }
	}


