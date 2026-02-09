package co.eci.snake.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Board {

    private final int width;
    private final int height;

    private final Set<Position> mice = new HashSet<>();
    private final Set<Position> obstacles = new HashSet<>();
    private final Set<Position> turbo = new HashSet<>();
    private final Map<Position, Position> teleports = new HashMap<>();
    private final Set<Position> freePositions = new HashSet<>();  // Pool de posiciones libres
    private final List<Snake> snakes = new ArrayList<>();  // Lista de serpientes vivas
    private final List<Snake> deadSnakes = new ArrayList<>();  // Lista de serpientes muertas
    private boolean paused = false;
    private Object pauseLock = new Object();

    public enum MoveResult {
        MOVED, ATE_MOUSE, HIT_OBSTACLE, ATE_TURBO, TELEPORTED
    }

    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        // Inicializar pool con todas las posiciones
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                freePositions.add(new Position(x, y));
            }
        }
        for (int i = 0; i < 6; i++) {
            mice.add(takeRandomFree());
        }
        for (int i = 0; i < 4; i++) {
            obstacles.add(takeRandomFree());
        }
        for (int i = 0; i < 3; i++) {
            turbo.add(takeRandomFree());
        }
        createTeleportPairs(2);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public synchronized Set<Position> mice() {
        return new HashSet<>(mice);
    }

    public synchronized Set<Position> obstacles() {
        return new HashSet<>(obstacles);
    }

    public synchronized Set<Position> turbo() {
        return new HashSet<>(turbo);
    }

    public synchronized Map<Position, Position> teleports() {
        return new HashMap<>(teleports);
    }

    /**
     * Espera si el juego está pausado. Debe llamarse ANTES de step().
     * Esto evita deadlock porque no adquiere el lock de Board.
     */
    public void waitIfPaused() {
        synchronized (pauseLock) {
            while (paused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public synchronized MoveResult step(Snake snake) {
        Objects.requireNonNull(snake, "snake");
        var head = snake.head();
        var dir = snake.direction();
        Position next = new Position(head.x() + dir.dx, head.y() + dir.dy).wrap(width, height);

        if (obstacles.contains(next)) {
            return MoveResult.HIT_OBSTACLE;
        }

        for (Snake other : snakes) {
            if (other != snake && other.snapshot().contains(next)) {
                return MoveResult.HIT_OBSTACLE;
            }
        }

        boolean teleported = false;
        if (teleports.containsKey(next)) {
            next = teleports.get(next);
            teleported = true;
        }

        boolean ateMouse = mice.remove(next);
        boolean ateTurbo = turbo.remove(next);
        
        // Devolver posiciones liberadas al pool
        if (ateMouse) releasePosition(next);
        if (ateTurbo) releasePosition(next);

        snake.advance(next, ateMouse);

        if (ateMouse) {
            Position newMouse = takeRandomFree();
            Position newObstacle = takeRandomFree();
            if (newMouse != null) mice.add(newMouse);
            if (newObstacle != null) obstacles.add(newObstacle);
            if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                Position newTurbo = takeRandomFree();
                if (newTurbo != null) turbo.add(newTurbo);
            }
        }

        if (ateTurbo) {
            return MoveResult.ATE_TURBO;
        }
        if (ateMouse) {
            return MoveResult.ATE_MOUSE;
        }
        if (teleported) {
            return MoveResult.TELEPORTED;
        }
        return MoveResult.MOVED;
    }

    private void createTeleportPairs(int pairs) {
        for (int i = 0; i < pairs; i++) {
            Position a = takeRandomFree();
            Position b = takeRandomFree();
            if (a != null && b != null) {
                teleports.put(a, b);
                teleports.put(b, a);
            }
        }
    }

    /**
     * Obtiene y remueve una posición aleatoria del pool de posiciones libres.
     * Complejidad O(n) para conversión a lista, pero evita loops infinitos.
     * @return posición libre aleatoria, o null si no hay posiciones disponibles
     */
    private Position takeRandomFree() {
        if (freePositions.isEmpty()) {
            return null;  // No hay posiciones libres
        }
        List<Position> freeList = new ArrayList<>(freePositions);
        int idx = ThreadLocalRandom.current().nextInt(freeList.size());
        Position selected = freeList.get(idx);
        freePositions.remove(selected);
        return selected;
    }

    /**
     * Devuelve una posición al pool de posiciones libres.
     */
    private void releasePosition(Position p) {
        if (p != null) {
            freePositions.add(p);
        }
    }

    public void setPaused(boolean paused) {
        synchronized (pauseLock) {
            this.paused = paused;
            if (!paused) {
                pauseLock.notifyAll();
            }
        }
    }

    public synchronized List<Snake> getDeadSnakes() {
        return new ArrayList<>(deadSnakes);
    }
    
    public synchronized void addDeadSnake(Snake snake) {
        this.deadSnakes.add(snake);
        this.snakes.remove(snake);
    }

    /** Agrega una serpiente al tablero */
    public synchronized void addSnake(Snake snake) {
        snakes.add(snake);
    }

    /** Retorna una copia de la lista de serpientes */
    public synchronized List<Snake> getSnakes() {
        return new ArrayList<>(snakes);
    }

    /** Retorna la serpiente en el índice dado */
    public synchronized Snake getSnake(int index) {
        if (index < 0 || index >= snakes.size()) return null;
        return snakes.get(index);
    }

    /** Retorna el número de serpientes */
    public synchronized int snakeCount() {
        return snakes.size();
    }
}
