import javafx.scene.control.RadioButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FeedbackSystemUI extends JFrame{
    private final static String TITLE = "Feedback System UI";

    private FeedbackSystem fbsInstance;

    private JPanel globalPanel;
    private FeedbackSystemUIControlPanel fbsControlPanel;
    private JScrollPane consoleLogPanel;

    private JTextArea   tarea_serialConsoleLog;

    public FeedbackSystemUI(FeedbackSystem fbs) {
        super(TITLE);
        this.fbsInstance = fbs;

        this.globalPanel = new JPanel();
        this.globalPanel.setLayout(new BorderLayout());

        this.setSize(new Dimension(500, 400));
        this.setLocation(100,100);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.tarea_serialConsoleLog = new JTextArea();

        //Configure SerialConsoleLog-Area
        tarea_serialConsoleLog.setEditable(false);
        tarea_serialConsoleLog.setBackground(Color.WHITE);
        tarea_serialConsoleLog.setRows(6);
        tarea_serialConsoleLog.setFont(new Font("monospaced", Font.PLAIN, 12));

        //Add components to fbsControlPanel
        this.fbsControlPanel = new FeedbackSystemUIControlPanel(fbsInstance);

        //Add components to consoleLogPanel
        this.consoleLogPanel  = new JScrollPane(tarea_serialConsoleLog);

        //Add Sub-Panels to Global-Panel
        this.globalPanel.add(fbsControlPanel,BorderLayout.CENTER);
        this.globalPanel.add(consoleLogPanel, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
            }
        });

        this.add(globalPanel);
        this.setVisible(true);
    }
}
