package controllers;

import battlecode.common.*;

// Contains useful functions and data for all bots
public strictfp abstract class CustomRobotController implements IBotController {
    private static RobotController rc;

    public int turn = 0;

    public static void setRC(RobotController rc) {
        CustomRobotController.rc = rc;
    }

    public CustomRobotController() { }

    public CustomRobotController(CustomRobotController crc) {}

    public void run() throws GameActionException {
        while (true) {
            doTurn();
            turn++;
            Clock.yield();
        }
    }

    public void runSafe() {
        while (true) {
            try {
                run();
            } catch (GameActionException ignored) {}
        }
    }

    // battlecode.common.RobotController wrapper functions. Do not touch unless
    // you want to override one of them, in which case you may need to remove
    // the static modifier, which shouldn't cause any problems. (Yes, I
    // manually typed all of these)
    public static int getRoundNum() { return rc.getRoundNum(); }
    public static int getTeamVotes() { return rc.getTeamVotes(); }
    public static int getRobotCount() { return rc.getRobotCount(); }
    public static double getEmpowerFactor(Team team, int roundsInFuture) { return rc.getEmpowerFactor(team, roundsInFuture); }
    public static int getID() { return rc.getID(); }
    public static Team getTeam() { return rc.getTeam(); }
    public static RobotType getType() { return rc.getType(); }
    public static MapLocation getLocation() { return rc.getLocation(); }
    public static int getInfluence() { return rc.getInfluence(); }
    public static int getConviction() { return rc.getConviction(); }
    public static boolean onTheMap(MapLocation loc) throws GameActionException { return rc.onTheMap(loc); }
    public static boolean canSenseLocation(MapLocation loc) { return rc.canSenseLocation(loc); }
    public static boolean canSenseRadiusSquared(int radiusSquared) { return rc.canSenseRadiusSquared(radiusSquared); }
    public static boolean canDetectLocation(MapLocation loc) { return rc.canDetectLocation(loc); }
    public static boolean canDetectRadiusSquared(int radiusSquared) { return rc.canDetectRadiusSquared(radiusSquared); }
    public static boolean isLocationOccupied(MapLocation loc) throws GameActionException { return rc.isLocationOccupied(loc); }
    public static RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException { return rc.senseRobotAtLocation(loc); }
    public static boolean canSenseRobot(int id) { return rc.canSenseRobot(id); }
    public static RobotInfo senseRobot(int id) throws GameActionException { return rc.senseRobot(id); }
    public static RobotInfo[] senseNearbyRobots() { return rc.senseNearbyRobots(); }
    public static RobotInfo[] senseNearbyRobots(int radiusSquared) { return rc.senseNearbyRobots(radiusSquared); }
    public static RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) { return rc.senseNearbyRobots(radiusSquared, team); }
    public static RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) { return rc.senseNearbyRobots(center, radiusSquared, team); }
    public static MapLocation[] detectNearbyRobots() { return rc.detectNearbyRobots(); }
    public static MapLocation[] detectNearbyRobots(int radiusSquared) { return rc.detectNearbyRobots(radiusSquared); }
    public static MapLocation[] detectNearbyRobots(MapLocation center, int radiusSquared) { return rc.detectNearbyRobots(center, radiusSquared); }
    public static double sensePassability(MapLocation loc) throws GameActionException { return rc.sensePassability(loc); }
    public static MapLocation adjacentLocation(Direction dir) { return rc.adjacentLocation(dir); }
    public static boolean isReady() { return rc.isReady(); }
    public static double getCooldownTurns() { return rc.getCooldownTurns(); }
    public static boolean canMove(Direction dir) { return rc.canMove(dir); }
    public static void move(Direction dir) throws GameActionException { rc.move(dir); }
    public static boolean canBuildRobot(RobotType type, Direction dir, int influence) { return rc.canBuildRobot(type, dir, influence); }
    public void buildRobot(RobotType type, Direction dir, int influence) throws GameActionException { rc.buildRobot(type, dir, influence); }
    public static boolean canEmpower(int radiusSquared) { return rc.canEmpower(radiusSquared); }
    public static void empower(int radiusSquared) throws GameActionException { rc.empower(radiusSquared); }
    public static boolean canExpose(MapLocation loc) { return rc.canExpose(loc); }
    public static void expose(MapLocation loc) throws GameActionException { rc.expose(loc); }
    public static boolean canBid(int influence) { return rc.canBid(influence); }
    public static void bid(int influence) throws GameActionException { rc.bid(influence); }
    public static boolean canSetFlag(int flag) { return rc.canSetFlag(flag); }
    public static void setFlag(int flag) throws GameActionException { rc.setFlag(flag); }
    public static boolean canGetFlag(int id) { return rc.canGetFlag(id); }
    public static int getFlag(int id) throws GameActionException { return rc.getFlag(id); }
    public static void resign() { rc.resign(); }
    public static void setIndicatorDot(MapLocation loc, int red, int green, int blue) { rc.setIndicatorDot(loc, red, green, blue); }
    public static void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) { rc.setIndicatorLine(startLoc, endLoc, red, green, blue); }
}
