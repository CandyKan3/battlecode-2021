package brycetestbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import communication.MarsNet.MarsNet;
import controllers.CustomRobotController;
import templatebot.ECController;
import templatebot.MessageType;
import templatebot.MuckrakerController;
import templatebot.PoliticianController;
import templatebot.SlandererController;

public strictfp class RobotPlayer {
    static final MarsNet<MessageType> marsNet;

    static {
        byte[] headerProtocol = null;

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
