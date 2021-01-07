package controllers;

import battlecode.common.*;
import communication.MarsNet.*;
import util.PriorityElement;

import java.util.ArrayList;
import java.util.PriorityQueue;

// Contains useful functions and data for all ECs
public strictfp abstract class CustomECController extends CustomRobotController {
    public final PriorityQueue<Integer> friendlyEC = new PriorityQueue<>();
    public final PriorityQueue<MapLocation> enemyEC = new PriorityQueue<>();
    public final PriorityQueue<MapLocation> neutralEC = new PriorityQueue<>();
    public final PriorityQueue<PriorityElement<Packet>> messageQueue = new PriorityQueue<>();
    public final ArrayList<Integer> botIDs = new ArrayList<>();
    public final int ECID = getID();

    public CustomECController() {
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
            buildRobot(robotType, direction, i);
            return true;
        } catch (GameActionException ignore) { }
        return false;
    }

    public void handleBots(PacketHandler<Void> ph) {
        for (int i = 0; i < botIDs.size(); i++) {
            int botID = botIDs.get(i);
            if (!canGetFlag(botID)) {
                botIDs.set(i, botIDs.get(botIDs.size()-1));
                botIDs.remove(botIDs.size()-1);
                i--;
                continue;
            }
            MarsNet.getAndHandleSafe(botID, ComType.BOT, ph);
        }
    }
}
