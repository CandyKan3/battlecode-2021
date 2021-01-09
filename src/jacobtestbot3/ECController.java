package jacobtestbot3;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;
import org.hamcrest.number.IsCloseTo;

import java.util.ArrayList;

public class ECController extends CustomECController<MessageType> {
    private Direction[] scoutDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private MessageType[] scoutDirectives = {MessageType.M_ScoutNorth, MessageType.M_ScoutEast, MessageType.M_ScoutSouth, MessageType.M_ScoutWest};
    private int numScoutDirections = 4;
    private int currScoutDirection = 0;
    private ArrayList<Integer> scouts = new ArrayList<>();
    private int northYCoord;
    private int eastXCoord;
    private int southYCoord;
    private int westXCoord;
    private boolean doneScouting = false;
    private boolean spawnedScoutLastTurn;
    private boolean lockFlag;

    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    private void removeScoutDir(Direction dir) {
        for (int i = 0; i < numScoutDirections-1; i++) {
            if (scoutDirections[i] == dir) {
                scoutDirections[i] = scoutDirections[numScoutDirections-1];
                scoutDirectives[i] = scoutDirectives[numScoutDirections-1];
                break;
            }
        }
        numScoutDirections--;
        if (numScoutDirections == 0) {
            doneScouting = true;
            scoutDirections = null;
            scoutDirectives = null;
        } else {
            currScoutDirection %= numScoutDirections;
        }
    }

    @Override
    public void doTurn() throws GameActionException {
        lockFlag = false;
        if (!doneScouting) {
            if (spawnedScoutLastTurn) {
                lockFlag = true;
                spawnedScoutLastTurn = false;
            }
            for (Direction dir : Direction.allDirections()) {
                if (buildRobotSafe(RobotType.MUCKRAKER, dir, 100)) {
                    scouts.add(getLastBuiltID());
                    marsNet.broadcastRaw(scoutDirectives[currScoutDirection],0);
                    spawnedScoutLastTurn = true;
                    lockFlag = true;
                    currScoutDirection = (currScoutDirection + 1) % numScoutDirections;
                    break;
                }
            }
            for (int i = 0; i < scouts.size(); i++) {
                int scoutID = scouts.get(i);
                if (!canGetFlag(scoutID)) {
                    scouts.set(i, scouts.get(scouts.size()-1));
                    scouts.remove(scouts.size()-1);
                    i--;
                    continue;
                }
                final int fi = i;
                i = marsNet.getAndHandleSafe(scoutID, (p) -> {
                    handleInfo: {
                        switch (p.mType) {
                            case FoundNorth:
                                northYCoord = p.asYCoord();
                                removeScoutDir(Direction.NORTH);
                                break;
                            case FoundEast:
                                eastXCoord = p.asXCoord();
                                removeScoutDir(Direction.EAST);
                                break;
                            case FoundSouth:
                                southYCoord = p.asYCoord();
                                removeScoutDir(Direction.SOUTH);
                                break;
                            case FoundWest:
                                westXCoord = p.asXCoord();
                                removeScoutDir(Direction.WEST);
                                break;
                            default:
                                break handleInfo;
                        }
                        scouts.set(fi, scouts.get(scouts.size() - 1));
                        scouts.remove(scouts.size() - 1);
                        return fi-1;
                    }
                    return fi;
                });
            }
        } else {
            int influence = Math.max(21, getInfluence() / 3);
            if (getInfluence() > influence) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildRobotSafe(RobotType.SLANDERER, dir, influence)) {
                        break;
                    }
                }
            }
        }

        handleBots((p) -> {
            MapLocation loc;
            switch (p.mType) {
                case FoundEnemyEC:
                    loc = p.asLocation();
                    if (enemyEC.contains(loc))
                        break;
                    neutralEC.add(loc);
                    enemyEC.add(loc);
                    break;
                case FoundNeutralEC:
                    loc = p.asLocation();
                    if (neutralEC.contains(loc))
                        break;
                    enemyEC.remove(loc);
                    neutralEC.add(loc);
                    break;
            }
            return null;
        });

        if (!lockFlag) {
            MapLocation attackLocation = enemyEC.peek();
            if (attackLocation != null) {
                marsNet.broadcastLocation(MessageType.A_Zerg, attackLocation);
            }
        }
    }
}
