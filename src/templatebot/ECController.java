package templatebot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;

public class ECController extends CustomECController<MessageType> {

    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    @Override
    public void doTurn() throws GameActionException {

    }
}
