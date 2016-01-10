import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;


public class FeedbackSystem implements SerialPortEventListener{
    private final static String TAG = "FeedbackSystem";
    private final static String appName = "CollisionAvoidance";

    private static final String PORT_NAMES[] = new PortNames().getPortNames();

    SerialPort serialPort = null;
    private BufferedReader input;
    private OutputStream output;

    private static final int TIME_OUT = 1000; // Port open timeout
    private static final int DATA_RATE = 9600; // Arduino serial port

    // Vibration intensity steps
    // Where the values 10-19 can be used for distances from 1m to 1,9m
    // Where the values 20-29 can be used for distances from 2m to 2,9m
    // Where the values 30-39 can be used for distances from 3m to 3,9m
    private final static byte DEFAULT_MOTOR_WARNING  = 100;  // Arduino-Value: 255
    private final static byte DEFAULT_MOTOR_OFF      = 0;    // Arduino-Value: 0
    private final static byte DEFAULT_METER_1        = 10;   // Arduino-Value: 200;
    private final static byte DEFAULT_METER_2        = 20;   // Arduino-Value: 150;
    private final static byte DEFAULT_METER_3        = 30;   // Arduino-Value: 100;

    public static byte MOTOR_WARNING;
    public static byte MOTOR_OFF;
    public static byte METER_1;
    public static byte METER_2;
    public static byte METER_3;

    public FeedbackSystem() {
        readFBSProperties();
    }


    /**
     * Searches for an arduino serial port device and try's to establish a connection
     * @return
     */
    public boolean initialize() {
        try {
            CommPortIdentifier portId = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            // Enumerate system ports and try connecting to Arduino over each
            System.out.println( "Trying:");
            while (portId == null && portEnum.hasMoreElements()) {

                // Iterate through your host computer's serial port IDs
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                System.out.println( "   port" + currPortId.getName() );
                for (String portName : PORT_NAMES) {
                    if ( currPortId.getName().equals(portName)
                            || currPortId.getName().startsWith(portName)) {

                        // Try to connect to the Arduino on this port
                        //
                        // Open serial port
                        serialPort = (SerialPort)currPortId.open(appName, TIME_OUT);
                        portId = currPortId;
                        System.out.println( "Connected on port" + currPortId.getName() );
                        break;
                    }
                }
            }

            if (portId == null || serialPort == null) {
                System.out.println("Oops... Could not connect to Arduino");
                return false;
            }


            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);


            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            // Give the Arduino some time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            return true;

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Send a byte[] array  to the device connected to serial por
     * @param data
     */
    public void sendByteArray(byte[] data) {
        try {
            System.out.println("Sending byte[] array: '" + data.toString() +"'");
            output = serialPort.getOutputStream();
            output.write(data);
        }
        catch (Exception e) {
            System.err.println(e.toString());
            System.exit(0);
        }
    }


    /**
     * Send integer data to the device connected to serial por
     * @param data
     */
    public void sendInt(Integer data) {
        try {
            System.out.println("Sending int data: '" + data +"'");
            output = serialPort.getOutputStream();
            output.write(data.byteValue());
        }
        catch (Exception e) {
            System.err.println(e.toString());
            System.exit(0);
        }
    }


    /**
     * Send string data to the device connected to serial por
     * @param data
     */
    public void sendString(String data) {
        try {
            System.out.println(TAG+" --> Sending string data: '" + data +"'");
            System.out.println(TAG+" --> "+data.getBytes().length);
            output = serialPort.getOutputStream();
            output.write(data.getBytes());
        }
        catch (Exception e) {
            System.err.println(e.toString());
            System.exit(0);
        }
    }


    /**
     * This should be called when you stop using the port
     */
    public synchronized void close() {
        if ( serialPort != null ) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }


    /**
     * Handle serial port events
     * @param oEvent
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        //System.out.println("Event received: " + oEvent.toString());
        try {
            switch (oEvent.getEventType() ) {
                case SerialPortEvent.DATA_AVAILABLE:
                    if ( input == null ) {
                        input = new BufferedReader(
                                new InputStreamReader(
                                        serialPort.getInputStream()));
                    }
                    String inputLine = input.readLine();
                    System.out.println(inputLine);
                    break;

                default:
                    break;
            }
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }


    /**
     * Checks current operating system and generates os specific list of serial port names
     */
    private static class PortNames {
        private static String OS;

        PortNames() {
            OS = System.getProperty("os.name").toLowerCase();
        }

        private static boolean isWindows() {
            return (OS.indexOf("win") >= 0);
        }

        private static boolean isMac() {
            return (OS.indexOf("mac") >= 0);
        }

        private static boolean isUnix() {
            return (OS.indexOf("nux") >= 0);
        }

        public String[] getPortNames() {
            if (isWindows()) {
                System.out.println(TAG+" --> Operating System: Windows");
                return new String[]{ "COM3" };
            }

            if (isMac()) {
                System.out.println(TAG+" --> Operating System: Mac OSX");
                return  new String[] { "/dev/tty.usbmodem" };
            }

            if (isUnix()) {
                System.out.println(TAG+" --> Operating System: Linux");
                return new String[] {
                    "/dev/usbdev",
                    "/dev/tty",
                    "/dev/serial"
                };
            }

            System.out.println(TAG+" --> ERROR: No matching OS found!");
            return new String[] {};
        }
    }


    private void readFBSProperties() throws IllegalArgumentException {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("feedbacksystem.properties");

            // load a properties file
            prop.load(input);

            System.out.println("Found Properties: "+prop.size());

            MOTOR_WARNING   = convPropValue(prop.getProperty("warning"));
            MOTOR_OFF       = convPropValue(prop.getProperty("off"));
            METER_1         = convPropValue(prop.getProperty("meter_1"));
            METER_2         = convPropValue(prop.getProperty("meter_2"));
            METER_3         = convPropValue(prop.getProperty("meter_3"));

            System.out.println("New FeedbackSystem-Protocol intensity values loaded!");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Using default values");
            MOTOR_WARNING   = DEFAULT_MOTOR_WARNING;
            MOTOR_OFF       = DEFAULT_MOTOR_OFF;
            METER_1         = DEFAULT_METER_1;
            METER_2         = DEFAULT_METER_2;
            METER_3         = DEFAULT_METER_3;

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte convPropValue(String StringValue) throws Exception {
        Byte b = new Byte(StringValue);
        if( b >= 0 && b <= 127) {
            return b;
        } else {
            throw new Exception("Propertie "+StringValue+" out of range (0 - 127)");
        }
    }
}


