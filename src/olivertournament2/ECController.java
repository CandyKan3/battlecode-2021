package olivertournament2;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;

public class ECController extends CustomECController<MessageType> {

    private int phase = 0;
    private int spawnCycle = 0;
    private MessageType[] scoutDirectives = {MessageType.M_ScoutEast, MessageType.M_ScoutWest, MessageType.M_ScoutNorth, MessageType.M_ScoutNorthEast, MessageType.M_ScoutNorthWest, MessageType.M_ScoutSouth, MessageType.M_ScoutSouthEast, MessageType.M_ScoutSouthWest};
    private int currScoutType = 0;
    private boolean canBroadcast;

    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    private boolean buildMuckrakerScoutSafe(Direction dir, int influence) {
        if (canBroadcast && buildRobotSafe(RobotType.MUCKRAKER, dir, influence)) {
            marsNet.broadcastRaw(scoutDirectives[currScoutType], 0);
            currScoutType = (currScoutType + 1) % scoutDirectives.length;
            canBroadcast = false;
            return true;
        }

        return false;
    }

    private boolean buildSlandererCornerSafe(Direction dir, int influence) {
        // Figure out safest area
        Direction enemyECDir = getLocation().directionTo(enemyEC.peek());

        if (enemyECDir != null && enemyECDir != Direction.CENTER) {
            MapLocation targetLoc;
            switch (enemyECDir.opposite()) {
                case NORTH:
                    targetLoc = getLocation().translate(0, 64);
                    break;
                case NORTHEAST:
                    targetLoc = getLocation().translate(64, 64);
                    break;
                case NORTHWEST:
                    targetLoc = getLocation().translate(-64, 64);
                    break;
                case EAST:
                    targetLoc = getLocation().translate(64, 0);
                    break;
                case SOUTH:
                    targetLoc = getLocation().translate(0, -64);
                    break;
                case SOUTHEAST:
                    targetLoc = getLocation().translate(64, -64);
                    break;
                case SOUTHWEST:
                    targetLoc = getLocation().translate(-64, 0);
                    break;
                default:
                    targetLoc = getLocation().translate(64, 64);
            }

           if (canBroadcast && buildRobotSafe(RobotType.SLANDERER, dir, influence)) {
               marsNet.broadcastLocation(MessageType.S_Turtle, targetLoc);
               canBroadcast = false;
               return true;
           }
        }

        return false;
    }

    @Override
    public void doTurn() throws GameActionException {

        canBroadcast = true;

        // Early turns are hardcoded
        // Create one strong Slanderer
        if (phase == 0) {
            for (Direction dir : Direction.allDirections()) {
                int influence = 150;
                if (buildRobotSafe(RobotType.SLANDERER, dir, influence)) {
                    phase++;
                    break;
                }
            }
            // Create one weak Muckraker
        } else if (phase == 1) {
            for (Direction dir : Direction.allDirections()) {
                int influence = 1;
                if (buildRobotSafe(RobotType.MUCKRAKER, dir, influence)) {
                    phase++;
                    break;
                }
            }
            // Create mix of all three unit types
        } else if (phase == 2) {

            // Decide robot type and influence
            RobotType buildType = RobotType.MUCKRAKER;
            int influence = 1;
            switch (spawnCycle) {
                case 0:
                case 1:
                    break;
                case 2:
                case 3:
                    buildType = RobotType.SLANDERER;
                    influence = 41;
                    break;
                case 4:
                    buildType = RobotType.POLITICIAN;
                    influence = 41;
            }

            // Build robot
            if (getInfluence() >= influence) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildType == RobotType.MUCKRAKER && buildMuckrakerScoutSafe(dir, influence)) {
                        spawnCycle = spawnCycle + 1 > 4 ? 0 : spawnCycle + 1;
                        break;
                    } else if (buildRobotSafe(buildType, dir, influence)) {
                        spawnCycle = spawnCycle + 1 > 4 ? 0 : spawnCycle + 1;
                        break;
                    }
                }
            }
        }


        // Check signals
        handleBots((p) -> {
            MapLocation loc;
            switch (p.mType) {
                case FoundEnemyEC:
                    loc = p.asLocation();
                    if (enemyEC.contains(loc))
                        break;
                    neutralEC.remove(loc);
                    enemyEC.add(loc);
                    break;
                case FoundNeutralEC:
                    loc = p.asLocation();
                    if (neutralEC.contains(loc))
                        break;
                    enemyEC.remove(loc);
                    neutralEC.add(loc);
                    break;
            }
            return null;
        });


        // Decide where to send units
        if (canBroadcast) {
            MapLocation attackLocation = enemyEC.peek();
            if (attackLocation != null) {
                marsNet.broadcastLocation(MessageType.A_Zerg, attackLocation);
            } else {
                attackLocation = neutralEC.peek();
                if (attackLocation != null) {
                    marsNet.broadcastLocation(MessageType.P_Zerg, attackLocation);
                }
            }
        }
        System.out.println(Clock.getBytecodeNum() +"-Oliver");
    }
}
