import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileSystemLoopException;
import java.util.Enumeration;
import java.util.Properties;


public class FeedbackSystem implements SerialPortEventListener{
    private final static String TAG = "FeedbackSystem";
    private final static String appName = "CollisionAvoidanceUI";

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
    public final static byte ALLOFF                = 127;
    private final static byte DEFAULT_MOTOR_OFF     = 0;    // Arduino-Value: 0
    private final static byte DEFAULT_METER_1       = 1;
    private final static byte DEFAULT_METER_1C5     = 2;
    private final static byte DEFAULT_METER_2       = 3;
    private final static byte DEFAULT_METER_2C5     = 4;
    private final static byte DEFAULT_METER_3       = 5;
    private final static byte DEFAULT_METER_3C5     = 6;
    private final static byte DEFAULT_METER_4       = 7;
    private final static byte DEFAULT_METER_4C5     = 8;
    private final static byte DEFAULT_MOTOR_WARNING = 9;    // Arduino-Value: 255

    private final static byte MOTOR_BASE_LEFT       = 10;
    private final static byte MOTOR_BASE_CENTER     = 20;
    private final static byte MOTOR_BASE_RIGHT      = 30;

    public static byte MOTOR_OFF;
    public static byte METER_1;
    public static byte METER_1C5;
    public static byte METER_2;
    public static byte METER_2C5;
    public static byte METER_3;
    public static byte METER_3C5;
    public static byte METER_4;
    public static byte METER_4C5;
    public static byte MOTOR_WARNING;

    private static FeedbackSystem currentInstance = new FeedbackSystem();
    private boolean isInitialized = false;


    private FeedbackSystem() {
        readFBSProperties();
        isInitialized = initialize();
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
    public boolean initialize() {
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
    public void sendDataArray(Byte[] msg) {
        System.out.println("\n Bulk SerialPort-Message");
        for(int i=0; i < msg.length ;i++) {
            Byte byteMsg = 0;
            System.out.print("P:"+i+" V:"+msg[i]+" --> ");

            //Left Motor
            if( i == 0) {
                byteMsg = (byte)(MOTOR_BASE_LEFT+msg[i]);
                System.out.println("Left Motor: "+byteMsg);
                sendToSerialPort(byteMsg);
            }

            //Center Motor
            if( i == 1 ) {
                byteMsg = (byte)(MOTOR_BASE_CENTER+msg[i]);
                System.out.println("Center Motor: "+byteMsg);
                sendToSerialPort(byteMsg);
            }

            //Right Motor
            if( i == 2 ) {
                byteMsg = (byte)(MOTOR_BASE_RIGHT+msg[i]);
                System.out.println("Right Motor: "+byteMsg);
                sendToSerialPort(byteMsg);
            }
        }
    }


    /**
     * This method gets called when only on motor needs to get updated
     * @param msg
     * @param base
     */
    public void sendMsg(Byte msg, Byte base) {
        System.out.println("\n Single Motor Update");
        Byte byteMsg = 0;

        if( base == ALLOFF) {
            System.out.println("All motors off!");
            sendToSerialPort(ALLOFF);
            return;
        }

        //Left Motor
        if( base == MOTOR_BASE_LEFT) {
            byteMsg = (byte)(base + msg);
            System.out.println("Left Motor: "+byteMsg);
            sendToSerialPort(byteMsg);
            return;
        }

        //Center Motor
        if( base == MOTOR_BASE_CENTER ) {
            byteMsg = (byte)(base + msg);
            System.out.println("Center Motor: "+byteMsg);
            sendToSerialPort(byteMsg);
            return;
        }

        //Right Motor
        if( base == MOTOR_BASE_RIGHT ) {
            byteMsg = (byte)(base + msg);
            System.out.println("Right Motor: "+byteMsg);
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
            //System.out.println("Sending byte[] array: '" + msg.toString() +"'");
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
        System.out.println("Event: "+ oEvent.getEventType() +" --> "+ oEvent.toString());
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
            MOTOR_OFF   = DEFAULT_MOTOR_OFF;
            METER_1     = DEFAULT_METER_1;
            METER_1C5   = DEFAULT_METER_1C5;
            METER_2     = DEFAULT_METER_2;
            METER_2C5   = DEFAULT_METER_2C5;
            METER_3     = DEFAULT_METER_3;
            METER_3C5   = DEFAULT_METER_3C5;
            METER_4     = DEFAULT_METER_4;
            METER_4C5   = DEFAULT_METER_4C5;
            MOTOR_WARNING   = DEFAULT_MOTOR_WARNING;
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


