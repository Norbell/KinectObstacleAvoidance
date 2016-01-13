import edu.ufl.digitalworlds.gui.DWApp;
import edu.ufl.digitalworlds.j4k.J4K2;
import edu.ufl.digitalworlds.j4k.J4KSDK;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

@SuppressWarnings("serial")
public class KinectViewPort extends DWApp {
    private final static String TAG = "KinectViewPort";

    private ViewerPanel3D viewPort;
    private CollisionAvoidance collisionAvoidance;

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    @Override
    public void GUIsetup(JPanel jPanel) {
        setLoadingProgress("Intitializing OpenGL...",60);
        viewPort =new ViewerPanel3D();
        viewPort.setShowVideo(false);
        myKinect.setViewer(main_panel);
        myKinect.setLabel(accelerometer);

    }

    public void GUIclosing() {
        collisionAvoidance.stop();
    }
}
