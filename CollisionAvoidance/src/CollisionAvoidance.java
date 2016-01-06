public class CollisionAvoidance {
    private final static String TAG = "CollisionAvoidance";

    private final static Integer yes = 121;
    private final static Integer no = 22;

    public static void main(String[] args) throws Exception {
        FeedbackSystem FbS = new FeedbackSystem();
        if ( FbS.initialize() ) {
            int i = 0;
            while (i < 5 ) {
                try {
                    Thread.sleep(2000);
                    FbS.sendInt(yes);
                } catch (InterruptedException ie) {
                    System.out.println(TAG + " : " + ie.toString());
                }
                try {
                    Thread.sleep(2000);
                    FbS.sendInt(no);
                } catch (InterruptedException ie) {
                    System.out.println(TAG + " : " + ie.toString());
                }
                i++;
            }

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