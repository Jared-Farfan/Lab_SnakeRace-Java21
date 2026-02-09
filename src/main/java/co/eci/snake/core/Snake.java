package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Snake {
  private final Deque<Position> body = new ArrayDeque<>();
  private volatile Direction direction;
  private int maxLength = 5;
  private int id;

  private Snake(Position start, Direction dir, int id) {
    body.addFirst(start); 
    this.direction = dir;
    this.id = id;
  }
  // Factory method for creating a new snake at a given position and direction
  public static Snake of(int x, int y, Direction dir, int id) {
    return new Snake(new Position(x, y), dir, id);
  }

  public Direction direction() { return direction; }
  // Method to change the snake's direction, ensuring it cannot reverse directly
  public void turn(Direction dir) {
    if ((direction == Direction.UP && dir == Direction.DOWN) ||
        (direction == Direction.DOWN && dir == Direction.UP) ||
        (direction == Direction.LEFT && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  public Position head() { return body.peekFirst(); }
  
  /** Retorna la longitud actual de la serpiente */
  public int length() { return body.size(); }
  
  // Method to get a snapshot of the snake's body positions for rendering or collision detection
  public Deque<Position> snapshot() { return new ArrayDeque<>(body); }
  // Method to advance the snake's position based on its current direction and whether it has eaten a mouse
  public void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }
  
  public int getId() {
    return id;
  }
}
