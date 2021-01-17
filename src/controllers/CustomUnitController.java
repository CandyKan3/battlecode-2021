package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

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

