import sun.awt.FullScreenCapable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CollisionAvoidance extends JFrame {
    private final static String TITLE = "Kinec Collision Avoidance";

    private FeedbackSystem fbs      = null;
    private FeedbackSystemUI fbsUI  = null;

    private JPanel mainPancel;
    private JPanel topPanel;
    private JButton b_FeedBackSystemUI;

    public CollisionAvoidance() {
        super(TITLE);

        this.setSize(300,300);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());

        this.topPanel = new JPanel();
        this.mainPancel = new JPanel();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
            }
        } );


        this.b_FeedBackSystemUI = new JButton("Feedback System");
        this.b_FeedBackSystemUI.addActionListener(openFeedBackSystemUI());

        this.topPanel.add(b_FeedBackSystemUI);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(mainPancel, BorderLayout.CENTER);

        this.fbs = new FeedbackSystem();
/*        if ( this.fbs.initialize() ) {

        };*/

        this.setVisible(true);
    }


    public static void main(String[] args) {
        new CollisionAvoidance();
      /*  SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame caInstance = new
            }
        });*/
    }


    private ActionListener openFeedBackSystemUI() {
        return e -> {
            if( fbsUI != null) {
                fbsUI.toFront();
                fbsUI.requestFocus();
            } else {
                this.fbsUI = new FeedbackSystemUI(fbs);
            }
        };
    }
}