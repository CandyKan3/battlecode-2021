package olivertournament2;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomMuckrakerController;

public class MuckrakerController extends CustomMuckrakerController<MessageType> {

    private MapLocation homeEC;
    private MapLocation scoutLocation;
    private Direction scoutDirection;
    private boolean scouting;
    private final Team enemy;
    private final int actionRadius;

    public MuckrakerController(MarsNet<MessageType> marsNet) {
        super(marsNet);
        enemy = getTeam().opponent();
        actionRadius = getType().actionRadiusSquared;

        scouting = marsNet.getAndHandleSafe(EC.ID, (p) -> {
            switch (p.mType) {
                case M_ScoutNorth:
                    scoutLocation = EC.location.translate(0, 64);
                    scoutDirection = Direction.NORTH;
                    return true;
                case M_ScoutNorthEast:
                    scoutLocation = EC.location.translate(64, 64);
                    scoutDirection = Direction.NORTHEAST;
                    return true;
                case M_ScoutNorthWest:
                    scoutLocation = EC.location.translate(-64, 64);
                    scoutDirection = Direction.NORTHWEST;
                    return true;
                case M_ScoutEast:
                    scoutLocation = EC.location.translate(64, 0);
                    scoutDirection = Direction.EAST;
                    return true;
                case M_ScoutSouth:
                    scoutLocation = EC.location.translate(0, -64);
                    scoutDirection = Direction.SOUTH;
                    return true;
                case M_ScoutSouthEast:
                    scoutLocation = EC.location.translate(64, -64);
                    scoutDirection = Direction.SOUTH;
                    return true;
                case M_ScoutSouthWest:
                    scoutLocation = EC.location.translate(-64, -64);
                    scoutDirection = Direction.SOUTHWEST;
                    return true;
                case M_ScoutWest:
                    scoutLocation = EC.location.translate(-64, 0);
                    scoutDirection = Direction.WEST;
                    return true;
            }
            return false;
        });
    }


    @Override
    public void doTurn() throws GameActionException {

        boolean exposed = false;
        for (RobotInfo robot : senseNearbyRobots(actionRadius, enemy)) {
            if (!exposed && robot.type.canBeExposed() && canExpose(robot.location)) {
                expose(robot.location);
                exposed = true;
            }

            // Report found ECs
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team != getTeam()) {
                if (robot.team == enemy)
                    marsNet.broadcastLocation(MessageType.FoundEnemyEC, robot.location);
                else
                    marsNet.broadcastLocation(MessageType.FoundNeutralEC, robot.location);
            }
        }


        if (scouting && scoutLocation != null)
            tryMoveToward(scoutLocation);
        else
            tryMoveRandom();
    }
}
