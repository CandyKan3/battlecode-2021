package jacobtestbot3;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import communication.MarsNet.MarsNet;
import controllers.CustomRobotController;

public strictfp class RobotPlayer {
    static final MarsNet<MessageType> marsNet;

    static {
        byte[] headerProtocol = {19,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,1,0,0,-1,0,0,0,0,2,0,0,-1,0,0,0,0,3,0,0,-1,0,0,0,0,4,0,0,-1,0,0,0,0,0,1,0,-1,-1,0,0,0,0,2,0,-1,-1,0,0,0,0,3,0,-1,-1,0,0,0,0,4,0,-1,-1,0,0,0,0,5,0,-1,-1,0,0,0,0,6,0,-1,-1,0,0,1,7,0,0,0,16,-1,0,0,0,1,5,0,0,0,8,-1,0,0,0,1,9,0,0,0,0,-1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,2,0,0,0,0,3,0,0,0,0,4,0,0,0,0,5,0,0,0,0,6,0,0,0,0,7,0,0,0,0,8,0,0,0,0,9,0,0,0,0,10,0,0,0,0,11,0,0,0,0,12,0,0,0,0,13,0,0,0,0,14,0,0,0,0,15,0,0,0,0,16,0,0,0,0,17,0,0,0,0,18,0,0,0};

        if (headerProtocol == null) {
            MarsNet<MessageType> mNet = new MarsNet<>(MessageType.values());
            byte[] data = mNet.serializedHeaderProtocol();
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            if (data.length > 0)
                sb.append(data[0]);
            for (int i = 1; i < data.length; i++) {
                sb.append(',');
                sb.append(data[i]);
            }
            sb.append("};\n");
            System.out.println("Serialized Header Protocol: " + sb);
            marsNet = null;
        } else {
            marsNet = new MarsNet<>(headerProtocol, MessageType.values());
        }
    }

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        if (marsNet == null)
            rc.resign();
        CustomRobotController.setRC(rc);
        MarsNet.setRC(rc);

        CustomRobotController<MessageType> crc = null;
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                crc = new ECController(marsNet);
                break;
            case POLITICIAN:
                crc = new PoliticianController(marsNet);
                break;
            case SLANDERER:
                crc = new SlandererController(marsNet);
                break;
            case MUCKRAKER:
                crc = new MuckrakerController(marsNet);
                break;
        }
        if (crc == null) {
            System.out.println("I don't know how we got here");
            return;
        }
        crc.runSafe();
    }
}
