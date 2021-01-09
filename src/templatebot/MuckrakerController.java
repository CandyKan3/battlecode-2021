package templatebot;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import communication.MarsNet.MarsNet;
import controllers.CustomMuckrakerController;

public class MuckrakerController extends CustomMuckrakerController<MessageType> {

    public MuckrakerController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    @Override
    public void doTurn() throws GameActionException {

    }
}
