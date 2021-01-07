package oliverecsearch;

import battlecode.common.*;
import communication.MarsNet.ComType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.MessageType;
import controllers.CustomMuckrakerController;

public class MuckrakerController extends CustomMuckrakerController {

    public MapLocation pushGoal = null;

    @Override
    public void doTurn() throws GameActionException {

        // Find pushGoal (where we want to move to)
        MapLocation foundLoc = MarsNet.getAndHandle(EC.ID, ComType.EC, (p) -> {
            if (p.mType == MessageType.M_Search) {
                return p.asLocation();
            }
            else if (p.mType == MessageType.M_Zerg) {
                return p.asLocation();
            }
            return null;
        });

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
            // Search for enlightenment centers
            else if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (robot.team == getTeam())
                    continue;

                MessageType mt = MessageType.FoundEnemyEC;
                if (robot.team != getTeam().opponent())
                    mt = MessageType.FoundNeutralEC;
                MarsNet.broadcastLocation(mt, robot.location);
                break;
            }
        }

        if (pushGoal != null) {
            if (tryMoveToward(pushGoal)) {
                // TODO: Figure out if you are in a corner then get coords
                System.out.println("x " + getLocation().x + " y " + getLocation().y);
            };
        } else {
            tryMoveRandom();
        }
    }
}
