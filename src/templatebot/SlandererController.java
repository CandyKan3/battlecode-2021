package templatebot;

import battlecode.common.*;
import communication.MarsNet.ComType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.MessageType;
import controllers.CustomRobotController;
import controllers.CustomSlandererController;

public class SlandererController extends CustomSlandererController {

    // This cannot be moved into CustomSlandererController, because it depends
    // on your actual implementation of PoliticianController, which is not
    // accessible from the abstract class. Keep it for your sake.
    private void transform() {
        if (getType() == RobotType.POLITICIAN) {
            // This below can be changed if you want to have a different
            // PoliticianController for converted Slanderers
            CustomRobotController crc = new PoliticianController(this);
            crc.runSafe();
        }
    }

    @Override
    public void doTurn() throws GameActionException {
        transform(); // Leave this here
    }
}
