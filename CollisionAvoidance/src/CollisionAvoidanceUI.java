import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CollisionAvoidanceUI extends JFrame {
    private final static String TITLE = "Collision Avoidance";

    private JFrame thisFrame        = null;
    private FeedbackSystemUI fbsUI  = null;

    private JPanel viewportPanel;
    private JPanel topPanel;
    private JButton b_OpenFeedBackSystemUI;
    private JButton b_StartKinect;

    private JTextField x_DetectionRange;
    private JTextField y_DetectionRange;

    public CollisionAvoidanceUI() {
        super(TITLE);
        this.thisFrame = this;

        this.setSize(650,550);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());

        this.topPanel       = new JPanel();
        this.viewportPanel  = new JPanel();

        this.b_StartKinect  = new JButton("Start detection");
        this.b_StartKinect.addActionListener(startDetectionAction());

        this.b_OpenFeedBackSystemUI = new JButton("Feedback System");
        this.b_OpenFeedBackSystemUI.addActionListener(openFeedBackSystemUIAction());

        this.topPanel.add(b_StartKinect);
        this.topPanel.add(b_OpenFeedBackSystemUI);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(viewportPanel, BorderLayout.CENTER);

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

    }

    private ActionListener openFeedBackSystemUIAction() {
        return e -> fbsUI = new FeedbackSystemUI();
    }

    private ActionListener startDetectionAction() {
        return e -> {
            fbsUI = new FeedbackSystemUI();
        };
    }
}