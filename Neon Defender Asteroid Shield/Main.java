import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Neon Defender: Asteroid Shield");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            GameModel model = new GameModel(panel);

            // Связывание компонентов через ссылки
            panel.setModel(model);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Старт игрового цикла в отдельном потоке
            model.startThread();
        });
    }
}
