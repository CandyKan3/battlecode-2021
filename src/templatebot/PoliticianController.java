package templatebot;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomPoliticianController;

public class PoliticianController extends CustomPoliticianController<MessageType> {

    public PoliticianController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    // This cannot be moved into CustomPoliticianController, because it depends
    // on your actual implementation of PoliticianController, which is not
    // accessible from the abstract class. Keep it, but add to as necessary.
    public PoliticianController(SlandererController sc) {
        super(sc);
    }

    @Override
    public void doTurn() throws GameActionException {

    }
}
