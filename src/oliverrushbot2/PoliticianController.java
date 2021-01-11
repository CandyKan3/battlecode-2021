package oliverrushbot2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomPoliticianController;

public class PoliticianController extends CustomPoliticianController<MessageType> {
    private MapLocation attackLocation;

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
        for (RobotInfo robot : senseNearbyRobots(RobotType.POLITICIAN.sensorRadiusSquared, getTeam().opponent())) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                marsNet.broadcastLocation(MessageType.FoundEnemyEC, robot.location);
                break;
            }
        }

        attackLocation = marsNet.getAndHandleSafe(EC.ID, (p) -> {
            switch (p.mType) {
                case A_Zerg:
                case P_Zerg:
                    return p.asLocation();
                case A_StopZerg:
                case P_StopZerg:
                    return null;
            }
            return attackLocation;
        });

        if (attackLocation != null) {
            MapLocation me = getLocation();
            if (me.isWithinDistanceSquared(attackLocation, 8) && canEmpower(9)) {
                empower(9);
                return;
            }
            tryMoveToward(attackLocation);
        } else {
            trySpreadMove();
        }
    }
}
