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
    public final PriorityQueue<MapLocation> enemyEC = new PriorityQueue<>(12, Comparator.comparingInt(a -> a.distanceSquaredTo(getLocation())));
    public final PriorityQueue<MapLocation> neutralEC = new PriorityQueue<>(12, Comparator.comparingInt(a -> a.distanceSquaredTo(getLocation())));
    public final ArrayList<Integer> botIDs = new ArrayList<>();
    public final ArrayList<Integer>[] botIDs = new ArrayList[4];
    public final int ECID = getID();
    private int getLastBotID=0;

    private final CircQueue<Packet<E>> messageQueue = new CircQueue<>(10);
    private Packet<E> currMessage;
    private Packet<E> nextMessage;
    private boolean flagLock = false;
    private boolean sendLock = false;

    public CustomECController(MarsNet<E> marsNet) {
        super(marsNet);
        botIDs[0]= new ArrayList<>();
        botIDs[1] = new ArrayList<>();
        botIDs[2] = new ArrayList<>();
        botIDs[3] = new ArrayList<>();
        friendlyEC.add(ECID);
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
        } catch (GameActionException ignore) { }
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

    public void messageInit() {
        sendLock = false;
        flagLock = nextMessage != null;
        currMessage = nextMessage;
        nextMessage = null;
    }

    public void forceNextMessage(Packet<E> p) {
        if (nextMessage == null)
            nextMessage = p;
    }

    public boolean setMessage(Packet<E> p) {
        if (!flagLock) {
            currMessage = p;
            return true;
        }
        return false;
    }
}

    public void queueMessage(Packet<E> p) {
        messageQueue.push(p);
    }

    public boolean setMessageLock(Packet<E> p) {
        if (!flagLock) {
            currMessage = p;
            flagLock = true;
            return true;
        }
        return false;
    }

    public boolean sendMessage() {
        if (sendLock)
            return false;
        sendLock = true;
        if (currMessage != null) {
            marsNet.broadcast(currMessage);
            return true;
        }
        Packet<E> toSend = messageQueue.pop();
        if (toSend != null) {
            marsNet.broadcast(toSend);
            return true;
        }
        try {
            setFlag(0);
        } catch (GameActionException ignore) { }
        return false;
    }
}
