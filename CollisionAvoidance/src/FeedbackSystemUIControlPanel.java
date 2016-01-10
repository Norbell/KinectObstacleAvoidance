import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class FeedbackSystemUIControlPanel extends JPanel{
    private FeedbackSystem fbs = null;

    private JPanel  controlPanel;
    private JPanel      buttonPanel;

    private JFormattedTextField input_LeftMotor;
    private JFormattedTextField input_CenterMotor;
    private JFormattedTextField input_RightMotor;

    private JButton     b_AllMotorsOff;
    private JButton     b_SendSerialMsg;

    private ButtonGroup leftMotorGroup;
    private ButtonGroup centerMotorGroup;
    private ButtonGroup rightMotorGroup;

    private JRadioButton[] leftMotorRBList      = new JRadioButton[5];
    private JRadioButton[] centerMotorRBList    = new JRadioButton[5];
    private JRadioButton[] rightMotorRBList     = new JRadioButton[5];

    private JLabel columMotorLeft;
    private JLabel columMotorCenter;
    private JLabel columMotorRight;
    private JLabel rowWarning;
    private JLabel rowOff;
    private JLabel row1Meter;
    private JLabel row2Meter;
    private JLabel row3Meter;
    private JLabel rowCustomize;


    public FeedbackSystemUIControlPanel(FeedbackSystem fbs) {
        super();
        this.fbs = fbs;

        this.controlPanel   = new JPanel();
        this.buttonPanel    = new JPanel();

        this.b_AllMotorsOff     = new JButton("All motors off");
        this.b_SendSerialMsg    = new JButton("Send values");

        this.leftMotorGroup     = new ButtonGroup();
        this.centerMotorGroup   = new ButtonGroup();
        this.rightMotorGroup    = new ButtonGroup();

        this.input_LeftMotor    = getFilterdTField();
        this.input_CenterMotor  = getFilterdTField();
        this.input_RightMotor   = getFilterdTField();

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
        this.columMotorLeft     = new JLabel("Left", SwingConstants.CENTER);
        this.columMotorCenter   = new JLabel("Center", SwingConstants.CENTER);
        this.columMotorRight    = new JLabel("Right", SwingConstants.CENTER);
        this.rowWarning = new JLabel("Warning", SwingConstants.RIGHT);
        this.rowOff     = new JLabel("Off", SwingConstants.RIGHT);
        this.row1Meter  = new JLabel("1 Meter", SwingConstants.RIGHT);
        this.row2Meter  = new JLabel("2 Meter", SwingConstants.RIGHT);
        this.row3Meter  = new JLabel("3 Meter", SwingConstants.RIGHT);
        this.rowCustomize   = new JLabel("Customize", SwingConstants.RIGHT);


        //Defines the ActionListeners for the buttons
        b_SendSerialMsg.addActionListener(sendSerialMsgAction());
        b_AllMotorsOff.addActionListener(allMotorsOffAction());

        //Align Elements in Grid
        setupRadioButtons();

        //Add RadioButton-Lists to individual ButtoGroups
        createRBGroups();

        //Set Buttons to default position
        setAllToOff();

        //Assign Buttons to button-Panel
        this.buttonPanel.add(b_SendSerialMsg);
        this.buttonPanel.add(b_AllMotorsOff);


        //Assign JComponents to grid
        controlPanel.setLayout(new GridLayout(7,4));
        //Row 1
        controlPanel.add(new JLabel("",SwingConstants.CENTER));
        controlPanel.add(columMotorLeft);
        controlPanel.add(columMotorCenter);
        controlPanel.add(columMotorRight);

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

        //Row 7 - Customize
        controlPanel.add(rowCustomize);
        controlPanel.add(input_LeftMotor);
        controlPanel.add(input_CenterMotor);
        controlPanel.add(input_RightMotor);


        controlPanel.setAlignmentX(CENTER_ALIGNMENT);
        controlPanel.setMinimumSize(new Dimension(400,250));

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
            byte left   = 0;
            byte center = 0;
            byte right  = 0;

            try {
                //Check LeftRow
                if(isTFieldDiffFromRButton(leftMotorRBList, input_LeftMotor))  {
                    left = getByteOfTField(input_LeftMotor);
                    leftMotorGroup.clearSelection();
                    System.out.println("Left is different");
                } else {
                    left = getRadioButtonValue(leftMotorRBList);
                }

                //Check CenterRow
                if(isTFieldDiffFromRButton(centerMotorRBList, input_CenterMotor)) {
                    center = getByteOfTField(input_CenterMotor);
                    centerMotorGroup.clearSelection();
                    System.out.println("Center is different");
                } else {
                    center = getRadioButtonValue(centerMotorRBList);
                }

                //Check RightRow
                if(isTFieldDiffFromRButton(rightMotorRBList, input_RightMotor)) {
                    right = getByteOfTField(input_RightMotor);
                    rightMotorGroup.clearSelection();
                    System.out.println("Right is different");
                } else {
                    right = getRadioButtonValue(rightMotorRBList);
                }

            } catch (Exception ex) {
                System.out.println(ex.toString());
                System.out.println(ex.getMessage());
            }

            byte[] msg = {left, center, right};
            fbs.sendByteArray(msg);
        };
    }


    /**
     * Defines an ActionLister for RadioButtons.
     * When ActionListener gets triggered it copy's the value of the currently selected RadioButton into the CustomTextField
     * @return
     */
    private ActionListener getRBAction() {
        return e -> {
            Byte value;

            //Left Row/Motor
            value = getRadioButtonValue(leftMotorRBList);
            input_LeftMotor.setText(value.toString());

            //Center Row/Motor
            value = getRadioButtonValue(centerMotorRBList);
            input_CenterMotor.setText(value.toString());

            //Right Row/Motor
            value = getRadioButtonValue(rightMotorRBList);
            input_RightMotor.setText(value.toString());
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


    private boolean isTFieldDiffFromRButton(JRadioButton[] jrbList, JTextField tField) throws Exception {
        for(JRadioButton aRButton : jrbList) {
            Byte tFieldByte = getByteOfTField(tField);
            Byte rbByte     = getRadioButtonValue(jrbList);

            if(!tFieldByte.equals(rbByte) && tFieldByte >= 0 && tFieldByte <= 127){
                return true;
            } else if(tFieldByte.equals(rbByte) && tFieldByte >= 0 && tFieldByte <= 127) {
                return false;
            } else {
                throw new Exception("Customized value has to be between 0 and 127");
            }
        }
        return false;
    }


    private byte getByteOfTField(JTextField tField) {
        Byte b = new Byte(tField.getText());
        return b;
    }


    /**
     * Align all RadioButtons horizontal center in Grid-Layout
     */
    private void setupRadioButtons() {
        for (JRadioButton aLeftMotorRBList : leftMotorRBList) {
            aLeftMotorRBList.setHorizontalAlignment(SwingConstants.CENTER);
            aLeftMotorRBList.addActionListener(getRBAction());
        }

        for (JRadioButton aCenterMotorRBList : centerMotorRBList) {
            aCenterMotorRBList.setHorizontalAlignment(SwingConstants.CENTER);
            aCenterMotorRBList.addActionListener(getRBAction());
        }

        for (JRadioButton aRightMotorRBList : rightMotorRBList) {
            aRightMotorRBList.setHorizontalAlignment(SwingConstants.CENTER);
            aRightMotorRBList.addActionListener(getRBAction());
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

        Byte offValue = FeedbackSystem.MOTOR_OFF;
        input_LeftMotor.setText(offValue.toString());
        input_CenterMotor.setText(offValue.toString());
        input_RightMotor.setText(offValue.toString());
    }


    /**
     * Creates an FormatedTextField with an applied DocumentFilter for numbers
     * @return
     */
    private JFormattedTextField getFilterdTField(){
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMaximumIntegerDigits(3);
        decimalFormat.setParseIntegerOnly(true);
        JFormattedTextField textField =  new JFormattedTextField(decimalFormat);
        textField.setHorizontalAlignment(JFormattedTextField.CENTER);
        return textField;
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

