public class CollisionAvoidance {
    private final static String TAG = "CollisionAvoidance";

    private final static byte[] test = {
            FeedbackSystem.MOTOR_WARNING,
            FeedbackSystem.METER_1,
            FeedbackSystem.MOTOR_OFF
    };
    private final static byte[] test2 = {
            FeedbackSystem.METER_3,
            FeedbackSystem.METER_3,
            FeedbackSystem.METER_1
    };

    public static void main(String[] args) throws Exception {
        FeedbackSystem FbS = new FeedbackSystem();
        if ( FbS.initialize() ) {
            FbS.sendByteArray(test);
            try { Thread.sleep(8000); } catch (InterruptedException ie) {}
            FbS.sendByteArray(test2);
            try { Thread.sleep(2000); } catch (InterruptedException ie) {}
            FbS.close();
        }

        // Wait 5 seconds then shutdown
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ie) {
            System.out.println(TAG +" : "+ ie.toString());
        }
    }
}