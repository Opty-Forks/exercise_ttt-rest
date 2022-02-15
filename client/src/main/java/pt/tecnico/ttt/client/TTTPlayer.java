package pt.tecnico.ttt.client;

import java.util.Scanner;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Feature;

import org.glassfish.jersey.logging.LoggingFeature;

import pt.tecnico.ttt.contract.PlayResult;

public class TTTPlayer {

  public static void main(String[] args) {
    System.out.println(TTTPlayer.class.getSimpleName());

    // receive and print arguments
    System.out.printf("Received %d arguments%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("arg[%d] = %s%n", i, args[i]);
    }

    // check arguments
    if (args.length < 2) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s host port%n", TTTPlayer.class.getName());
      return;
    }

    final String host = args[0];
    final int port = Integer.parseInt(args[1]);
    final String target = host + ":" + port;

    String restURL = "http://" + target + "/ttt/game";
    playGame(restURL);
  }

  private static void playGame(String restURL) {
    Logger logger = Logger.getLogger(TTTPlayer.class.getName());
    Feature feature = new LoggingFeature(logger);
    Client client = ClientBuilder.newClient().register(feature);

    /* Reset board to play a new game */
    client.target(restURL).path("restart").request().get();

    int player = 0; // Player number - 0 or 1
    int go; // Square selection number for turn
    int row; // Row index for a square
    int column; // Column index for a square
    int winner; // The winning player
    PlayResult play_res;

    /* The main game loop. The game continues for up to 9 turns. As long as there is no winner. */
    do {
      /* Get valid player square selection */
      do {
        /* Print current board */
        System.out.println(client.target(restURL).path("board").request().get(String.class));

        System.out.printf(
            "\nPlayer %d, "
                + "please enter the number of the square where you want to place your '%c' "
                + "(or 10 to refresh the board): ",
            player, (player == 1) ? 'X' : 'O');
        go = readPlay();

        if (go == 0) {
          play_res = PlayResult.UNKNOWN;
          continue;
        } else if (go == 10) {
          client.target(restURL).path("restart").request().get();
          player = 0;
          play_res = PlayResult.UNKNOWN;
          continue;
        }

        row = --go / 3; // Get row index of square
        column = go % 3; // Get column index of square
        String playString = "play/" + row + '/' + column + '/' + player;
        play_res = client.target(restURL).path(playString).request().get(PlayResult.class);
        if (play_res != PlayResult.SUCCESS) {
          displayResult(play_res);
        }

      } while (play_res != PlayResult.SUCCESS);

      /* Select player */
      player = (player + 1) % 2;

      winner = client.target(restURL).path("board/checkwinner").request().get(Integer.class);
    } while (winner == -1);

    /* Game is over so display the final board */
    System.out.println(client.target(restURL).path("board").request().get(String.class));

    /* Display result message */
    if (winner == 2) System.out.println("\nHow boring, it is a draw");
    else System.out.println("\nCongratulations, player " + winner + ", YOU ARE THE WINNER!");
  }

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
    System.out.println("Try again...");
  }

  private static int readPlay() {
    Scanner scanner = new Scanner(System.in);
    return scanner.nextInt();
  }
}
