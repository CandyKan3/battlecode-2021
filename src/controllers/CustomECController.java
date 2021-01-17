package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.PacketHandler;

import java.util.ArrayList;
import java.util.PriorityQueue;

// Contains useful functions and data for all ECs
public strictfp abstract class CustomECController<E extends Enum<E> & IGetDataType> extends CustomRobotController<E> {
    public final int x = getLocation().x;
    public final int y = getLocation().y;
    public final PriorityQueue<Integer> friendlyEC = new PriorityQueue<>();
    public final PriorityQueue<MapLocation> enemyEC = new PriorityQueue<>(12, (a, b) -> (int) Math.round((getDistanceTo(a) - getDistanceTo(b))));
    public final PriorityQueue<MapLocation> neutralEC = new PriorityQueue<>(12, (a, b) -> (int) Math.round((getDistanceTo(a) - getDistanceTo(b))));
    public final ArrayList<Integer>[] botIDs = new ArrayList[4];
    private final ArrayList<Integer> idset1 =new ArrayList<Integer>();
    public final int ECID = getID();
    private int getLastBotID=0;
    public CustomECController(MarsNet<E> marsNet) {

        super(marsNet);
        botIDs[0]= new ArrayList<Integer>();
        botIDs[1] = new ArrayList<Integer>();
        botIDs[2] = new ArrayList<Integer>();
        botIDs[3] = new ArrayList<Integer>();
        friendlyEC.add(ECID);
    }

    public boolean isMaster() {
        //noinspection ConstantConditions
        return friendlyEC.peek() == ECID;
    }

    public int getMaster() {
        //noinspection ConstantConditions
        return friendlyEC.peek();
    }

    @Override
    public void buildRobot(RobotType robotType, Direction direction, int i) throws GameActionException {
        super.buildRobot(robotType, direction, i);
        MapLocation botloc = adjacentLocation(direction);
        RobotInfo ri = senseRobotAtLocation(botloc);
        botIDs[ri.ID & 0x3].add(ri.ID);
        getLastBotID=ri.ID;
    }

    public boolean buildRobotSafe(RobotType robotType, Direction direction, int i) {
        try {
            if (canBuildRobot(robotType, direction, i)) {
                buildRobot(robotType, direction, i);
                return true;
            }
        } catch (GameActionException ignore) {
        }
        return false;
    }

    public int getLastBuiltID() {
        return getLastBotID;
    }

    public void handleBots(PacketHandler<?, E> ph) {
        ArrayList<Integer> turnIDs = botIDs[getRoundNum() & 0x3];
        for (int i = 0; i < turnIDs.size(); i++) {
            int botID = turnIDs.get(i);
            if (!canGetFlag(botID)) {
                turnIDs.set(i, turnIDs.get(turnIDs.size() - 1));
                turnIDs.remove(turnIDs.size() - 1);
                i--;
                continue;
            }
            marsNet.getAndHandleSafe(botID, ph);
        }
    }

    private double getDistanceTo(MapLocation ml) {
        return Math.sqrt((ml.x - x) * (ml.x - x) + (ml.y - y) * (ml.y - y));
    }
}

