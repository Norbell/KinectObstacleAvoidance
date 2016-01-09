import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;


public class FeedbackSystemUIControlPanel extends JPanel{
    private FeedbackSystem fbs;

    private JScrollBar  controlPanel;
    private JPanel      buttonPanel;

    private JTextField  ifield_IntensMotor1;
    private JTextField  ifield_IntensMotor2;
    private JTextField  ifield_IntensMotor3;

    private JButton     b_AllMotorsOff;
    private JButton     b_SendSerialMsg;

    private ButtonGroup leftMotorGroup;
    private ButtonGroup centerMotorGroup;
    private ButtonGroup rightMotorGroup;

    private JRadioButton[] leftMotorRBList      = new JRadioButton[5];
    private JRadioButton[] centerMotorRBList    = new JRadioButton[5];
    private JRadioButton[] rightMotorRBList     = new JRadioButton[5];

    private JLabel columMotor1;
    private JLabel columMotor2;
    private JLabel columMotor3;
    private JLabel rowWarning;
    private JLabel rowOff;
    private JLabel row1Meter;
    private JLabel row2Meter;
    private JLabel row3Meter;


    public FeedbackSystemUIControlPanel(FeedbackSystem fbs) {
        super();
        this.fbs = fbs;

        this.controlPanel   = new JScrollBar();
        this.buttonPanel    = new JPanel();

        this.b_AllMotorsOff     = new JButton("All motors off");
        this.b_SendSerialMsg    = new JButton("Send values");

        this.leftMotorGroup     = new ButtonGroup();
        this.centerMotorGroup   = new ButtonGroup();
        this.rightMotorGroup    = new ButtonGroup();

        //Left Motor
        this.leftMotorRBList[0] = new JRadioButton();   //Off
        this.leftMotorRBList[1] = new JRadioButton();   //Warning
        this.leftMotorRBList[2] = new JRadioButton();   //1 Meter
        this.leftMotorRBList[3] = new JRadioButton();   //2 Meter
        this.leftMotorRBList[4] = new JRadioButton();   //3 Meter
        this.leftMotorRBList = setRBNames(leftMotorRBList, "l");

        //Center Motor
        this.centerMotorRBList[0]   = new JRadioButton();   //Off
        this.centerMotorRBList[1]   = new JRadioButton();   //Warning
        this.centerMotorRBList[2]   = new JRadioButton();   //1 Meter
        this.centerMotorRBList[3]   = new JRadioButton();   //2 Meter
        this.centerMotorRBList[4]   = new JRadioButton();   //3 Meter
        this.centerMotorRBList      = setRBNames(centerMotorRBList, "c");

        //Right Motor
        this.rightMotorRBList[0]    = new JRadioButton();   //Off
        this.rightMotorRBList[1]    = new JRadioButton();   //Warning
        this.rightMotorRBList[2]    = new JRadioButton();   //1 Meter
        this.rightMotorRBList[3]    = new JRadioButton();   //2 Meter
        this.rightMotorRBList[4]    = new JRadioButton();   //3 Meter
        this.rightMotorRBList       = setRBNames(rightMotorRBList, "r");

        //Grid Labels
        this.columMotor1 = new JLabel("Left");
        this.columMotor2 = new JLabel("Center");
        this.columMotor3 = new JLabel("Right");
        this.rowWarning = new JLabel("Warning (255)");
        this.rowOff     = new JLabel("Off");
        this.row1Meter  = new JLabel("1 Meter (200");
        this.row2Meter  = new JLabel("2 Meter (175)");
        this.row3Meter  = new JLabel("3 Meter (150");


        //Defines the ActionListeners for the buttons
        b_SendSerialMsg.addActionListener(sendSerialMsgAction());
        b_AllMotorsOff.addActionListener(allMotorsOffAction());

        //Align Elements in Grid
        alignRadioButtons();
        columMotor1.setHorizontalAlignment(SwingConstants.CENTER);
        columMotor2.setHorizontalAlignment(SwingConstants.CENTER);
        columMotor3.setHorizontalAlignment(SwingConstants.CENTER);

        //Add RadioButton-Lists to individual ButtoGroups
        createRBGroups();

        //Set Buttons to default position
        setAllToOff();

        //Assign Buttons to button-Panel
        this.buttonPanel.add(b_SendSerialMsg);
        this.buttonPanel.add(b_AllMotorsOff);

        //Assign JComponents to grid
        controlPanel.setLayout(new GridLayout(6,4));
        //Row 1
        controlPanel.add(new JLabel());
        controlPanel.add(columMotor1);
        controlPanel.add(columMotor2);
        controlPanel.add(columMotor3);

        //Row 2 - Off
        controlPanel.add(rowOff);
        controlPanel.add(leftMotorRBList[0]);
        controlPanel.add(centerMotorRBList[0]);
        controlPanel.add(rightMotorRBList[0]);

        //Row 3 - Warning
        controlPanel.add(rowWarning);
        controlPanel.add(leftMotorRBList[1]);
        controlPanel.add(centerMotorRBList[1]);
        controlPanel.add(rightMotorRBList[1]);

        //Row 4 - 1 Meter
        controlPanel.add(row1Meter);
        controlPanel.add(leftMotorRBList[2]);
        controlPanel.add(centerMotorRBList[2]);
        controlPanel.add(rightMotorRBList[2]);

        //Row 5 - 2 Meter
        controlPanel.add(row2Meter);
        controlPanel.add(leftMotorRBList[3]);
        controlPanel.add(centerMotorRBList[3]);
        controlPanel.add(rightMotorRBList[3]);

        //Row 6 - 3 Meter
        controlPanel.add(row3Meter);
        controlPanel.add(leftMotorRBList[4]);
        controlPanel.add(centerMotorRBList[4]);
        controlPanel.add(rightMotorRBList[4]);

        controlPanel.setAlignmentX(CENTER_ALIGNMENT);
        controlPanel.setMinimumSize(new Dimension(400,200));

        this.setLayout(new BorderLayout());
        this.add(controlPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }


    /**
     * Creates ActionListener for Button "Turn all off"
     * @return
     */
    private ActionListener allMotorsOffAction() {
        return e -> {
            byte[] msg = {
                    FeedbackSystem.MOTOR_OFF,
                    FeedbackSystem.MOTOR_OFF,
                    FeedbackSystem.MOTOR_OFF
            };

            fbs.sendByteArray(msg);
            setAllToOff();
        };
    }


    /**
     * Creates the ActionListener for the "Send values" Button
     * @return
     */
    private ActionListener sendSerialMsgAction() {
        return e -> {
            byte[] msg = {
                getRadioButtonValue(leftMotorRBList),
                getRadioButtonValue(centerMotorRBList),
                getRadioButtonValue(rightMotorRBList)
            };

            fbs.sendByteArray(msg);
        };
    }


    /**
     * Adds each RadioButton list to its independent ButtonGrup
     */
    private void createRBGroups() {
        for (int i = 0; i < leftMotorRBList.length; i++) {
            leftMotorGroup.add(leftMotorRBList[i]);
        }

        for (int i = 0; i < centerMotorRBList.length; i++) {
            centerMotorGroup.add(centerMotorRBList[i]);
        }

        for (int i = 0; i < rightMotorRBList.length; i++) {
            rightMotorGroup.add(rightMotorRBList[i]);
        }
    }


    /**
     * Builds the RadioButton names using the first letter of the list, and the array index of the JRadioButton
     * @param list
     * @param listLetter
     * @return
     */
    private JRadioButton[] setRBNames(JRadioButton[] list, String listLetter){
        for(int i=0; i < list.length; i++) {
            //Example: l0,l1,l2,l3,l4
            list[i].setName(listLetter+i);
        }

        return list;
    }


    /**
     * Align all RadioButtons horizontal center in Grid-Layout
     */
    private void alignRadioButtons() {
        for(int i=0; i < leftMotorRBList.length; i++) {
            leftMotorRBList[i].setHorizontalAlignment(SwingConstants.CENTER);
        }

        for(int i=0; i < centerMotorRBList.length; i++) {
            centerMotorRBList[i].setHorizontalAlignment(SwingConstants.CENTER);
        }

        for(int i=0; i < rightMotorRBList.length; i++) {
            rightMotorRBList[i].setHorizontalAlignment(SwingConstants.CENTER);
        }
    }


    /**
     * Set all Radio-Buttons to off
     */
    private void setAllToOff(){
        //Motor 1 - Left
        for(int i=0; i < leftMotorRBList.length; i++) {
            if(i == 0) {
                leftMotorRBList[i].setSelected(true);
            } else {
                leftMotorRBList[i].setSelected(false);
            }
        }

        //Motor 2 - Center
        for(int i=0; i < centerMotorRBList.length; i++) {
            if(i == 0) {
                centerMotorRBList[i].setSelected(true);
            } else {
                centerMotorRBList[i].setSelected(false);
            }
        }

        //Motor 3 - Right
        for(int i=0; i < rightMotorRBList.length; i++) {
            if(i == 0) {
                rightMotorRBList[i].setSelected(true);
            } else {
                rightMotorRBList[i].setSelected(false);
            }
        }
    }


    /**
     * Checks if RadioButton is selected and gets corresponding FeedbackSystem value
     * @param jrbList
     * @return
     */
    private byte getRadioButtonValue(JRadioButton[] jrbList){
        for(int i=0; i < jrbList.length; i++) {
            //0 - Motor intensity - off
            if( i == 0 && jrbList[i].isSelected()) {
                return FeedbackSystem.MOTOR_OFF;
            }

            //1 - Motor intensity - Warning
            if( i == 1 && jrbList[i].isSelected()) {
                return FeedbackSystem.MOTOR_WARNING;
            }

            //2 - Motor intensity - 1 Meter
            if( i == 2 && jrbList[i].isSelected()) {
                return FeedbackSystem.METER_1;
            }

            //3 - Motor intensity - 2 Meter
            if( i == 3 && jrbList[i].isSelected()) {
                return FeedbackSystem.METER_2;
            }

            //4 - Motor intensity - 3 Meter
            if( i == 4 && jrbList[i].isSelected()) {
                return FeedbackSystem.METER_3;
            }
        }

        return 0;
    }
}

