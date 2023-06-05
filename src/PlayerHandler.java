import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class PlayerHandler implements Runnable {

	public static ArrayList<PlayerHandler> playerhandlers = new ArrayList<>();
	private static ArrayList<String> words = new ArrayList<>();
	public static ArrayList<PlayerHandler> team1 = new ArrayList<>();
	public static ArrayList<PlayerHandler> team2 = new ArrayList<>();
	public static ArrayList<PlayerHandler> gameRoom = new ArrayList<>();
	private static boolean multiGameStart = false;
	private boolean loggedIn = false;
	private boolean secondMenuFlag = false;
	private Socket clientSocket;
	private CredentialsDatabase credentials;
	private ScoresDatabase scores;
	private PhrasesDatabase phrases;
	private BufferedReader in;
	private PrintWriter out;
	private String playerUsername;
	private int accountIntialScore;
	private int accountNewScore;
	private int gameScore;
	private int num_attempt;
	private int multigameScore;
	private String teamName;

	public PlayerHandler(Socket clientSocket, CredentialsDatabase credentials, ScoresDatabase scores,
			PhrasesDatabase phrases) {
		try {

			this.clientSocket = clientSocket;
			this.credentials = credentials;
			this.scores = scores;
			this.phrases = phrases;

			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.out = new PrintWriter(clientSocket.getOutputStream(), true); // true for the automatic flush

			playerhandlers.add(this);

		} catch (IOException e) {
			closeEverything(clientSocket, in, out);
		}
	}

	@Override
	public void run() {
		while (clientSocket.isConnected()) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				out.println("Welcome to our Hangman game...You can enter (-) at anytime to exit!");
				while (true) {
					// Ask the player whether they want to login or register
					while (!this.loggedIn) {

						firstMenu();
						out.println("--------------------------");
					}
                   //display second menu to player
					while (secondMenuFlag) {
						secondMenu();

					}
					playerhandlers.remove(this);
					clientSocket.close();
				}
			} catch (IOException e) {
				closeEverything(clientSocket, in, out);
				break;
			}
		}
	}

	private void firstMenu() throws IOException {

		out.println("1-Login");
		out.println("2-Register");
		out.println("Please enter your choice!");

		String action = in.readLine();
		if (action.equals("1")) {
			// Handle login
			handleLogin(in, out);
		} else if (action.equals("2")) {
			// Handle registration
			handleRegistration(in, out);

		}

	}

	private void secondMenu() throws IOException {

		out.println("1-Single Player");
		out.println("2-Multi Player");
		out.println("3-View history");
		out.println("4-Logout");
		out.println("--------------------------");
		String action = in.readLine();
		if (action.equals("1")) {
			// Single player game
			singlePlayer(in, out);
		} else if (action.equals("2")) {
			// Multiplayer game
			/*
			 * team1.clear(); team2.clear(); gameRoom.clear();
			 * out.println("Please enter a team name."); String teamName; teamName =
			 * in.readLine();
			 */
			PlayerHandler player = this;
			if (team1.size() <= team2.size()) {
				team1.add(player);
				player.setTeamName("Team 1");
				out.println("You have been added to Team 1");
				out.println("Waiting for game to start....");
				secondMenuFlag = false;
			} else {
				team2.add(player);
				out.println("You have been added to Team 2");
				out.println("Waiting for game to start....");
				player.setTeamName("Team 2");
				secondMenuFlag = false;

			}

			words = phrases.getWords();
			while (!multiGameStart) {
				if (team1.size() == 2 && team2.size() == 2) {
					gameRoom.addAll(team1);
					gameRoom.addAll(team2);
					out.println("Game starting...");
					MultiPlayer multi = new MultiPlayer(team1, team2, gameRoom, words);
					multi.playMultiplayerHangman();
					multiGameStart = true;

				}
			}
			secondMenuFlag = true;

		} else if (action.equals("3")) {

			List<String> history = ScoresDatabase.getScoreHistory(playerUsername);
			for (String line : history) {
				out.println(line);
			}
			out.println("--------------------------");
			secondMenu();
		} else if (action.equals("4")) {
			firstMenu();

		} else {
			out.println("Invalid action.");
		}

	}

	public String getPlayerUsername() {
		return playerUsername;
	}

	public PrintWriter getPrintWriter() {
		return out;
	}

	public BufferedReader getIn() {
		return in;
	}

	public String getRandomWord() throws IOException {
		words = phrases.getWords();

		// pick one word from our list of words between 0 and size of list
		Random rand = new Random();
		String word = words.get(rand.nextInt(words.size()));
		return word;
	}

	private void singlePlayer(BufferedReader in, PrintWriter out) throws IOException {
		num_attempt = 6;
		gameScore = 0;
		accountIntialScore = Integer.parseInt(scores.getPlayerScore(playerUsername));

		out.println("--------------------------");
		out.println("Your account currently has a total score of: " + accountIntialScore);
		out.println("Game Score: " + gameScore);
		out.println("Your game is starting now!");
		out.println("This is your word..now start guessing!");

		// pick one word from our list of words between 0 and size of list
		String word = getRandomWord();

		// out.println(word);

		List<Character> guessesMade = new ArrayList<>();

		printWordProgress(word, guessesMade);
		while (true) {

			boolean guessExists = addGuess(word, guessesMade); // add guess asks for a letter and checks it and then
																// printword progress prints current progress

			calcScore(gameScore, guessExists);
			out.println("Game Score: " + gameScore);
			if (printWordProgress(word, guessesMade)) {
				out.println("\nYou win!");
				// out.println("Game Score: "+gameScore);
				accountNewScore = accountIntialScore + gameScore;
				out.println(ScoresDatabase.updatePlayerScore(playerUsername, accountNewScore));
				out.println(ScoresDatabase.writeIndividualScoreHistory(playerUsername, "single", "win", gameScore));
				break;
			}
			if (num_attempt == 0) {
				if (accountIntialScore - 10 < 0) {
					accountNewScore = 0;
				}
				accountNewScore = accountIntialScore - 10; // just minusing 10 points from overall accounts score if
															// player loses
				out.println("\nYou're all out of guess attempts.Better Luck next time!");
				out.println(ScoresDatabase.updatePlayerScore(playerUsername, accountNewScore));
				out.println(ScoresDatabase.writeIndividualScoreHistory(playerUsername, "single", "lose", gameScore));
				out.println("Your word was: " + word);
				break;

			}
		}
		out.println("\nDo you wanna play again? Press 1 to continue/ 0 to go to previous menu.");
		String decision = in.readLine();
		if (decision.equals("1")) {
			singlePlayer(in, out);
		} else {
			secondMenuFlag = true;
		}
	}

	private void calcScore(int score, boolean guessExists) {

		if (guessExists) {
			gameScore += 2;
		} else if (!guessExists && gameScore != 0) {
			gameScore -= 1;
		}

	}

	private boolean printWordProgress(String word, List<Character> guessesMade) {
		int correctGuessCount = 0;
		for (int i = 0; i < word.length(); i++) {
			char c = Character.toLowerCase(word.charAt(i)); // converting to lowercase to make guesses not case
															// sensitive
			if (guessesMade.contains(Character.toLowerCase(c))) {
				out.print(word.charAt(i));
				correctGuessCount++;
			} else {
				out.println("_ ");
			}
		}
		return (word.length() == correctGuessCount);
	}

	private boolean addGuess(String word, List<Character> guessesMade) throws IOException {
		out.println("\n\nPlease enter a letter: ");
		String guess = in.readLine();

		// adding only the first character of whatever the user entered
		// in case they entered more than just one character
		char c = Character.toLowerCase(guess.charAt(0)); // converting to lowercase to make guesses not case sensitive
		guessesMade.add(c);

		// checking the guess to minus from number of attempts available for player
		boolean guessExists = checkGuess(word, c);

		return guessExists;
	}

	private boolean checkGuess(String word, Character c) {
		boolean exists = false;
		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == c) {
				exists = true;
				break;
			}
		}
		if (exists) {
			num_attempt = num_attempt;
			drawHangman();
			out.println("**************************");
			out.println("Number of guesses left: " + num_attempt);
		} else {
			num_attempt -= 1;
			drawHangman();
			out.println("**************************");
			out.println("Number of guesses left: " + num_attempt);
		}
		return exists;
	}

	public void drawHangman() {
		if (num_attempt == 6) {
			out.println("|----------");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
		} else if (num_attempt == 5) {
			out.println("|----------");
			out.println("|    O");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
		} else if (num_attempt == 4) {
			out.println("|----------");
			out.println("|    O");
			out.println("|    |");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
		} else if (num_attempt == 3) {
			out.println("|----------");
			out.println("|    O");
			out.println("|   -|");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
		} else if (num_attempt == 2) {
			out.println("|----------");
			out.println("|    O");
			out.println("|   -|-");
			out.println("|");
			out.println("|");
			out.println("|");
			out.println("|");
		} else if (num_attempt == 1) {
			out.println("|----------");
			out.println("|    O");
			out.println("|   -|-");
			out.println("|   /");
			out.println("|");
			out.println("|");
			out.println("|");
		} else {
			out.println("|----------");
			out.println("|    O");
			out.println("|   -|-");
			out.println("|   / \\");
			out.println("|");
			out.println("|");
			out.println("|");
		}
	}

	private void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
		// Read username and password from client
		out.println("Enter username:");
		playerUsername = in.readLine();
		out.println("Enter password:");
		String password = in.readLine();

		// Check if username and password are valid

		String ret = credentials.validLogin(playerUsername, password);
		if (ret.equals("Logged in successfully!!")) {
			out.println("Glad to see you again..." + this.playerUsername);
			this.loggedIn = true;
			this.secondMenuFlag = true;

		}

		out.println(ret);

	}

	private void handleRegistration(BufferedReader in, PrintWriter out) throws IOException {
		// Read name, username, and password from client
		out.println("Enter name:");
		String name = in.readLine();
		out.println("Enter username:");
		String username = in.readLine();
		out.println("Enter password:");
		String password = in.readLine();

		// Try to register the user
		if (credentials.registerUser(name, username, password)) {
			scores.setNewPlayerScore(username);
			out.println("Registration successful.");
		} else {
			out.println("Username already taken.");
		}
	}

	public void removePlayerHandler() {
		playerhandlers.remove(this);

	}

	public void setMultiGameScore(int multigameScore) {
		this.multigameScore = multigameScore;
	}

	public int getMultiGameScore() {
		return multigameScore;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void closeEverything(Socket socket, BufferedReader bufferedreader, PrintWriter writer) {
		removePlayerHandler();
		try {
			if (bufferedreader != null) {
				bufferedreader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
}
