import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame ca = new CollisionAvoidanceUI();
                ca.setVisible(true);
            }
        });
        System.out.println(System.getProperty("user.dir"));
    }
}
