package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Snake {
  private final Deque<Position> body = new ArrayDeque<>();
  private volatile Direction direction;
  private int maxLength = 5;
  private int id;

  /** Constructor privado para inicializar la serpiente */
  private Snake(Position start, Direction dir, int id) {
    body.addFirst(start); 
    this.direction = dir;
    this.id = id;
  }
  
  /** Crea una nueva serpiente en la posición (x, y) con dirección dir e identificador id */
  public static Snake of(int x, int y, Direction dir, int id) {
    return new Snake(new Position(x, y), dir, id);
  }

  /** Retorna la dirección actual de la serpiente */
  public Direction direction() { return direction; }

  /** Cambia la dirección de la serpiente, evitando giros de 180 grados */
  public void turn(Direction dir) {
    if ((direction == Direction.UP && dir == Direction.DOWN) ||
        (direction == Direction.DOWN && dir == Direction.UP) ||
        (direction == Direction.LEFT && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  /** Retorna la posición de la cabeza de la serpiente */
  public Position head() { return body.peekFirst(); }
  
  /** Retorna la longitud actual de la serpiente */
  public int length() { return body.size(); }
  
  /** Retorna una copia de las posiciones del cuerpo de la serpiente */
  public Deque<Position> snapshot() { return new ArrayDeque<>(body); }
  
  
  /** Avanza la serpiente una posición en su dirección actual */
  public void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }
  
  /** Retorna el identificador de la serpiente */
  public int getId() {
    return id;
  }
}
