package jacobtestbot3;

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
        Team enemy = getTeam().opponent();
        int actionRadius = getType().actionRadiusSquared;
        for (RobotInfo robot : senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (canExpose(robot.location)) {
                    expose(robot.location);
                    return;
                }
            }
        }
        tryMoveRandom();
    }
}
