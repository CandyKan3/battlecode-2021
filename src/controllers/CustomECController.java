package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.PacketHandler;

import java.util.ArrayList;
import java.util.PriorityQueue;

// Contains useful functions and data for all ECs
public strictfp abstract class CustomECController<E extends Enum<E> & IGetDataType> extends CustomRobotController<E> {
    public final PriorityQueue<Integer> friendlyEC = new PriorityQueue<>();
    public final PriorityQueue<MapLocation> enemyEC = new PriorityQueue<>();
    public final PriorityQueue<MapLocation> neutralEC = new PriorityQueue<>();
    public final ArrayList<Integer> botIDs = new ArrayList<>();
    public final int ECID = getID();

    public CustomECController(MarsNet<E> marsNet) {
        super(marsNet);
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
        botIDs.add(ri.ID);
    }

    public boolean buildRobotSafe(RobotType robotType, Direction direction, int i) {
        try {
            if (canBuildRobot(robotType, direction, i)) {
                buildRobot(robotType, direction, i);
                return true;
            }
        } catch (GameActionException ignore) { }
        return false;
    }

    public int getLastBuiltID() {
        return botIDs.get(botIDs.size() - 1);
    }

    public void handleBots(PacketHandler<?, E> ph) {
        for (int i = 0; i < botIDs.size(); i++) {
            int botID = botIDs.get(i);
            if (!canGetFlag(botID)) {
                botIDs.set(i, botIDs.get(botIDs.size()-1));
                botIDs.remove(botIDs.size()-1);
                i--;
                continue;
            }
            marsNet.getAndHandleSafe(botID, ph);
        }
    }
}
