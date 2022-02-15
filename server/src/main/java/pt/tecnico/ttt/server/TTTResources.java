package pt.tecnico.ttt.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import pt.tecnico.ttt.contract.PlayRequest;
import pt.tecnico.ttt.contract.PlayResult;

/** Root resource (exposed at "game" path) */
@Path("game")
public class TTTResources {

  /** TTTGame currently being played */
  static TTTGame game = new TTTGame();

  /**
   * Method handling HTTP GET requests.
   * The returned object will be sent to the client as "text/plain" media type.
   *
   * @return Board that will be returned as a text/plain response.
   */
  @GET
  @Path("board")
  @Produces(MediaType.TEXT_PLAIN)
  public String getBoard() {
    return game.toString();
  }

  /**
   * Method handling HTTP GET requests.
   * The returned object will be sent to the client as "text/plain" media type.
   *
   * @return Board that will be returned as a text/plain response.
   */
  @GET
  @Path("board/reset")
  @Produces(MediaType.TEXT_PLAIN)
  public String resetBoard() {
    game.resetBoard();
    return game.toString();
  }

  @GET
  @Path("board/checkwinner")
  @Produces(MediaType.TEXT_PLAIN)
  public int checkWinner() {
    return game.checkWinner();
  }

  @POST
  @Path("play")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PlayResult play(PlayRequest playRequest) {
    return game.play(playRequest.getRow(), playRequest.getColumn(), playRequest.getPlayer());
  }

  @GET
  @Path("play/{row}/{column}/{player}")
  @Produces(MediaType.APPLICATION_JSON)
  public PlayResult play(
      @PathParam("row") int row, @PathParam("column") int column, @PathParam("player") int player) {
    return game.play(row, column, player);
  }

  @GET
  @Path("restart")
  public void restart() {
    game.restart();
  }
}
