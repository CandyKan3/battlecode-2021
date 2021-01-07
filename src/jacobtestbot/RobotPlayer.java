package jacobtestbot;
import battlecode.common.*;
import communication.MarsNet.*;

import java.util.ArrayList;
import java.util.HashSet;

class BotData {
    public final RobotInfo center;
    public HashSet<MapLocation> enemyCenters = new HashSet<>();
    public HashSet<MapLocation> neutralCenters = new HashSet<>();

    public BotData(RobotInfo center) {
        this.center = center;
    }

    public void seeEnemyCenter(MapLocation loc) {
        enemyCenters.add(loc);
        neutralCenters.remove(loc);
    }

    public void seeNeutralCenter(MapLocation loc) {
        neutralCenters.add(loc);
        enemyCenters.remove(loc);
    }

    public boolean isKnownEnemyCenter(MapLocation loc) {
        return enemyCenters.contains(loc);
    }

    public boolean isKnownNeutralCenter(MapLocation loc) {
        return neutralCenters.contains(loc);
    }
}

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final RobotType BOT = RobotType.POLITICIAN;

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
        MarsNet.setRC(rc);

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
        HashSet<MapLocation> enemyCenters = new HashSet<>();
        HashSet<MapLocation> neutralCenters = new HashSet<>();
        ArrayList<Integer> botids = new ArrayList<>();
        while (true) {
            turnCount++;
            //System.out.println("-Start Turn-");
            //System.out.println("Influence: " + rc.getInfluence());
            int influence = 20*rc.getRobotCount();
            //int influence = (((rc.getInfluence()) / 40) + 1)*20;
            if (turnCount > 600) rc.resign();
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

            Packet toSend = new Packet();
            for (int i = 0; i < botids.size(); i++) {
                int botid = botids.get(i);
                if (!rc.canGetFlag(botid)) {
                    botids.set(i, botids.get(botids.size()-1));
                    botids.remove(botids.size()-1);
                    i--;
                    continue;
                }
                MarsNet.getAndHandle(botid, ComType.BOT, (p) -> {
                    MapLocation loc;
                    switch (p.mType) {
                        case FoundEnemyEC:
                            loc = p.asLocation();
                            neutralCenters.remove(loc);
                            enemyCenters.add(loc);
                            if (!toSend.isSet())
                                toSend.setLocation(MessageType.S_Zerg, loc);
                            break;
                        case FoundNeutralEC:
                            loc = p.asLocation();
                            enemyCenters.remove(loc);
                            neutralCenters.add(loc);
                            break;
                    }
                    return null;
                });
            }
            if (toSend.isSet())
                MarsNet.broadcast(toSend);


            //System.out.println("Bytecodes used: " + Clock.getBytecodeNum());
            //System.out.println("Num Bots: " + rc.getRobotCount());
            Clock.yield();
        }
    }

    static void runPolitician(BotData indata) throws GameActionException {
        if (indata == null) {
            for (Direction dir : directions) {
                MapLocation adj = rc.adjacentLocation(dir);
                if (!rc.canSenseLocation(adj))
                    continue;
                RobotInfo robot = rc.senseRobotAtLocation(adj);
                if (robot == null)
                    continue;
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    indata = new BotData(robot);
                    break;
                }
            }
        }
        if (indata == null) {
            System.out.println("No center near on spawn?");
            return;
        }
        final BotData data = indata;

        Team me = rc.getTeam();
        Team enemy = me.opponent();
        while (true) {
            if (!tryMoveToward(data.center.location) && rc.canEmpower(3)) {
                rc.empower(3);
            }

            for (RobotInfo robot : rc.senseNearbyRobots()) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    if (robot.team == me)
                        continue;

                    MessageType mt = MessageType.FoundEnemyEC;
                    if (robot.team != enemy)
                        mt = MessageType.FoundNeutralEC;
                    MarsNet.broadcastLocation(mt, robot.location);
                    break;
                }
            }

            Clock.yield();
        }
    }

    static void runSlanderer() throws GameActionException {
        BotData indata = null;
        Team myteam = rc.getTeam();
        int id = rc.getID();
        for (Direction dir : directions) {
            MapLocation adj = rc.adjacentLocation(dir);
            if (!rc.canSenseLocation(adj))
                continue;
            RobotInfo robot = rc.senseRobotAtLocation(adj);
            if (robot == null)
                continue;
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == myteam) {
                indata = new BotData(robot);
                break;
            }
        }
        if (indata == null) {
            System.out.println("No center near on spawn?");
            return;
        }
        final BotData data = indata;

        Team me = rc.getTeam();
        Team enemy = me.opponent();
        MapLocation attackCenter = null;
        while (true) {
            if (rc.getType() == RobotType.POLITICIAN) {
                runPolitician(data);
                continue;
            }

            MapLocation foundLoc = MarsNet.getAndHandle(data.center.ID, ComType.EC, (p) -> {
                MapLocation loc;
                if (p.mType == MessageType.S_Zerg) {
                    loc = p.asLocation();
                    return loc;
                }
                return null;
            });
            if (attackCenter == null)
                attackCenter = foundLoc;

            for (RobotInfo robot : rc.senseNearbyRobots()) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    if (robot.team == me)
                        continue;

                    MessageType mt = MessageType.FoundEnemyEC;
                    if (robot.team != enemy)
                        mt = MessageType.FoundNeutralEC;
                    MarsNet.broadcastLocation(mt, robot.location);
                    break;
                }
            }

            if (attackCenter != null) {
                tryMoveToward(attackCenter);
            } else
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
        Direction p, s;
        if (slope > 0.414214 && slope < 2.414214) {
            p = diagonal;
            if (slope <= 1)
                s = sidetoside;
            else
                s = upanddown;
        }
        else if (slope <= 0.414214) {
            p = sidetoside;
            s = diagonal;
        }
        else {
            p = upanddown;
            s = diagonal;
        }
        boolean pRotateLeft = true;
        if (p.rotateLeft().equals(s))
            pRotateLeft = false;
        for (int i = 0; i < 5; i++) {
            if (rc.canMove(p)) {
                rc.move(p);
                return true;
            }
            Direction temp;
            if (pRotateLeft)
                temp = p.rotateLeft();
            else
                temp = p.rotateRight();
            p = s;
            s = temp;
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