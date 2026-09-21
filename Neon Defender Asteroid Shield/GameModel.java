import java.awt.Rectangle;

/**
 * Класс логики и физики игры.
 * Отвечает за игровой цикл в отдельном потоке (Runnable), обнаружение столкновений и счёт.
 */
public class GameModel implements Runnable, GameConstants {

    // Ссылка на компонент отрисовки для вызова repaint()
    private final GamePanel view;

    // Поток игрового цикла
    private Thread gameThread;
    private volatile boolean isRunning;
    private boolean isPaused;
    private boolean isGameOver;
    private boolean isVictory;
    private boolean isBallAttached;

    // Игровые показатели
    private int score;
    private int lives;

    // Координаты и скорости
    private double paddleX;
    private double ballX;
    private double ballY;
    private double ballSpeedX;
    private double ballSpeedY;

    // Сетка астероидов
    private final boolean[][] asteroids;

    public GameModel(GamePanel view) {
        this.view = view;
        this.asteroids = new boolean[ASTEROID_ROWS][ASTEROID_COLS];
        initGameSession();
    }

    /**
     * Старт игрового потока
     */
    public synchronized void startThread() {
        if (gameThread == null || !isRunning) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * Остановка игрового потока при выходе
     */
    public synchronized void stopThread() {
        isRunning = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    /**
     * Основной игровой цикл
     */
    @Override
    public void run() {
        while (isRunning) {
            if (!isPaused && !isGameOver && !isVictory) {
                updatePhysics();
            }

            // Запрос перерисовки графики
            view.repaint();

            try {
                Thread.sleep(FPS_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Полный сброс и старт новой игры
     */
    public void initGameSession() {
        score = 0;
        lives = INITIAL_LIVES;
        isGameOver = false;
        isVictory = false;
        isPaused = false;
        resetAsteroids();
        resetPaddleAndBall();
    }

    /**
     * Запуск шара с платформы
     */
    public void launchBall() {
        if (isBallAttached && !isGameOver && !isVictory && !isPaused) {
            isBallAttached = false;
            ballSpeedY = BALL_INITIAL_SPEED_Y;
            ballSpeedX = (Math.random() * 4.0) - 2.0; // Случайный начальный разброс X
        }
    }

    public void togglePause() {
        if (!isGameOver && !isVictory) {
            isPaused = !isPaused;
        }
    }

    public void updatePaddlePosition(int mouseX) {
        // Ограничиваем ракетку границами игрового поля
        paddleX = Math.max(0, Math.min(mouseX - (PADDLE_WIDTH / 2.0), WINDOW_WIDTH - PADDLE_WIDTH));

        if (isBallAttached) {
            ballX = paddleX + (PADDLE_WIDTH / 2.0) - (BALL_SIZE / 2.0);
            ballY = PADDLE_Y_POS - BALL_SIZE - 2;
        }
    }

    private void updatePhysics() {
        if (isBallAttached) {
            return;
        }

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Отскок от левой и правой границы
        if (ballX <= 0) {
            ballX = 0;
            ballSpeedX = -ballSpeedX;
        } else if (ballX + BALL_SIZE >= WINDOW_WIDTH) {
            ballX = WINDOW_WIDTH - BALL_SIZE;
            ballSpeedX = -ballSpeedX;
        }

        // Отскок от верхней границы
        if (ballY <= 0) {
            ballY = 0;
            ballSpeedY = -ballSpeedY;
        }

        // Вылет за нижнюю границу (потеря жизни)
        if (ballY > WINDOW_HEIGHT) {
            handleLifeLoss();
            return;
        }

        checkPaddleCollision();
        checkAsteroidCollisions();
    }

    private void checkPaddleCollision() {
        Rectangle ballRect = new Rectangle((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);
        Rectangle paddleRect = new Rectangle((int) paddleX, PADDLE_Y_POS, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Столкновение регистрируется только при движении вниз для избежания залипания
        if (ballSpeedY > 0 && ballRect.intersects(paddleRect)) {
            ballSpeedY = -ballSpeedY;

            // Расчёт угла отражения в зависимости от точки касания платформы
            double hitPoint = (ballX + (BALL_SIZE / 2.0)) - (paddleX + (PADDLE_WIDTH / 2.0));
            double normalizedHit = hitPoint / (PADDLE_WIDTH / 2.0);
            ballSpeedX = normalizedHit * BALL_MAX_SPEED_X;

            // Коррекция позиции для предотвращения застревания
            ballY = PADDLE_Y_POS - BALL_SIZE - 1;
        }
    }

    private void checkAsteroidCollisions() {
        Rectangle ballRect = new Rectangle((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);
        boolean remainingAsteroids = false;

        for (int row = 0; row < ASTEROID_ROWS; row++) {
            for (int col = 0; col < ASTEROID_COLS; col++) {
                if (asteroids[row][col]) {
                    remainingAsteroids = true;
                    int astX = ASTEROID_OFFSET_LEFT + col * (ASTEROID_WIDTH + ASTEROID_GAP_X);
                    int astY = ASTEROID_OFFSET_TOP + row * (ASTEROID_HEIGHT + ASTEROID_GAP_Y);
                    Rectangle astRect = new Rectangle(astX, astY, ASTEROID_WIDTH, ASTEROID_HEIGHT);

                    if (ballRect.intersects(astRect)) {
                        asteroids[row][col] = false;
                        score += SCORE_PER_ASTEROID;

                        // Отражение шара по оси Y
                        ballSpeedY = -ballSpeedY;
                        return; // Обрабатываем одно столкновение за кадр
                    }
                }
            }
        }

        if (!remainingAsteroids) {
            isVictory = true;
        }
    }

    private void handleLifeLoss() {
        lives--;
        if (lives <= 0) {
            isGameOver = true;
        } else {
            resetPaddleAndBall();
        }
    }

    private void resetPaddleAndBall() {
        paddleX = (WINDOW_WIDTH - PADDLE_WIDTH) / 2.0;
        ballX = paddleX + (PADDLE_WIDTH / 2.0) - (BALL_SIZE / 2.0);
        ballY = PADDLE_Y_POS - BALL_SIZE - 2;
        ballSpeedX = 0;
        ballSpeedY = 0;
        isBallAttached = true;
    }

    private void resetAsteroids() {
        for (int r = 0; r < ASTEROID_ROWS; r++) {
            for (int c = 0; c < ASTEROID_COLS; c++) {
                asteroids[r][c] = true;
            }
        }
    }

    // Геттеры для отрисовки
    public double getPaddleX() { return paddleX; }
    public double getBallX() { return ballX; }
    public double getBallY() { return ballY; }
    public boolean isAsteroidAlive(int row, int col) { return asteroids[row][col]; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isVictory() { return isVictory; }
    public boolean isPaused() { return isPaused; }
    public boolean isBallAttached() { return isBallAttached; }
}