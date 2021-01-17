package controllers;

import battlecode.common.*;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.Packet;
import communication.MarsNet.PacketHandler;
import util.HashSet11;
import util.CircQueue;

import java.util.ArrayList;
import java.util.Objects;

// Contains useful functions and data for all ECs
public strictfp abstract class CustomECController<E extends Enum<E> & IGetDataType> extends CustomRobotController<E> {
    public final HashSet11 friendlyLocations = new HashSet11();
    public final HashSet11 enemyLocations = new HashSet11();
    public final ArrayList<Integer> botIDs = new ArrayList<>();

    private final CircQueue<Packet<E>> messageQueue = new CircQueue<>(10);
    private Packet<E> currMessage;
    private Packet<E> nextMessage;
    private boolean flagLock = false;
    private boolean sendLock = false;

    public CustomECController(MarsNet<E> marsNet) {
        super(marsNet);
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
        try {
            for (int i = 0; i < botIDs.size(); i++) {
                int botID = botIDs.get(i);
                if (!canGetFlag(botID)) {
                    botIDs.set(i, botIDs.get(botIDs.size() - 1));
                    botIDs.remove(botIDs.size() - 1);
                    i--;
                    continue;
                }
                marsNet.getAndHandleF(botID, Objects::nonNull, ph);
            }
        } catch (GameActionException ignore) { } // Exception will never be thrown.
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
