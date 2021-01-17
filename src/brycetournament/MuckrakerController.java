package brycetournament;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomMuckrakerController;
import util.HashSet11;

public class MuckrakerController extends CustomMuckrakerController<MessageType> {

    private MapLocation homeEC;
    private MapLocation scoutLocation;
    private Direction scoutDirection;
    private boolean scouting;
    private final Team enemy;
    private final int actionRadius;
    private HashSet11 foundlocs = new HashSet11();
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
        if(((this.getRoundNum() ^ getID()) & 0x3 )== 0){
            //clear cached message for new broadcast

            for (RobotInfo robot : senseNearbyRobots(actionRadius, enemy)) {
                if (!exposed && robot.type.canBeExposed() && canExpose(robot.location)) {
                    expose(robot.location);
                    exposed = true;
                }

                // Report found ECs
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team != getTeam()) {
                    if (robot.team == enemy&&foundlocs.contains(robot.location)==false){
                        foundlocs.add(robot.location);
                        marsNet.broadcastLocation(MessageType.FoundEnemyEC, robot.location);
                    }
                    else if(foundlocs.contains(robot.location)==false){
                        foundlocs.add(robot.location);
                        marsNet.broadcastLocation(MessageType.FoundNeutralEC, robot.location);
                    }
                    else{
                        //just vibing
                    }
                }
            }
        }
        else{
            for (RobotInfo robot : senseNearbyRobots(actionRadius, enemy)) {
                if (!exposed && robot.type.canBeExposed() && canExpose(robot.location)) {
                    expose(robot.location);
                    exposed = true;
                }
            }
        }



        if (scouting && scoutLocation != null)
            tryMoveToward(scoutLocation);
        else
            tryMoveRandom();
    }
}
