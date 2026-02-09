package co.eci.snake.concurrency;

import java.util.concurrent.ThreadLocalRandom;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Snake;

public final class SnakeRunner implements Runnable {
  private final Snake snake;
  private final Board board;
  private final int baseSleepMs = 80;
  private final int turboSleepMs = 40;
  private int turboTicks = 0;

  public SnakeRunner(Snake snake, Board board) {
    this.snake = snake;
    this.board = board;
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        // Esperar si el juego está pausado (sin bloquear el lock de Board)
        board.waitIfPaused();
        
        maybeTurn();  // para qutar el movimiento aleatorio y hacer que la serpiente siga una trayectoria más predecible
        var res = board.step(snake);
        if (res == Board.MoveResult.HIT_OBSTACLE) {
        randomTurn();  // Si choca con un obstáculo, intenta girar para evitarlo
        } else if (res == Board.MoveResult.ATE_TURBO) {
          turboTicks = 100;
        }
        int sleep = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        if (turboTicks > 0) turboTicks--;
        Thread.sleep(sleep);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  private void maybeTurn() {
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) randomTurn();
  }
  // Randomly change direction with a certain probability to make the snake's movement less predictable
  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }
}
