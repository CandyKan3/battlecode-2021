package oliverecsearch;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import communication.MarsNet.ComType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.MessageType;
import controllers.CustomRobotController;
import controllers.CustomSlandererController;

public class SlandererController extends CustomSlandererController {
    public MapLocation attackCenter = null;

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

        MapLocation foundLoc = MarsNet.getAndHandle(EC.ID, ComType.EC, (p) -> {
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
                MarsNet.broadcastLocation(mt, robot.location);
                break;
            }
        }

        if (attackCenter != null) {
            tryMoveToward(attackCenter);
        } else
            tryMoveRandom();
    }
}
