package bot.tournament.sprint;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomPoliticianController;


public class PoliticianController extends CustomPoliticianController<MessageType> {
    public MapLocation attackCenter = null;

    // This cannot be moved into CustomPoliticianController, because it depends
    // on your actual implementation of PoliticianController, which is not
    // accessible from the abstract class. Keep it, but add to as necessary.
    public PoliticianController(SlandererController sc) {
        super(sc);
    }
    public PoliticianController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    @Override
    public void doTurn() throws GameActionException {

        for (RobotInfo robot : senseNearbyRobots()) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (robot.team == getTeam()) {
                    if (getLocation().isAdjacentTo(robot.location) && canEmpower(3))
                        empower(3);
                    continue;
                }

                MessageType mt = MessageType.FoundEnemyEC;
                if (robot.team != getTeam().opponent())
                    mt = MessageType.FoundNeutralEC;
                marsNet.broadcastLocation(mt, robot.location);
                break;
            }
        }

        MapLocation foundLoc = marsNet.getAndHandle(EC.ID, (p) -> {
            if (p.mType == MessageType.S_Zerg || p.mType== MessageType.Pre_Zerg) {
                return p.asLocation();
            }
            return null;
        });
        if (attackCenter == null)
        {
            attackCenter = foundLoc;
        }



        tryMoveToward(EC.location);
    }
}
