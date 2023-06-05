import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiPlayer {
	ScoresDatabase scores;
	int numAttemptsTeam1 = 6;
	int numAttemptsTeam2 = 6;
	public static ArrayList<PlayerHandler> gameRoom = new ArrayList<>();
	public static ArrayList<PlayerHandler> team1 = new ArrayList<>();
	public static ArrayList<PlayerHandler> team2 = new ArrayList<>();
	private static ArrayList<String> words = new ArrayList<>();
	String word;

	MultiPlayer(ArrayList<PlayerHandler> team1, ArrayList<PlayerHandler> team2, ArrayList<PlayerHandler> gameRoom,
			ArrayList<String> words) {
		this.team1 = team1;
		this.team2 = team2;
		this.gameRoom = gameRoom;
		this.words = words;
	}

	public void playMultiplayerHangman() throws IOException {
		// Initialize game variables
		Random rand = new Random();
		String word = words.get(rand.nextInt(words.size()));
		boolean gameEnd = false;
		List<Character> guessesMade = new ArrayList<>();

		for (PlayerHandler player : gameRoom) {
			player.setMultiGameScore(0);
		}
		
		int currentPlayerIndex = 0;
		int currentTeamIndex = 0;
		PlayerHandler currentPlayer = gameRoom.get(currentPlayerIndex);

		// Start game loop
		while (!gameEnd) {
			
			// Print game status to all players
			for (PlayerHandler player : gameRoom) {
				PrintWriter playerOut = player.getPrintWriter();
				playerOut.println("\n---------------------------------------------\n");
				printWordProgress(word, guessesMade, playerOut);
				playerOut.println("\n\nNumber of attempts remaining for Team 1: " + numAttemptsTeam1);
				playerOut.println("Number of attempts remaining for Team 2: " + numAttemptsTeam2);
				for (PlayerHandler opponent : gameRoom) {

					playerOut.println(opponent.getPlayerUsername() + "'s score: " + opponent.getMultiGameScore());

				}
				playerOut.println("It's " + currentPlayer.getPlayerUsername() + "'s turn.");
			}

			// Get guess from current player
			// Update game variables based on guess
			boolean guessExists = addGuess(word, guessesMade, currentPlayer, currentTeamIndex);

			if (guessExists) {
				currentPlayer.setMultiGameScore(currentPlayer.getMultiGameScore() + 2);
			} else {
				if (numAttemptsTeam1 == 0 || numAttemptsTeam2 == 0) {
                    gameEnd = true;
                } else if(currentPlayer.getMultiGameScore() > 0) {
                    currentPlayer.setMultiGameScore(currentPlayer.getMultiGameScore() - 1);
                }
			}

			gameEnd = wordGuessed(word, guessesMade);

			if (!gameEnd) {
				// Move on to next player's turn

				if (currentPlayerIndex == 0) {
					currentPlayerIndex = 2; // switch to team2's turn
					currentTeamIndex = 1;
				} else if (currentPlayerIndex == 2) {
					currentPlayerIndex = 1; // switch back to team1's turn
					currentTeamIndex = 0;
				} else if (currentPlayerIndex == 1) {
					currentPlayerIndex = 3; // switch back to team2's turn
					currentTeamIndex = 1;
				} else {

					currentPlayerIndex = 0; // switch back to team1's turn
					currentTeamIndex = 0;

				}

				currentPlayer = gameRoom.get(currentPlayerIndex);
			}
		}
		// Print end-of-game status to all players.

		for (PlayerHandler player : gameRoom) {
			PrintWriter playerOut = player.getPrintWriter();
			if (wordGuessed(word, guessesMade)) {
				playerOut.println("Congratulations " + currentPlayer.getTeamName() + ", you have won the game!");
			} else {
				playerOut.println("Sorry, nobody guessed the word. The word was " + word);
			}
			playerOut.println("Final scores:");
			for (PlayerHandler p : gameRoom) {
				playerOut.println(p.getPlayerUsername() + ": " + p.getMultiGameScore());

			}
			ScoresDatabase.writeIndividualScoreHistory(player.getPlayerUsername(), "multi", player.getTeamName(),player.getMultiGameScore());
			
		}
	}

	private boolean wordGuessed(String word, List<Character> guessesMade) {
		for (int i = 0; i < word.length(); i++) {
			if (!guessesMade.contains(word.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private boolean addGuess(String word, List<Character> guessesMade, PlayerHandler currentPlayer,
			int currentTeamIndex) throws IOException {
		PrintWriter currentPlayerOut = currentPlayer.getPrintWriter();
		currentPlayerOut.println("\nGuess a letter:");

		String guess = currentPlayer.getIn().readLine();
		// adding only the first character of whatever the user entered
		// in case they entered more than just one character
		char c = Character.toLowerCase(guess.charAt(0)); // converting to lowercase to make guesses not case sensitive
		guessesMade.add(c);

		// checking the guess to minus from number of attempts available for player
		boolean guessExists = checkGuess(word, c, currentTeamIndex);

		return guessExists;
	}

	private boolean checkGuess(String word, Character c, int currentTeamIndex) {
		boolean exists = false;
		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == c) {
				exists = true;
				break;
			}
		}
		if (exists && currentTeamIndex == 0) {
			numAttemptsTeam1 = numAttemptsTeam1;

		} else if (exists && currentTeamIndex == 1) {
			numAttemptsTeam2 = numAttemptsTeam2;

		} else if (!exists && currentTeamIndex == 0) {
			numAttemptsTeam1 -= 1;

		} else {
			numAttemptsTeam2 -= 1;

		}
		return exists;
	}

	private boolean printWordProgress(String word, List<Character> guessesMade, PrintWriter out) {
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
}
