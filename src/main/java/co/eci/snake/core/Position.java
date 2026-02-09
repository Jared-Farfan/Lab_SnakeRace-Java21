package co.eci.snake.core;

/**
* Immutable 2D position with wrapping logic.
*/
public record Position(int x, int y) {
  public Position wrap(int width, int height) { // Wrap coordinates to stay within board bounds
    int nx = ((x % width) + width) % width; // Handle negative values correctly
    int ny = ((y % height) + height) % height; // Handle negative values correctly
    return new Position(nx, ny);
  }
}
