
import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.gui.DWApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CollisionAvoidanceUI extends JFrame{
    private final static String TITLE = "Collision Avoidance";

    private JFrame thisFrame        = null;
    private FeedbackSystemUI fbsUI  = null;

    private ViewerPanel3D viewport;
    private JPanel topPanel;
    private JPanel leftCol;
    private JPanel rightCol;
    private JButton b_OpenFeedBackSystemUI;
    private JButton b_StartKinect;

    private JLabel lmaxDepth;
    private JLabel lleftBorder;
    private JLabel lrightBorder;

    private JTextField input_leftBorder;
    private JTextField input_rightBorder;
    private JTextField input_maxdepth;

    private JCheckBox showInfrared;
    private CollisionAvoidance cAvoid;
    private boolean showVideo = false;

    public CollisionAvoidanceUI() {
        super(TITLE);
        this.thisFrame = this;

        this.setSize(new Dimension(850,550));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());

        this.topPanel       = new JPanel();
        this.leftCol        = new JPanel();
        this.rightCol       = new JPanel();

        this.showInfrared   = new JCheckBox("Infrared");
        showInfrared.setSelected(false);
        showInfrared.addActionListener(showInfraredAction());

        this.b_StartKinect  = new JButton("Start detection");
        this.b_StartKinect.addActionListener(startDetectionAction());

        this.b_OpenFeedBackSystemUI = new JButton("Feedback System");
        this.b_OpenFeedBackSystemUI.addActionListener(openFeedBackSystemUIAction());
        this.b_OpenFeedBackSystemUI.setHorizontalAlignment(SwingConstants.RIGHT);;

        this.lmaxDepth      = new JLabel("Max Depth: ");
        this.lleftBorder    = new JLabel("Left: ");
        this.lrightBorder   = new JLabel("Right: ");

        this.input_maxdepth     = new JTextField("2.0", 5);
        this.input_leftBorder   = new JTextField("0", 5);
        this.input_rightBorder  = new JTextField("0", 5);

        this.input_maxdepth.setHorizontalAlignment(SwingConstants.CENTER);
        this.input_leftBorder.setHorizontalAlignment(SwingConstants.CENTER);
        this.input_rightBorder.setHorizontalAlignment(SwingConstants.CENTER);

        this.topPanel.setLayout(new GridLayout(1,2));
        this.leftCol.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.rightCol.setLayout(new FlowLayout(FlowLayout.TRAILING));

        this.leftCol.add(lmaxDepth);
        this.leftCol.add(input_maxdepth);
        this.leftCol.add(lleftBorder);
        this.leftCol.add(input_leftBorder);
        this.leftCol.add(lrightBorder);
        this.leftCol.add(input_rightBorder);

        this.rightCol.add(b_StartKinect);
        this.rightCol.add(b_OpenFeedBackSystemUI);

        this.topPanel.add(leftCol);
        this.topPanel.add(rightCol);

        viewport = new ViewerPanel3D();
        viewport.setShowVideo(showVideo);
        this.add(viewport, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(cAvoid != null) cAvoid.close();
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
            Float maxDepth      = new Float(input_maxdepth.getText());
            Integer leftBorder  = new Integer(input_leftBorder.getText());
            Integer rightBorder = new Integer(input_rightBorder.getText());

            if( cAvoid == null){
                cAvoid = new CollisionAvoidance(viewport, maxDepth, leftBorder, rightBorder);
                cAvoid.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.INFRARED|J4KSDK.XYZ|J4KSDK.UV);
                if(cAvoid.isInitialized()){
                    b_StartKinect.setText("Update");
                } else {
                    cAvoid.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.INFRARED|J4KSDK.XYZ|J4KSDK.UV);
                }
            } else {
                cAvoid.update(maxDepth, leftBorder, rightBorder, showVideo);
            }

            System.out.println("Max Depth: "+maxDepth);
            System.out.println("Left Border: "+leftBorder);
            System.out.println("Right Border: "+rightBorder);
        };
    }

    private ActionListener showInfraredAction() {
        return e -> {
            System.out.println("Show infrared");
        };
    }
}