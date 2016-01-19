
import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.gui.DWApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CollisionAvoidanceUI extends JFrame{
    private final static String TITLE = "Collision Avoidance";

    private JFrame thisFrame        = null;
    private FeedbackSystemUI fbsUI  = null;

    private ViewerPanel3D viewport;
    private JPanel bottomPanel;
    private JPanel leftCol;
    private JPanel rightCol;
    private JButton b_OpenFeedBackSystemUI;
    private JButton b_StartKinect;
    private JButton b_calcMaxDistance;

    private JLabel lmaxDepth;
    private JLabel lminDepth;
    private JLabel lmaxDepthCalc;
    private JLabel lmaxDepthValue;
    private JLabel lZone1;
    private JLabel lZone2;
    private JLabel lZone3;
    private JLabel lWarningZone;
    private JLabel lleftBorder;
    private JLabel lrightBorder;

    private JTextField input_leftBorder;
    private JTextField input_rightBorder;
    private JTextField input_maxdepth;

    private JFormattedTextField input_minDepth;
    private JFormattedTextField input_warning_zone;
    private JFormattedTextField input_zone1;
    private JFormattedTextField input_zone2;
    private JFormattedTextField input_zone3;

    private CollisionAvoidance cAvoid;
    private boolean showVideo = false;

    private DecimalFormat depthFormat;

    public CollisionAvoidanceUI() {
        super(TITLE);
        this.thisFrame = this;

        this.setSize(new Dimension(850,550));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        depthFormat = new DecimalFormat("#.##", dfs);


        this.bottomPanel       = new JPanel();
        this.leftCol        = new JPanel();
        this.rightCol       = new JPanel();

        this.b_StartKinect  = new JButton("Start detection");
        this.b_StartKinect.addActionListener(startDetectionAction());

        this.b_calcMaxDistance = new JButton("Calc max Distance");
        this.b_calcMaxDistance.addActionListener(calcMaxDistanceAction());

        this.b_OpenFeedBackSystemUI = new JButton("Feedback System");
        this.b_OpenFeedBackSystemUI.addActionListener(openFeedBackSystemUIAction());
        this.b_OpenFeedBackSystemUI.setHorizontalAlignment(SwingConstants.RIGHT);;

        this.lmaxDepth      = new JLabel("Max Depth: ");
        this.lminDepth      = new JLabel("Min Depth: "+CollisionAvoidance.MIN_DEPTH+" m ");
        this.lZone1     = new JLabel("Zone 1: ");
        this.lZone2     = new JLabel("Zone 2: ");
        this.lZone3     = new JLabel("Zone 3: ");
        this.lmaxDepthCalc  = new JLabel("Max Distance: ");
        this.lWarningZone   = new JLabel("Warning: ");
        this.lmaxDepthValue = new JLabel();
        //this.lleftBorder    = new JLabel("Left: ");
        //this.lrightBorder   = new JLabel("Right: ");

        this.input_maxdepth     = new JTextField("1.6", 3);
        this.input_zone1        = new JFormattedTextField();
        this.input_zone2        = new JFormattedTextField();
        this.input_zone3        = new JFormattedTextField();
        this.input_warning_zone = new JFormattedTextField();
        //this.input_leftBorder   = new JTextField("0", 5);
        //this.input_rightBorder  = new JTextField("0", 5);

        //this.input_maxdepth.setHorizontalAlignment(SwingConstants.CENTER);
        //this.input_leftBorder.setHorizontalAlignment(SwingConstants.CENTER);
        //this.input_rightBorder.setHorizontalAlignment(SwingConstants.CENTER);

        //Setup input fields
        this.input_warning_zone.setColumns(3);
        this.input_warning_zone.setHorizontalAlignment(SwingConstants.CENTER);

        this.input_zone1.setColumns(3);
        this.input_zone1.setHorizontalAlignment(SwingConstants.CENTER);

        this.input_zone2.setColumns(3);
        this.input_zone2.setHorizontalAlignment(SwingConstants.CENTER);

        this.input_zone3.setColumns(3);
        this.input_zone3.setHorizontalAlignment(SwingConstants.CENTER);


        //Set default Zone values
        input_warning_zone.setText("0.3");
        input_warning_zone.addPropertyChangeListener(getmaxDepthListener());

        input_zone1.setText("0.3");
        input_zone1.addPropertyChangeListener(getmaxDepthListener());

        input_zone2.setText("0.3");
        input_zone2.addPropertyChangeListener(getmaxDepthListener());

        input_zone3.setText("0.3");
        input_zone3.addPropertyChangeListener(getmaxDepthListener());


        //Setup JPanels
        this.bottomPanel.setLayout(new GridLayout(1,2));
        this.leftCol.setLayout(new GridLayout(5,2));
        this.rightCol.setLayout(new FlowLayout(FlowLayout.TRAILING));

        //Setup bottomPanel
        JPanel col1_row1 = new JPanel();
        col1_row1.add(lminDepth);
        leftCol.add(col1_row1);

        JPanel col2_row1 = new JPanel();
        col2_row1.add(lmaxDepthCalc);
        col2_row1.add(lmaxDepthValue);
        leftCol.add(col2_row1);

        JPanel col1_row2 = new JPanel();
        col1_row2.add(lWarningZone);
        col1_row2.add(input_warning_zone);
        leftCol.add(col1_row2);

        JPanel col2_row2 = new JPanel();
        col2_row2.add(lZone1);
        col2_row2.add(input_zone1);
        leftCol.add(col2_row2);

        JPanel col1_row3 = new JPanel();
        col1_row3.add(lZone2);
        col1_row3.add(input_zone2);
        leftCol.add(col1_row3);

        JPanel col2_row3 = new JPanel();
        col2_row3.add(lZone3);
        col2_row3.add(input_zone3);
        leftCol.add(col2_row3);

        leftCol.add(new JLabel(""));
        JPanel col2_row4 = new JPanel();
        col2_row4.add(b_calcMaxDistance);
        leftCol.add(col2_row4);


        this.rightCol.add(b_StartKinect);
        this.rightCol.add(b_OpenFeedBackSystemUI);

        this.bottomPanel.add(leftCol);
        this.bottomPanel.add(rightCol);

        viewport = new ViewerPanel3D();
        viewport.setShowVideo(showVideo);
        this.add(viewport, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        lmaxDepthValue.setText(getMaxDepth()+" m ");

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


    /**
     * ActionListener for starting the Debug-Window fo the Feedback System
     * @return
     */
    private ActionListener openFeedBackSystemUIAction() {
        return e -> {
            if(cAvoid != null){
                cAvoid.close();
            }

            fbsUI = new FeedbackSystemUI();
        };
    }


    /**
     * Start collision detection
     * @return
     */
    private ActionListener startDetectionAction() {
        return e -> {

            if( cAvoid == null ){
                System.out.println("Start Collision avoidance system");
                cAvoid = new CollisionAvoidance(
                        viewport,
                        Float.parseFloat(getMaxDepth()),
                        getFloatValue(input_warning_zone),
                        getFloatValue(input_zone1),
                        getFloatValue(input_zone2),
                        getFloatValue(input_zone3)
                );
                cAvoid.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.INFRARED|J4KSDK.XYZ|J4KSDK.UV);
                if(cAvoid.isInitialized()){
                    b_StartKinect.setText("Update");
                } else {
                    cAvoid.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.INFRARED|J4KSDK.XYZ|J4KSDK.UV);
                }
            } else if(!cAvoid.isInitialized()){
                System.out.println("Re-Initialize Kinect");
                cAvoid.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.INFRARED|J4KSDK.XYZ|J4KSDK.UV);
                cAvoid.update(
                        Float.parseFloat(getMaxDepth()),
                        showVideo,
                        getFloatValue(input_warning_zone),
                        getFloatValue(input_zone1),
                        getFloatValue(input_zone2),
                        getFloatValue(input_zone3)
                    );
            } else {
                cAvoid.update(
                        Float.parseFloat(getMaxDepth()),
                        showVideo,
                        getFloatValue(input_warning_zone),
                        getFloatValue(input_zone1),
                        getFloatValue(input_zone2),
                        getFloatValue(input_zone3)
                );
            }

            System.out.println();
            System.out.println("Update Collision Avoidance settings");
            System.out.println("Max Depth: "+Float.parseFloat(getMaxDepth()));
            System.out.println("Zone Warning: "+getFloatValue(input_warning_zone));
            System.out.println("ZOne 1: "+getFloatValue(input_zone1));
            System.out.println("Zone 2: "+getFloatValue(input_zone2));
            System.out.println("Zone 3: "+getFloatValue(input_zone3));
            System.out.println();
        };
    }


    /**
     * Updates the max distance(depth) field in the window
     * @return
     */
    private ActionListener calcMaxDistanceAction() {
        return e -> {
            lmaxDepthValue.setText(getMaxDepth()+" m ");
        };
    }


    /**
     * When input field gets changed, it calculates a new max depth
     * @return
     */
    private PropertyChangeListener getmaxDepthListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if("value".equals(propertyName)){
                    String md = getMaxDepth();
                    if(md.isEmpty()){
                        lmaxDepthValue.setText(" -- ");
                    } else {
                        lmaxDepthValue.setText(getMaxDepth()+" m ");
                    }
                }
            }
        };
    }


    /**
     * Calculates the max depth value
     * @return
     */
    private String getMaxDepth(){
        Float maxDepth;

        maxDepth = CollisionAvoidance.MIN_DEPTH;
        try {
            if (input_warning_zone.getText().isEmpty() || "".equals(input_warning_zone.getText())) return "";
            maxDepth = (Float) (maxDepth + Float.parseFloat(input_warning_zone.getText()));

            if (input_zone1.getText().isEmpty() || "".equals(input_zone1.getText())) return "";
            maxDepth = (Float) (maxDepth + Float.parseFloat(input_zone1.getText()));

            if (input_zone2.getText().isEmpty() || "".equals(input_zone2.getText())) return "";
            maxDepth = (Float) (maxDepth + Float.parseFloat(input_zone2.getText()));

            if (input_zone3.getText().isEmpty() || "".equals(input_zone3.getText())) return "";
            maxDepth = (Float) (maxDepth + Float.parseFloat(input_zone3.getText()));

        } catch (Exception e){
            return "";
        }

        String md = depthFormat.format(maxDepth);
        return md;
    }


    /**
     * Returns a nice formated float value
     * @param tfield
     * @return
     */
    private float getFloatValue(JFormattedTextField tfield){
        String tTextValue   = tfield.getText();
        Float tFloatValue   = Float.parseFloat(tTextValue);
        String tFormated    = depthFormat.format(tFloatValue);
        return Float.parseFloat(tFormated);
    }
}