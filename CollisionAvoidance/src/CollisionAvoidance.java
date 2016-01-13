import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.J4KSDK;

public class CollisionAvoidance extends J4KSDK {
    private final static String TAG = "CollisionAvoidance";
    private final static byte MICROSOFT_KINECT_2 = 0x2;
    private float minDepth = (float)0.5;

    private Integer LEFT_COL_WIDTH    = 170;
    private Integer CENTER_COl_WIDTH  = 172;
    private Integer RIGHT_COL_WIDTH   = 170;

    private ViewerPanel3D viewer =null;
    private boolean useinfrared = false;

    private Integer infraWidth  = getInfraredWidth();
    private Integer infraHeight = getInfraredHeight();

    private Float   maxDepth;
    private Integer leftBorder;
    private Integer rightBorder;

    private boolean hideBottomRow = true;

    private FeedbackSystem fbs = null;

    private long frameCounter = 0;

    public CollisionAvoidance(ViewerPanel3D viewport, float maxDepth, int leftBorder, int rightBorder) {
        super(MICROSOFT_KINECT_2);

        System.out.println("Created CollisionAvoidance UI");
        this.viewer      = viewport;
        this.maxDepth    = maxDepth;
        this.leftBorder  = leftBorder;
        this.rightBorder = rightBorder;

        this.fbs = FeedbackSystem.getInstance();
    }


    @Override
    public void onDepthFrameEvent(short[] depth_frame, byte[] body_index, float[] xyz, float[] uv) {
        //System.out.println("Frame Number:"+frameCounter);
        DepthMap map = new DepthMap( this.getDepthWidth(), this.getDepthHeight(), xyz);
        map.setMaximumAllowedDeltaZ(minDepth);
        map.maskZ(maxDepth);
        if(uv != null) {
            map.setUV(uv);
        } else if (useinfrared){
            map.setUVuniform();
        }

        if(frameCounter == 14){
            filterDepthFrame(map);
            frameCounter = 0;
        }

        if(leftBorder != 0 || rightBorder != 0) {
            int recWidth = this.getDepthWidth()-leftBorder-rightBorder;
            map.maskRect(leftBorder, 0,recWidth, this.getDepthHeight());
        }

        //map.setUVuniform();
        frameCounter++;
        viewer.map = map;
    }


    @Override
    public void onInfraredFrameEvent(short[] data) {

    }

    @Override
    public void onColorFrameEvent(byte[] bytes) {
//        viewport.videoTexture.update(getColorWidth(), getColorHeight(), data);
        return;
    }

    @Override
    public void onSkeletonFrameEvent(boolean[] booleen, float[] floats, float[] floats1, byte[] bytes) {

    }


    private DepthMap filterDepthFrame(DepthMap dmap){
        float ZERO = (float)0.0;
        float[] closestZ    = {maxDepth, maxDepth, maxDepth};
        int[] pointIndex  = new int[3];

        for(int w = 0; w < dmap.getWidth() ;w++) {
            for (int h = 0; h < dmap.getHeight(); h++) {

                if(h <= 350 && hideBottomRow) {
                    if (w <= LEFT_COL_WIDTH) {
                        if (dmap.validDepthAt(w, h)) {
                            float realZ = dmap.realZ[h * w];

                            if (isZCloser(realZ, closestZ[0])) {
                                closestZ[0] = realZ;
                                pointIndex[0] = w * h;
                            }
                        } else {
                           // dmap.realZ[w * h] = ZERO;
                        }
                    }

                    if (w > LEFT_COL_WIDTH && w <= (LEFT_COL_WIDTH + CENTER_COl_WIDTH)) {
                        if (dmap.validDepthAt(w, h)) {
                            float realZ = dmap.realZ[h * w];

                            if (isZCloser(realZ, closestZ[1])) {
                                closestZ[1] = realZ;
                                pointIndex[1] = w * h;
                            }
                        } else {
                           // dmap.realZ[w * h] = ZERO;
                        }
                    }

                    if (w > (LEFT_COL_WIDTH + CENTER_COl_WIDTH) && w <= (LEFT_COL_WIDTH + CENTER_COl_WIDTH + RIGHT_COL_WIDTH)) {
                        if (dmap.validDepthAt(w, h)) {
                            float realZ = dmap.realZ[h * w];

                            if (isZCloser(realZ, closestZ[2])) {
                                closestZ[2] = realZ;
                                pointIndex[2] = w * h;
                            }
                        } else {
                            //dmap.realZ[w * h] = ZERO;
                        }
                    }
                }
            }
        }

        sendFeedbackMsg(closestZ[0], FeedbackSystem.MOTOR_BASE_LEFT);
        sendFeedbackMsg(closestZ[1], FeedbackSystem.MOTOR_BASE_CENTER);
        sendFeedbackMsg(closestZ[2], FeedbackSystem.MOTOR_BASE_RIGHT);

        return dmap;
    }


    private boolean isZCloser(float z, float closestZ){
        if(z >= minDepth && z <= maxDepth) {
            if(z < closestZ){
                return true;
            }
        }

        return false;
    }


    private void sendFeedbackMsg(float z, byte motorBase) {
        //System.out.println("MotorBase: "+motorBase+" --> "+z);
        if(fbs.isInitialized()){
            // Warning
            if(z > minDepth && z < 0.8) {
                fbs.sendMsg(FeedbackSystem.MOTOR_WARNING, motorBase);
            }

            //1 Meter
            if(z >= 0.8 && z < 1.2) {
                fbs.sendMsg(FeedbackSystem.METER_1, motorBase);
            }

            //1,5 Meter
            if(z >= 1.2 && z < 1.6 ) {
                fbs.sendMsg(FeedbackSystem.METER_1C5, motorBase);
            }

            //2 Meter
            if(z >= 1.6 && z < maxDepth ){
                fbs.sendMsg(FeedbackSystem.METER_2, motorBase);
            }

            //Motor Off
            if(z == 0 || z >= maxDepth){
                fbs.sendMsg(FeedbackSystem.MOTOR_OFF, motorBase);
            }
        }
    }


    /**
     * Update the settings of the depthFrame
     * @param maxDepth
     * @param leftBorder
     * @param rightBorder
     * @param showVideo
     */
    public void update(float maxDepth, int leftBorder, int rightBorder, boolean showVideo) {
        this.maxDepth = maxDepth;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
        this.viewer.setShowVideo(showVideo);
    }


    /**
     * Stop's the connection to the Kinect
     */
    public void close(){
        if(fbs.isInitialized()){
            fbs.sendMsg(FeedbackSystem.ALLOFF, FeedbackSystem.ALLOFF);
        }
        fbs.close();
        this.stop();
        this.viewer = null;
    }
}
