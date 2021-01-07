package oliverecsearch;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import communication.MarsNet.MarsNet;
import controllers.CustomRobotController;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        CustomRobotController.setRC(rc);
        MarsNet.setRC(rc);

        CustomRobotController crc = null;
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                crc = new ECController();
                break;
            case POLITICIAN:
                crc = new PoliticianController();
                break;
            case SLANDERER:
                crc = new SlandererController();
                break;
            case MUCKRAKER:
                crc = new MuckrakerController();
                break;
        }
        if (crc == null) {
            System.out.println("I don't know how we got here");
            return;
        }
        crc.runSafe();
    }
}
