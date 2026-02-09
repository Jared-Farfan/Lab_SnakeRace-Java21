# Snake Race — ARSW Lab #2 (Java 21, Virtual Threads)

**Escuela Colombiana de Ingeniería – Arquitecturas de Software**  
Laboratorio de programación concurrente: condiciones de carrera, sincronización y colecciones seguras.

---

## Jared Sebastian Farfan Guevara.

---

## Requisitos

- **JDK 21** (Temurin recomendado)
- **Maven 3.9+**
- SO: Windows, macOS o Linux

---

## Cómo ejecutar

```bash
mvn clean verify
mvn -q -DskipTests exec:java -Dsnakes=4
```

- `-Dsnakes=N` → inicia el juego con **N** serpientes (por defecto 2).
- **Controles**:
  - **Flechas**: serpiente **0** (Jugador 1).
  - **WASD**: serpiente **1** (si existe).
  - **Espacio** o botón **Action**: Pausar / Reanudar.

---

## Reglas del juego (resumen)

- **N serpientes** corren de forma autónoma (cada una en su propio hilo).
- **Ratones**: al comer uno, la serpiente **crece** y aparece un **nuevo obstáculo**.
- **Obstáculos**: si la cabeza entra en un obstáculo hay **rebote**.
- **Teletransportadores** (flechas rojas): entrar por uno te **saca por su par**.
- **Rayos (Turbo)**: al pisarlos, la serpiente obtiene **velocidad aumentada** temporal.
- Movimiento con **wrap-around** (el tablero “se repite” en los bordes).

---

## Arquitectura (carpetas)

```
co.eci.snake
├─ app/                 # Bootstrap de la aplicación (Main)
├─ core/                # Dominio: Board, Snake, Direction, Position
├─ core/engine/         # GameClock (ticks, Pausa/Reanudar)
├─ concurrency/         # SnakeRunner (lógica por serpiente con virtual threads)
└─ ui/legacy/           # UI estilo legado (Swing) con grilla y botón Action
```

---

# Actividades del laboratorio

## Parte I — (Calentamiento) `wait/notify` en un programa multi-hilo

