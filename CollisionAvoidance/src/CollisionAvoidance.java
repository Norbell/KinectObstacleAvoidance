import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CollisionAvoidance extends JFrame {
    private final static String TITLE = "Collision Avoidance";

    private JFrame thisFrame        = null;
    private FeedbackSystemUI fbsUI  = null;

    private JPanel mainPancel;
    private JPanel topPanel;
    private JButton b_OpenFeedBackSystemUI;

    public CollisionAvoidance() {
        super(TITLE);
        this.thisFrame = this;

        this.setSize(300,300);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());

        this.topPanel   = new JPanel();
        this.mainPancel = new JPanel();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                thisFrame.dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                thisFrame.setVisible(false);
                System.exit(0);
            }
        });

        this.b_OpenFeedBackSystemUI = new JButton("Feedback System");
        this.b_OpenFeedBackSystemUI.addActionListener(openFeedBackSystemUIAction());

        this.topPanel.add(b_OpenFeedBackSystemUI);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(mainPancel, BorderLayout.CENTER);
    }

    private ActionListener openFeedBackSystemUIAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fbsUI = new FeedbackSystemUI();
            }
        };
    }
}