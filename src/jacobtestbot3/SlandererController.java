package jacobtestbot3;

import battlecode.common.*;
import communication.MarsNet.Filters.DestinationFilter;
import communication.MarsNet.MarsNet;
import controllers.CustomRobotController;
import controllers.CustomSlandererController;

public class SlandererController extends CustomSlandererController<MessageType> {
    public MapLocation attackCenter;

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

        MapLocation foundLoc = marsNet.getAndHandleF(EC.ID, DestinationFilter::Slanderer, (p) -> {
            if (p.mType == MessageType.S_Zerg) {
                return p.asLocation();
            }
            return null;
        });
        if (attackCenter == null)
            attackCenter = foundLoc;

        for (RobotInfo robot : senseNearbyRobots()) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (robot.team == getTeam())
                    continue;

                MessageType mt = MessageType.FoundEnemyEC;
                if (robot.team != getTeam().opponent())
                    mt = MessageType.FoundNeutralEC;
                marsNet.broadcastLocation(mt, robot.location);
                break;
            }
        }

        if (attackCenter != null) {
            tryMoveToward(attackCenter);
        } else
            tryMoveRandom();
    }
}
