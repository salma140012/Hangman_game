import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PhrasesDatabase {
	
	 private String filename;
	 private static ArrayList<String> words = new ArrayList<>();
	 
	 public PhrasesDatabase(String filename) {
	        this.filename = filename;
	    }
	  public ArrayList<String> getWords() throws IOException {
	        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                words.add(line);
	                
	            }
	            return words;
	        }
	    }
}
