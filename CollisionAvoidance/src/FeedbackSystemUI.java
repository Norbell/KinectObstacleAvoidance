import javafx.scene.control.RadioButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FeedbackSystemUI extends JFrame{
    private final static String TITLE = "Feedback System UI";
    private JFrame thisFrame;

    private FeedbackSystem fbs;

    private JPanel globalPanel;
    private JPanel fbsControlPanel;
    private JScrollPane consoleLogPanel;

    private JTextArea   tarea_serialConsoleLog;

    public FeedbackSystemUI() {
        super(TITLE);
        this.thisFrame = this;

        this.globalPanel = new JPanel();
        this.globalPanel.setLayout(new BorderLayout());

        this.setSize(new Dimension(500, 300));
        this.setLocation(200,100);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.tarea_serialConsoleLog = new JTextArea();

        //Configure SerialConsoleLog-Area
        tarea_serialConsoleLog.setEditable(false);
        tarea_serialConsoleLog.setBackground(Color.WHITE);
        tarea_serialConsoleLog.setRows(6);
        tarea_serialConsoleLog.setFont(new Font("monospaced", Font.PLAIN, 12));

        //Add components to fbsControlPanel
        this.fbs = FeedbackSystem.getInstance();
        this.fbsControlPanel = new FeedbackSystemUIControlPanel();

        //Add components to consoleLogPanel
        this.consoleLogPanel  = new JScrollPane(tarea_serialConsoleLog);

        //Add Sub-Panels to Global-Panel
        this.globalPanel.add(fbsControlPanel,BorderLayout.CENTER);
        //this.globalPanel.add(consoleLogPanel, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(fbs.isInitialized()){
                    fbs.close();
                } else {
                    System.out.println("SerialPort already disconnected");
                }
                thisFrame.dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                thisFrame.setVisible(false);
            }
        });

        this.add(globalPanel);
        setVisible(true);
    }


    public void closeWindow() {
        WindowEvent closingEvent = new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
    }
}
