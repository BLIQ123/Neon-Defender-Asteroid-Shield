import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * Все настройки, габариты, цвета и управление вынесены в константы.
 * Исключает использование «магических чисел» в коде проекта.
 */
public interface GameConstants {
    // Размеры игрового окна
    int WINDOW_WIDTH = 800;
    int WINDOW_HEIGHT = 650;
    int FPS_DELAY_MS = 16; // Приблизительно 60 кадров в секунду

    // Параметры платформы игрока
    int PADDLE_WIDTH = 110;
    int PADDLE_HEIGHT = 18;
    int PADDLE_Y_POS = 560;

    // Параметры энергетического ядра (снаряда)
    int BALL_SIZE = 16;
    double BALL_INITIAL_SPEED_Y = -5.0;
    double BALL_MIN_SPEED_X = -6.0;
    double BALL_MAX_SPEED_X = 6.0;

    // Параметры астероидов (целей)
    int ASTEROID_ROWS = 4;
    int ASTEROID_COLS = 7;
    int ASTEROID_WIDTH = 85;
    int ASTEROID_HEIGHT = 26;
    int ASTEROID_OFFSET_TOP = 60;
    int ASTEROID_OFFSET_LEFT = 60;
    int ASTEROID_GAP_X = 15;
    int ASTEROID_GAP_Y = 12;
    int SCORE_PER_ASTEROID = 100;

    // Игровой процесс
    int INITIAL_LIVES = 3;

    // Клавиши управления
    int KEY_NEW_GAME = KeyEvent.VK_N;
    int KEY_PAUSE = KeyEvent.VK_P;
    int KEY_LAUNCH_BALL = KeyEvent.VK_SPACE;
    int KEY_EXIT = KeyEvent.VK_ESCAPE;

    // Авторская неоновая цветовая палитра
    Color COLOR_BACKGROUND = new Color(15, 15, 28);
    Color COLOR_GRID_LINES = new Color(30, 30, 55);
    Color COLOR_PADDLE_BODY = new Color(0, 230, 255);
    Color COLOR_BALL = new Color(255, 60, 140);
    Color COLOR_BALL_GLOW = new Color(255, 60, 140, 70);
    Color COLOR_TEXT = new Color(240, 240, 255);
    Color COLOR_HINT_TEXT = new Color(130, 140, 180);
    Color COLOR_OVERLAY = new Color(10, 10, 20, 210);

    // Градиентные цвета для слоев астероидов
    Color[] COLOR_ASTEROID_TIERS = {
        new Color(255, 75, 75),   // Красный (верхний ряд)
        new Color(255, 140, 0),   // Оранжевый
        new Color(0, 230, 120),   // Зеленый
        new Color(0, 180, 255)    // Голубой (нижний ряд)
    };
}