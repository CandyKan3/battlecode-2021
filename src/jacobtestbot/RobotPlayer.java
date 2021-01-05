package jacobtestbot;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.HashMap;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        //System.out.println("I'm a " + rc.getType() + " and I just got created!");
        if (rc.getType() == RobotType.POLITICIAN) {
            System.out.println("WHAT");
        }
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician(null);          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        System.out.println("NEW CENTER???!?!?!?");
        HashMap<Integer, MapLocation> enemycenters = new HashMap<>();
        HashMap<Integer, MapLocation> neutralcenters = new HashMap<>();
        ArrayList<Integer> botids = new ArrayList<>();
        while (true) {
            turnCount++;
            System.out.println("-Start Turn-");
            System.out.println("Influence: " + rc.getInfluence());
            int influence = 20*rc.getRobotCount();
            //int influence = (((rc.getInfluence()) / 40) + 1)*20;
            if (rc.getInfluence() > influence) {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.SLANDERER, dir, influence)) {
                        rc.buildRobot(RobotType.SLANDERER, dir, influence);
                        MapLocation buildloc = rc.adjacentLocation(dir);
                        RobotInfo robot = rc.senseRobotAtLocation(buildloc);
                        botids.add(robot.ID);
                        break;
                    }
                }
            }

            System.out.println("Bytecodes used: " + Clock.getBytecodeNum());
            System.out.println("Num Bots: " + rc.getRobotCount());
            Clock.yield();
        }
    }

    static void runPolitician(RobotInfo center) throws GameActionException {
        System.out.println(center);
        if (center == null) {
            for (Direction dir : directions) {
                MapLocation adj = rc.adjacentLocation(dir);
                if (!rc.canSenseLocation(adj))
                    continue;
                RobotInfo robot = rc.senseRobotAtLocation(adj);
                if (robot == null)
                    continue;
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    center = robot;
                    break;
                }
            }
        }
        if (center == null) {
            System.out.println("No center near on spawn?");
            return;
        }
        while (center == null) {
            if (rc.canEmpower(3))
                rc.empower(3);
            Clock.yield();
        }
        while (true) {
            if (!tryMoveToward(center.location) && rc.canEmpower(3)) {
                rc.empower(3);
            }

            Clock.yield();
        }
    }

    static void runSlanderer() throws GameActionException {
        RobotInfo center = null;
        Team myteam = rc.getTeam();
        int id = rc.getID();
        for (Direction dir : directions) {
            MapLocation adj = rc.adjacentLocation(dir);
            RobotInfo robot = rc.senseRobotAtLocation(adj);
            if (robot == null)
                continue;
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == myteam) {
                center = robot;
                break;
            }
        }
        if (center == null) {
            System.out.println("No center near on spawn?");
        }
        while (true) {
            if (rc.getType() == RobotType.POLITICIAN) {
                System.out.println(id == rc.getID());
                runPolitician(center);
                continue;
            }
            tryMove(randomDirection());

            Clock.yield();
        }
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static boolean tryMoveToward(MapLocation to) throws GameActionException {
        MapLocation from = rc.getLocation();
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        Direction sidetoside = Direction.EAST;
        if (dx < 0) {
            sidetoside = Direction.WEST;
        }
        Direction upanddown = Direction.NORTH;
        if (dy < 0) {
            upanddown = Direction.SOUTH;
        }
        boolean isNorth = upanddown == Direction.NORTH;
        Direction diagonal;
        if (sidetoside == Direction.EAST) {
            if (isNorth) {
                diagonal = Direction.NORTHEAST;
            } else {
                diagonal = Direction.SOUTHEAST;
            }
        } else {
            if (isNorth) {
                diagonal = Direction.NORTHWEST;
            } else {
                diagonal = Direction.SOUTHWEST;
            }
        }
        double slope;
        if (dx == 0) {
            slope = Double.POSITIVE_INFINITY;
        } else {
            slope = ((double)dy)/((double)dx);
        }
        if (slope < 0)
            slope *= -1;
        if (slope > 0.57735 && slope < 1.73205) {
            if (rc.canMove(diagonal)) {
                rc.move(diagonal);
                return true;
            }
            if (slope <= 1) {
                if (rc.canMove(sidetoside)) {
                    rc.move(sidetoside);
                    return true;
                }
                if (rc.canMove(upanddown)) {
                    rc.move(upanddown);
                    return true;
                }
            } else {
                if (rc.canMove(upanddown)) {
                    rc.move(upanddown);
                    return true;
                }
                if (rc.canMove(sidetoside)) {
                    rc.move(sidetoside);
                    return true;
                }
            }
        }
        else if (slope <= 0.57735) {
            if (rc.canMove(sidetoside)) {
                rc.move(sidetoside);
                return true;
            }
            if (rc.canMove(diagonal)) {
                rc.move(diagonal);
                return true;
            }
            if (rc.canMove(upanddown)) {
                rc.move(upanddown);
                return true;
            }
        }
        else {
            if (rc.canMove(upanddown)) {
                rc.move(upanddown);
                return true;
            }
            if (rc.canMove(diagonal)) {
                rc.move(diagonal);
                return true;
            }
            if (rc.canMove(sidetoside)) {
                rc.move(sidetoside);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        //System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}