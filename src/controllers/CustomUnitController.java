package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

import java.util.Random;

// Contains useful functions and data for non-EC bots
public strictfp abstract class CustomUnitController<E extends Enum<E> & IGetDataType> extends CustomRobotController<E> {

    public final RobotInfo EC;
    private static Random r = new Random(getID());

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
        Direction[] directions = Direction.allDirections();
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
            slope = ((double) dy) / ((double) dx);
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
        } else if (slope <= 0.414214) {
            p = sidetoside;
            s = diagonal;
        } else {
            p = upanddown;
            s = diagonal;
        }
        boolean pRotateLeft = true;
        if (p.rotateLeft().equals(s))
            pRotateLeft = false;
        for (int i = 0; i < 5; i++) {
            if (canMove(p)) {
                move(p);
                return true;
            }
            Direction temp;
            if (pRotateLeft)
                temp = p.rotateLeft();
            else
                temp = p.rotateRight();
            p = s;
            s = temp;
            pRotateLeft = !pRotateLeft;
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

