package co.eci.snake.core.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import co.eci.snake.core.GameState;

public final class GameClock implements AutoCloseable {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final long periodMillis;
  private final Runnable tick;
  private final java.util.concurrent.atomic.AtomicReference<GameState> state = new AtomicReference<>(GameState.STOPPED);

  public GameClock(long periodMillis, Runnable tick) {
    if (periodMillis <= 0) throw new IllegalArgumentException("periodMillis must be > 0");
    this.periodMillis = periodMillis;
    this.tick = java.util.Objects.requireNonNull(tick, "tick");
  }

  public void start() {
    if (state.compareAndSet(GameState.STOPPED, GameState.PAUSED)) {
      scheduler.scheduleAtFixedRate(() -> {
        // Siempre ejecutar tick (repaint) para mostrar el tablero, incluso en pausa
        tick.run();
      }, 0, periodMillis, TimeUnit.MILLISECONDS);
    }
  }

  public void pause()  { state.set(GameState.PAUSED); }
  public void resume() { state.set(GameState.RUNNING); }
  public void stop()   { state.set(GameState.STOPPED); }
  @Override public void close() { scheduler.shutdownNow(); }
}
