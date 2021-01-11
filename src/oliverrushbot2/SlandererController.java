package oliverrushbot2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomRobotController;
import controllers.CustomSlandererController;

public class SlandererController extends CustomSlandererController<MessageType> {
    public MapLocation attackCenter = null;

    public SlandererController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    // This cannot be moved into CustomSlandererController, because it depends
    // on your actual implementation of PoliticianController, which is not
    // accessible from the abstract class. Keep it for your sake.
    private void transform() {
        if (getType() == RobotType.POLITICIAN) {
            // This below can be changed if you want to have a different
            // PoliticianController for converted Slanderers
            CustomRobotController<MessageType> crc = new PoliticianController(this);
            crc.runSafe();
        }
    }

    @Override
    public void doTurn() throws GameActionException {
        transform(); // Leave this here

        if (turn < 10)
            tryMoveRandom();
        else
            trySpreadMove();
    }
}
