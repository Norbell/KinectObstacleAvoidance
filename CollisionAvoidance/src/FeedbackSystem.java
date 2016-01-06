import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


public class FeedbackSystem implements SerialPortEventListener{
    private final static String TAG = "FeedbackSystem";
    private final static String appName = "CollisionAvoidance";

    private static final String PORT_NAMES[] = new PortNames().getPortNames();

    private SerialPort serialPort = null;
    private BufferedReader input;
    private OutputStream output;

    private static final int TIME_OUT = 1000; // Port open timeout
    private static final int DATA_RATE = 9600; // Arduino serial port

    // Vibration intensity steps
    public final static Integer WARNING    = 255;
    public final static Integer METER_1    = 225;
    public final static Integer METER_1_5  = 200;
    public final static Integer METER_2    = 175;
    public final static Integer METER_2_5  = 150;
    public final static Integer METER_3    = 100;


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
            System.out.println("Sending string data: '" + data +"'");
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
}


