package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

import javax.swing.*;
import java.util.Random;

// Contains useful functions and data for non-EC bots
public strictfp abstract class CustomUnitController<E extends Enum<E> & IGetDataType> extends CustomRobotController<E> {

    public final RobotInfo EC;
    private static Random r = new Random(getID());
    private final static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    private final static double invr2 = 1 / Math.sqrt(2);
    private final static double[] negDeltaXs = {0, -invr2, -1, -invr2, 0, invr2, 1, invr2};
    private final static double[] negDeltaYs = {-1, -invr2, 0, invr2, 1, invr2, 0, -invr2};

    public CustomUnitController(MarsNet<E> marsNet) {
        super(marsNet);
        findEC:
        {
            for (Direction dir : Direction.allDirections()) {
                MapLocation adj = adjacentLocation(dir);
                RobotInfo robot;
                try {
                    robot = senseRobotAtLocation(adj);
                } catch (GameActionException ignored) {
                    continue;
                }
                if (robot == null)
                    continue;
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == getTeam()) {
                    EC = robot;
                    break findEC;
                }
            }
            EC = new RobotInfo(0, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(0, 0));
        }
    }

    public CustomUnitController(CustomUnitController<E> cbc) {
        super(cbc);
        EC = cbc.EC;
    }

    public static boolean tryMoveRandom() throws GameActionException {
        for (int i = directions.length - 1; i > 0; i--) {
            int index = (int) (r.nextDouble() * i);
            Direction temp = directions[index];
            directions[index] = directions[i];
            directions[i] = temp;
        }
        for (Direction dir : directions) {
            if (canMove(dir)) {
                move(dir);
                return true;
            }
        }
        return false;
    }

    public static boolean tryMoveToward(MapLocation to) throws GameActionException {
        MapLocation from = getLocation();
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        Direction bestDirection = from.directionTo(to);
        Direction left = bestDirection.rotateLeft();
        int leftIdx = left.ordinal();
        double leftCor = dx * negDeltaXs[leftIdx] + dy * negDeltaYs[leftIdx];
        Direction right = bestDirection.rotateRight();
        int rightIdx = right.ordinal();
        double rightCor = dx * negDeltaXs[rightIdx] + dy * negDeltaYs[rightIdx];
        Direction nextBestDirection;
        boolean rotateLeft;
        if (rightCor < leftCor) {
            rotateLeft = true;
            nextBestDirection = right;
        } else {
            rotateLeft = false;
            nextBestDirection = left;
        }
        Direction temp;
        for (int i = 0; i < 5; i++) {
            if (canMove(bestDirection)) {
                move(bestDirection);
                return true;
            }
            if (rotateLeft)
                temp = bestDirection.rotateLeft();
            else
                temp = bestDirection.rotateRight();
            bestDirection = nextBestDirection;
            nextBestDirection = temp;
            rotateLeft = !rotateLeft;
        }
        return false;
    }

    public void circleEC() {
        if (r.nextDouble() < 0.1) {
            try {
                tryMoveRandom();
            } catch (GameActionException ignore) { }
            return;
        }
        MapLocation me = getLocation();
        double dx = EC.location.x - me.x;
        double dy = EC.location.y - me.y;
        int i_dx = (int) -Math.round(dx);
        int i_dy = (int) -Math.round(dy);
        MapLocation antiEC = me.translate(i_dx, i_dy);
        MapLocation leftEC = me.translate(-i_dy, i_dx);
        MapLocation rightEC = me.translate(i_dy, -i_dx);
        // If too close to the EC, try moving directly away from the EC. Could be replaced with something more useful...
        if (Math.max(Math.abs(dx), Math.abs(dy)) <= 2) {
            try {
                tryMoveToward(antiEC);
            } catch (GameActionException ignore) { }
            return;
        }
        double invhyp = 1.0 / Math.hypot(dx, dy);
        dx *= invhyp;
        dy *= invhyp;
        double b_dx, b_dy, dot;
        int behindCount = 0;
        int frontCount = 0;
        int sideCount = 0;
        // always sense 20 out so
        for (RobotInfo bot : senseNearbyRobots(20, getTeam())) {
            b_dx = bot.location.x - me.x;
            b_dy = bot.location.y - me.y;
            // The dot product of the vector from the unit to the other bot and the vector from the unit to the EC
            // The larger dot is, the more towards the EC the unit is, the smaller (negative) it is, the farther away
            // 0 (and close to 0) means that the unit is roughly "sideways"
            dot = b_dx * dx + b_dy * dy;
            // The thresholds here are pretty much arbitrary, play around and see what value works best
            if (dot > 2)
                behindCount++;
            else if (dot < -2)
                frontCount++;
            else
                sideCount++;
        }
        // here is where the most freedom exists, and how the behavior is largely controlled. Finding good logic
        // here means the difference between something awful and something awesome.
        try {
            if (behindCount < frontCount - sideCount + 2)
                tryMoveToward(EC.location);
            else if (frontCount < behindCount - sideCount)
                tryMoveToward(antiEC);
            else {
                if (r.nextFloat() < 0.5)
                    tryMoveToward(leftEC);
                else
                    tryMoveToward(rightEC);
            }
        } catch (GameActionException ignore) { }
    }

    public static boolean trySpreadMove() throws GameActionException {

        MapLocation[] nearbyRobots = detectNearbyRobots();

        double avgX = 0;
        double avgY = 0;
        for (MapLocation loc : nearbyRobots) {
            avgX += loc.x;
            avgY += loc.y;
        }
        avgX /= nearbyRobots.length;
        avgY /= nearbyRobots.length;

        MapLocation currLoc = getLocation();
        Direction dir = currLoc.directionTo(new MapLocation((int) avgX, (int) avgY)).opposite();

        if (canMove(dir)) {
            move(dir);
            return true;
        }

        return false;
    }
}

