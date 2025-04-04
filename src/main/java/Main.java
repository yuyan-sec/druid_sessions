import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        JFrame frame = new JFrame("Alibaba Druid");
        frame.setContentPane(new GUI().rootJPanel);
        frame.setSize(1200, 800);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
