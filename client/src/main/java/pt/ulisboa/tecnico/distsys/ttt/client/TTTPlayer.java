package pt.ulisboa.tecnico.distsys.ttt.client;

import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pt.ulisboa.tecnico.distsys.ttt.contract.PlayRequest;
import pt.ulisboa.tecnico.distsys.ttt.contract.PlayResult;

public class TTTPlayer {

	/** Set flag to true to print debug messages. The flag can be set using the -Ddebug command line option. */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	/**
	 * During the exercise, there are two code alternatives for calling play. Set
	 * flag to true to play using a POST with a body. Set flag to false to use a GET
	 * with arguments.
	 */
	private static final boolean USE_POST_TO_PLAY_FLAG = true;

	/** Main method. */
	public static void main(String[] args) {
		System.out.println(TTTPlayer.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", TTTPlayer.class.getName());
			return;
		}
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		String restURL = "http://" + target + "/ttt/game";
		debug("URL: " + restURL);

		playGame(restURL);
	}

	private static void playGame(String restURL) {

		Client client = ClientBuilder.newClient();

		int player = 0; /* Player number - 0 or 1 */
		int go = 0; /* Square selection number for turn */
		int row = 0; /* Row index for a square */
		int column = 0; /* Column index for a square */
		int winner = -1; /* The winning player */
		PlayResult play_res;

		/*
		 * Using try with scanner - ensures the resource is closed in the end, even if
		 * there are exceptions.
		 */
		try (Scanner scanner = new Scanner(System.in)) {

			/* The main game loop. The game continues for up to 9 turns, */
			/* as long as there is no winner. */
			do {
				/* Get valid player square selection. */
				do {
					/* Print current board. */
					debug("Calling GET board");
					String board = client.target(restURL).path("board").request().get(String.class);
					System.out.println(board);

					System.out.printf(
							"\nPlayer %d, please enter the number of the square "
									+ "where you want to place your %c (or 0 to refresh the board): ",
							player, (player == 1) ? 'X' : 'O');
					go = scanner.nextInt();

					debug("go = " + go);

					if (go == 0) {
						play_res = PlayResult.UNKNOWN;
						continue;
					}

					row = --go / 3; /* Get row index of square. */
					column = go % 3; /* Get column index of square. */
					debug("row = " + row + ", column = " + column);

					PlayRequest playRequest = new PlayRequest(row, column, player);

					Response response = null;
					if (USE_POST_TO_PLAY_FLAG) {
						/* Use POST to play. */
						debug("Calling POST play");
						response = client.target(restURL).path("play").request(MediaType.APPLICATION_JSON)
								.post(Entity.entity(playRequest, MediaType.APPLICATION_JSON), Response.class);
						debug("Response status: " + response.getStatus() + " " + response.getStatusInfo());
						play_res = response.readEntity(PlayResult.class);
					} else {
						/* Use URL parameter to play. */
						/* URL to play is: play/{row}/{column}/{player} */
						String playPath = "play/" + String.valueOf(row) + '/' + String.valueOf(column) + '/'
								+ String.valueOf(player);
						debug("Calling GET " + playPath);
						play_res = client.target(restURL).path(playPath).request().get(PlayResult.class);
					}

					if (play_res != PlayResult.SUCCESS) {
						displayResult(play_res);
					}

				} while (play_res != PlayResult.SUCCESS);

				debug("Calling GET checkwinner");
				winner = client.target(restURL).path("board/checkwinner").request().get(Integer.class);

				/* Select next player. */
				player = (player + 1) % 2;

				System.out.println("player " + player);

			} while (winner == -1);

			/* Game is over so display the final board. */
			debug("Calling GET board");
			System.out.println(client.target(restURL).path("board").request().get(String.class));

			/* Display result message. */
			if (winner == 2) {
				System.out.println();
				System.out.println("How boring, it is a draw");
			} else {
				System.out.println();
				System.out.println("Congratulations, player " + winner + ", YOU ARE THE WINNER!");
			}

		}
	}

	/** Helper method to display result as text. */
	private static void displayResult(PlayResult play_res) {
		switch (play_res) {
		case OUT_OF_BOUNDS:
			System.out.print("Position outside board.");
			break;
		case SQUARE_TAKEN:
			System.out.print("Square already taken.");
			break;
		case WRONG_TURN:
			System.out.print("Not your turn.");
			break;
		case GAME_FINISHED:
			System.out.print("Game has finished.");
			break;
		default:
			break;
		}
		System.out.println(" Try again...");
	}

}