1. Toma el programa [**PrimeFinder**](https://github.com/ARSW-ECI/wait-notify-excercise).
  -   [Repositorio](https://github.com/Jared-Farfan/wait-notify-excercise).
2. Modifícalo para que **cada _t_ milisegundos**:
   - Se **pausen** todos los hilos trabajadores.
   - Se **muestre** cuántos números primos se han encontrado.
   - El programa **espere ENTER** para **reanudar**.

![alt text](img/parte1.2.png)


3. La sincronización debe usar **`synchronized`**, **`wait()`**, **`notify()` / `notifyAll()`** sobre el **mismo monitor** (sin _busy-waiting_).

Se realizaron dos formas diferentes para entender el uso del synchronized con parametro de un objeto, para enternder la diferencia de que este se use intenamente de cada hilo o en base al hilo principal.

 - Usando synchronized en el cotrolador

![alt text](img/runBloqueoGeneral.png)

 - Usando ssynchronized por cada  hilo 

![alt text](img/runBloqueoIndividual.png)

4. Entrega en el reporte de laboratorio **las observaciones y/o comentarios** explicando tu diseño de sincronización (qué lock, qué condición, cómo evitas _lost wakeups_).

> Objetivo didáctico: practicar suspensión/continuación **sin** espera activa y consolidar el modelo de monitores en Java.

---

 - A: Usando synchronized en el controlador podemos ver que cunado un hilo llega a este esto y es detenido detiene a todos los demas hilos pues no pueden acceder al mismo recurso ya que el objeto el cual es parametro del synchronized es comun para todos.

 - B: Al tener synchronized en cada hilo se tiene un "objeto" parametro diferente dentro de este metodo lo que hace que no se detengan pues no compiten por un recurso, esto sirve ya que podemos elegir q hilos se detendria y cuales continuan.

## Parte II — SnakeRace concurrente (núcleo del laboratorio)

### 1) Análisis de concurrencia

- Explica **cómo** el código usa hilos para dar autonomía a cada serpiente.

  Eljuego crea hilos para cada serpiente (Runnable), cada turno o movimiento cada hilo valida el movimiento y toma una decision en base a el.

- **Identifica** y documenta en **`el reporte de laboratorio`**:
  - Posibles **condiciones de carrera**.

    Viendo que la clase Boar tiene los metodos para consultar los objetos del mapa de forma synchronize y la logica para su siguente posicion no habria condiciones de carrera pues cuando una serpiente este usando eso ninguna más lo hará.

    ![alt text](img/boarSyn.png.png)

  - **Colecciones** o estructuras **no seguras** en contexto concurrente.

    En cocurrencia podemos ver como insegura toda estructura que se consulte recurrentemente y puede alterar el comportamiento de un objeto, commo lo son: map, array, list, set, Queue, Stack , etc.
    En este caso las estucturas son la que contienen los objetos del mapa como lo son: (contenidas en la clase Boar)

    ![alt text](img/objBoar.png)

  - Ocurrencias de **espera activa** (busy-wait) o de sincronización innecesaria.

    Se puede crear una espera activa en el movimiento de las serpientes y ya que si estas comen un raton puede entar al loop para ubicar los nuevos objeto y quedar hay si no hay posiciónes libres.
    El juego no esta puede pausar en realidad.

### 2) Correcciones mínimas y regiones críticas

- **Elimina** esperas activas reemplazándolas por **señales** / **estados** o mecanismos de la librería de concurrencia.

Se añadio una conjunto de las posiciones libres para evitar la búsqueda por fuerza bruta, asi el metodo ThreadLocalRandom.current().nextInt() damos una posición libre.
![alt text](img/freePositions.png)

- Protege **solo** las **regiones críticas estrictamente necesarias** (evita bloqueos amplios).

Existia un riesgo de alta espera e incluso nunca salir del bluque 

- Justifica en **`el reporte de laboratorio`** cada cambio: cuál era el riesgo y cómo lo resuelves.

Si el tablero se encontrara lleno de objetos nunca saldria del blucle hasta q no encontrara un espacio vacio donde poner los objetos y el programa moririra.

### 3) Control de ejecución seguro (UI)

- Implementa la **UI** con **Iniciar / Pausar / Reanudar** (ya existe el botón _Action_ y el reloj `GameClock`).
- Al **Pausar**, muestra de forma **consistente** (sin _tearing_):
  - La **serpiente viva más larga**.
  - La **peor serpiente** (la que **primero murió**).
- Considera que la suspensión **no es instantánea**; coordina para que el estado mostrado no quede “a medias”.

### 3) Control de ejecución seguro (UI)

- Implementa la **UI** con **Iniciar / Pausar / Reanudar** (ya existe el botón _Action_ y el reloj `GameClock`).
- Al **Pausar**, muestra de forma **consistente** (sin _tearing_):
  - La **serpiente viva más larga**.
  - La **peor serpiente** (la que **primero murió**).
- Considera que la suspensión **no es instantánea**; coordina para que el estado mostrado no quede “a medias”.

  Erro en la pausa, se añadio un metodo en boar para notificar a los hilos y que se detengan de la forma correcta.
  ![alt text](img/pausaBoard.png)

  Como no se tiene una logica inicial de muerte se definio como muerte si una serpiente choca con un obstaculo u otra serpiente, modificamos la logica para tener las serpientes contenidas en board y para ver las estadisticas al pausar el juego.
  ![alt text](img/estadisticas.png)

### 4) Robustez bajo carga

- Ejecuta con **N alto** (`-Dsnakes=20` o más) y/o aumenta la velocidad.
- El juego **no debe romperse**: sin `ConcurrentModificationException`, sin lecturas inconsistentes, sin _deadlocks_.
- Si habilitas **teleports** y **turbo**, verifica que las reglas no introduzcan carreras.

> Entregables detallados más abajo.

---

## Entregables

1. **Código fuente** funcionando en **Java 21**.
2. Todo de manera clara en **`**el reporte de laboratorio**`** con:
   - Data races encontradas y su solución.
   - Colecciones mal usadas y cómo se protegieron (o sustituyeron).
   - Esperas activas eliminadas y mecanismo utilizado.
   - Regiones críticas definidas y justificación de su **alcance mínimo**.
3. UI con **Iniciar / Pausar / Reanudar** y estadísticas solicitadas al pausar.

---

## Criterios de evaluación (10)

- (3) **Concurrencia correcta**: sin data races; sincronización bien localizada.
- (2) **Pausa/Reanudar**: consistencia visual y de estado.
- (2) **Robustez**: corre **con N alto** y sin excepciones de concurrencia.
- (1.5) **Calidad**: estructura clara, nombres, comentarios; sin _code smells_ obvios.
- (1.5) **Documentación**: **`reporte de laboratorio`** claro, reproducible;

---

## Tips y configuración útil

- **Número de serpientes**: `-Dsnakes=N` al ejecutar.
- **Tamaño del tablero**: cambiar el constructor `new Board(width, height)`.
- **Teleports / Turbo**: editar `Board.java` (métodos de inicialización y reglas en `step(...)`).
- **Velocidad**: ajustar `GameClock` (tick) o el `sleep` del `SnakeRunner` (incluye modo turbo).

---

## Cómo correr pruebas

```bash
mvn clean verify
```

Incluye compilación y ejecución de pruebas JUnit. Si tienes análisis estático, ejecútalo en `verify` o `site` según tu `pom.xml`.

---

## Créditos

Este laboratorio es una adaptación modernizada del ejercicio **SnakeRace** de ARSW. El enunciado de actividades se conserva para mantener los objetivos pedagógicos del curso.

**Base construida por el Ing. Javier Toquica.**
