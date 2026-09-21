import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Класс отрисовки игрового мира и обработки пользовательского ввода.
 * Связан с моделью через прямую ссылку.
 */
public class GamePanel extends JPanel implements GameConstants {

    private GameModel model;

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(COLOR_BACKGROUND);
        setFocusable(true);

        initInputListeners();
    }

    public void setModel(GameModel model) {
        this.model = model;
    }

    private void initInputListeners() {
        // Управление платформой мышью
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (model != null) {
                    model.updatePaddlePosition(e.getX());
                }
            }
        });

        // Клавиатурные команды: N (Новая игра), P (Пауза), SPACE (Пуск), ESC (Выход)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (model == null) return;

                int code = e.getKeyCode();
                if (code == KEY_NEW_GAME) {
                    model.initGameSession();
                } else if (code == KEY_PAUSE) {
                    model.togglePause();
                } else if (code == KEY_LAUNCH_BALL) {
                    model.launchBall();
                } else if (code == KEY_EXIT) {
                    model.stopThread();
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model == null) return;

        Graphics2D g2 = (Graphics2D) g;
        // Включение сглаживания для качественной неоновой графики
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackgroundGrid(g2);
        drawAsteroids(g2);
        drawPaddle(g2);
        drawBall(g2);
        drawHUD(g2);

        if (model.isGameOver()) {
            drawStatusOverlay(g2, "GAME OVER", "Нажмите N для перезапуска");
        } else if (model.isVictory()) {
            drawStatusOverlay(g2, "СЕКТОР ОЧИЩЕН! ПОБЕДА", "Нажмите N для новой игры");
        } else if (model.isPaused()) {
            drawStatusOverlay(g2, "ПАУЗА", "Нажмите P для продолжения");
        }
    }

    private void drawBackgroundGrid(Graphics2D g2) {
        g2.setColor(COLOR_GRID_LINES);
        for (int x = 0; x < WINDOW_WIDTH; x += 40) {
            g2.drawLine(x, 0, x, WINDOW_HEIGHT);
        }
        for (int y = 0; y < WINDOW_HEIGHT; y += 40) {
            g2.drawLine(0, y, WINDOW_WIDTH, y);
        }
    }

private void drawAsteroids(Graphics2D g2) {
        for (int row = 0; row < ASTEROID_ROWS; row++) {
            Color baseColor = COLOR_ASTEROID_TIERS[row % COLOR_ASTEROID_TIERS.length];
            // Создаем темный оттенок для тени и светлый для неонового контура
            Color darkShade = baseColor.darker().darker();
            Color glowOutline = baseColor.brighter();

            for (int col = 0; col < ASTEROID_COLS; col++) {
                if (model.isAsteroidAlive(row, col)) {
                    int x = ASTEROID_OFFSET_LEFT + col * (ASTEROID_WIDTH + ASTEROID_GAP_X);
                    int y = ASTEROID_OFFSET_TOP + row * (ASTEROID_HEIGHT + ASTEROID_GAP_Y);
                    int w = ASTEROID_WIDTH;
                    int h = ASTEROID_HEIGHT;

                    // 1. Формируем граненый многоугольник астероида (со срезанными углами и сколами)
                    int[] xPoints = {
                        x + 12, x + w / 2, x + w - 10, x + w, 
                        x + w - 8, x + w / 2 + 10, x + 8, x
                    };
                    int[] yPoints = {
                        y, y + 4, y + 2, y + 10, 
                        y + h, y + h - 3, y + h, y + 12
                    };
                    java.awt.Polygon asteroidPoly = new java.awt.Polygon(xPoints, yPoints, 8);

                    // 2. Тело астероида (базовый цвет)
                    g2.setColor(baseColor);
                    g2.fillPolygon(asteroidPoly);

                    // 3. Теневая нижняя грань для объема
                    g2.setColor(darkShade);
                    int[] shadowX = { x, x + 8, x + w / 2 + 10, x + w - 8, x + w - 16, x + 16 };
                    int[] shadowY = { y + 12, y + h, y + h - 3, y + h, y + h - 8, y + h - 8 };
                    g2.fillPolygon(shadowX, shadowY, 6);

                    // 4. Кратеры на поверхности
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fillOval(x + 18, y + 6, 12, 8);
                    g2.fillOval(x + 48, y + 11, 15, 9);
                    g2.fillOval(x + 32, y + 13, 8, 6);

                    // 5. Неоновый контур осколка
                    g2.setColor(glowOutline);
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawPolygon(asteroidPoly);
                }
            }
        }
    }

    private void drawPaddle(Graphics2D g2) {
        g2.setColor(COLOR_PADDLE_BODY);
        g2.fillRoundRect((int) model.getPaddleX(), PADDLE_Y_POS, PADDLE_WIDTH, PADDLE_HEIGHT, 10, 10);
    }

    private void drawBall(Graphics2D g2) {
        int bx = (int) model.getBallX();
        int by = (int) model.getBallY();

        // Неоновый ореол
        g2.setColor(COLOR_BALL_GLOW);
        g2.fillOval(bx - 3, by - 3, BALL_SIZE + 6, BALL_SIZE + 6);

        // Ядро снаряда
        g2.setColor(COLOR_BALL);
        g2.fillOval(bx, by, BALL_SIZE, BALL_SIZE);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setFont(new Font("Consolas", Font.BOLD, 15));
        g2.setColor(COLOR_TEXT);
        g2.drawString("Счёт: " + model.getScore(), 20, 30);
        g2.drawString("Энергия щита (Жизни): " + model.getLives(), 200, 30);

        // Подсказки по управлению на экране
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(COLOR_HINT_TEXT);
        String controls = "Мышь: движение | Пробел: запуск | N: заново | P: пауза | ESC: выход";
        g2.drawString(controls, 20, WINDOW_HEIGHT - 15);
    }

    private void drawStatusOverlay(Graphics2D g2, String title, String subtitle) {
        g2.setColor(COLOR_OVERLAY);
        g2.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g2.setColor(COLOR_TEXT);
        g2.setFont(new Font("Consolas", Font.BOLD, 36));
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = (WINDOW_WIDTH - fmTitle.stringWidth(title)) / 2;
        g2.drawString(title, titleX, WINDOW_HEIGHT / 2 - 20);

        g2.setFont(new Font("Consolas", Font.PLAIN, 18));
        FontMetrics fmSub = g2.getFontMetrics();
        int subX = (WINDOW_WIDTH - fmSub.stringWidth(subtitle)) / 2;
        g2.drawString(subtitle, subX, WINDOW_HEIGHT / 2 + 30);
    }
}