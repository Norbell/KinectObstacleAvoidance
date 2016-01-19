package CollisionAvoidance;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;


public class FeedbackSystem implements SerialPortEventListener{
    private final static String TAG = "CollisionAvoidance.FeedbackSystem";
    private final static String appName = "CollisionAvoidance.CollisionAvoidanceUI";

    private static final String PORT_NAMES[] = new PortNames().getPortNames();

    SerialPort serialPort = null;
    private BufferedReader input;
    private OutputStream output;

    private static final int TIME_OUT = 1000; // Port open timeout
    private static final int DATA_RATE = 9600; // Arduino serial port

    private boolean debugMode = false;

    // Vibration intensity steps
    // Where the values 10-19 can be used for distances from 1m to 1,9m
    // Where the values 20-29 can be used for distances from 2m to 2,9m
    // Where the values 30-39 can be used for distances from 3m to 3,9m
    public final static byte ALLOFF                = 127;
    private final static byte DEFAULT_OFF = 0;    // Arduino-Value: 0
    private final static byte DEFAULT_ZONE1 = 1;
    private final static byte DEFAULT_ZONE2 = 2;
    private final static byte DEFAULT_ZONE3 = 3;
    private final static byte DEFAULT_ZONE4 = 4;
    private final static byte DEFAULT_ZONE5 = 5;
    private final static byte DEFAULT_ZONE6 = 6;
    private final static byte DEFAULT_ZONE7 = 7;
    private final static byte DEFAULT_ZONE8 = 8;
    private final static byte DEFAULT_WARNING = 9;    // Arduino-Value: 255

    public final static byte MOTOR_BASE_LEFT       = 10;
    public final static byte MOTOR_BASE_CENTER     = 20;
    public final static byte MOTOR_BASE_RIGHT      = 30;

    public static byte OFF;
    public static byte ZONE1;
    public static byte ZONE2;
    public static byte ZONE3;
    public static byte ZONE4;
    public static byte ZONE5;
    public static byte ZONE6;
    public static byte ZONE7;
    public static byte ZONE8;
    public static byte WARNING;

    private static FeedbackSystem currentInstance = new FeedbackSystem();
    private boolean isInitialized = false;


    private FeedbackSystem() {
        readFBSProperties();
        isInitialized = isInitialized();
    }

    /**
     * Returns the Singletons-Instance of this class
     * @return
     */
    public static FeedbackSystem getInstance() {
        return currentInstance;
    }

    /**
     * Check if SerialPort is already initialized;
     * If not, then it will try to initialize a new connection to the SerialPort
     * @return
     */
    public boolean isInitialized() {
        if ( !isInitialized ) {
            isInitialized = initializeSerialPort();
            System.out.println("Initialize SerialPort: "+isInitialized);
        }

        return isInitialized;
    }


    /**
     * Searches for an arduino serial port device and try's to establish a connection
     * @return
     */
    private boolean initializeSerialPort() {
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
     * Send Serialport messages. Creates a new thread for each message
     * @param msg
     */
    public void sendMsg(Byte byteMsg) {
        if (debugMode) System.out.println("\n Bulk SerialPort-Message");
        sendToSerialPort(byteMsg);
    }


    /**
     * This method gets called when only on motor needs to get updated
     * @param msg
     * @param base
     */
    public void sendMsg(Byte msg, Byte base) {
        //System.out.println("\n Single Motor Update");
        Byte byteMsg = 0;

        if( base == ALLOFF) {
            System.out.println("All motors off!");
            sendToSerialPort(ALLOFF);
            return;
        }

        //Left Motor
        if( base == MOTOR_BASE_LEFT) {
            byteMsg = (byte)(base + msg);
            if (debugMode) System.out.println("Left Motor: "+byteMsg);
            sendToSerialPort(byteMsg);
            return;
        }

        //Center Motor
        if( base == MOTOR_BASE_CENTER ) {
            byteMsg = (byte)(base + msg);
            if (debugMode) System.out.println("Center Motor: "+byteMsg);
            sendToSerialPort(byteMsg);
            return;
        }

        //Right Motor
        if( base == MOTOR_BASE_RIGHT ) {
            byteMsg = (byte)(base + msg);
            if (debugMode) System.out.println("Right Motor: "+byteMsg);
            sendToSerialPort(byteMsg);
            return;
        }
    }


    /**
     * Send intensity value to SerialPort
     * @param msg
     */
    private void sendToSerialPort(Byte msg){
        try {
            if (debugMode) System.out.println("Sending Byte-Value: '" + msg.toString() +"'");
            output = serialPort.getOutputStream();
            output.write(msg);
        }
        catch (Exception e) {
            System.err.println(e.toString());
            System.exit(0);
        }
    }


    /**
     * Handle serial port events
     * @param oEvent
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (debugMode) System.out.println("Event: "+ oEvent.getEventType() +" --> "+ oEvent.toString());
        try {
            switch (oEvent.getEventType() ) {
                case SerialPortEvent.DATA_AVAILABLE:
                    if ( input == null ) {
                        input = new BufferedReader(
                                new InputStreamReader(
                                        serialPort.getInputStream()));
                    }
                    if (input.ready()) {
                        String inputLine = input.readLine();
                        System.out.println(inputLine);
                    }
                    break;
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    System.out.println("Data send successfully!");
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            System.err.println(e.toString());
            System.err.println("Error in EventHandler!");
        }
    }


    /**
     * This should be called when you stop using the port
     */
    public synchronized void close() {
        if ( serialPort != null ) {
            serialPort.removeEventListener();
            serialPort.close();
            isInitialized = false;
            input = null;
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
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

            input = new FileInputStream("config/feedbacksystem.properties");

            // load a properties file
            prop.load(input);

            System.out.println("Found Properties: "+prop.size());

            WARNING = convPropValue(prop.getProperty("WARNING"));
            OFF     = convPropValue(prop.getProperty("OFF"));
            ZONE1   = convPropValue(prop.getProperty("ZONE1"));
            ZONE2   = convPropValue(prop.getProperty("ZONE2"));
            ZONE3   = convPropValue(prop.getProperty("ZONE3"));
            ZONE4   = convPropValue(prop.getProperty("ZONE4"));
            ZONE5   = convPropValue(prop.getProperty("ZONE5"));
            ZONE6   = convPropValue(prop.getProperty("ZONE6"));
            ZONE7   = convPropValue(prop.getProperty("ZONE7"));
            ZONE8   = convPropValue(prop.getProperty("ZONE8"));

            System.out.println("New CollisionAvoidance.FeedbackSystem-Protocol intensity values loaded!");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Using default values");
            OFF     = FeedbackSystem.DEFAULT_OFF;
            ZONE1   = FeedbackSystem.DEFAULT_ZONE1;
            ZONE2   = FeedbackSystem.DEFAULT_ZONE2;
            ZONE3   = FeedbackSystem.DEFAULT_ZONE3;
            ZONE4   = FeedbackSystem.DEFAULT_ZONE4;
            ZONE5   = FeedbackSystem.DEFAULT_ZONE5;
            ZONE6   = FeedbackSystem.DEFAULT_ZONE6;
            ZONE7   = FeedbackSystem.DEFAULT_ZONE7;
            ZONE8   = FeedbackSystem.DEFAULT_ZONE8;
            WARNING = FeedbackSystem.DEFAULT_WARNING;
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


