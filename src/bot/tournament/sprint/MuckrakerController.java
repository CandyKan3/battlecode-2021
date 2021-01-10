package bot.tournament.sprint;

import battlecode.common.*;
import communication.MarsNet.Filters.DestinationFilter;
import communication.MarsNet.MarsNet;
import controllers.CustomMuckrakerController;

public class MuckrakerController extends CustomMuckrakerController<MessageType> {
    public MuckrakerController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }
    public MapLocation initialGoal = null;

    @Override
    public void doTurn() throws GameActionException {

        // Find initialGoal (where we want to move to)
        MapLocation foundLoc = marsNet.getAndHandleF(EC.ID, DestinationFilter::Muckraker, (p) -> {
            if (p.mType == MessageType.M_Search)
                return p.asLocation();
            return null;
        });

        if (foundLoc != null && initialGoal == null) initialGoal = foundLoc;

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
                marsNet.broadcastLocation(mt, robot.location);
                break;
            }
        }

        if (initialGoal != null) {
            tryMoveToward(initialGoal);
        } else {
            tryMoveRandom();
        }
    }
}
