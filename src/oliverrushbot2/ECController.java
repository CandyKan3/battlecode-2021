package oliverrushbot2;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;

import java.util.ArrayList;

public class ECController extends CustomECController<MessageType> {
    private Direction[] scoutDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private MessageType[] scoutDirectives = {MessageType.M_ScoutNorth, MessageType.M_ScoutEast, MessageType.M_ScoutSouth, MessageType.M_ScoutWest};
    private int numScoutDirections = 4;
    private int currScoutDirection = 0;
    private int scoutCooldown = 0;
    private int spawnCycle = 0;
    private ArrayList<Integer> scouts = new ArrayList<>();
    private int northYCoord;
    private int eastXCoord;
    private int southYCoord;
    private int westXCoord;
    private boolean doneScouting = false;
    private boolean spawnedScoutLastTurn;
    private boolean lockFlag;
    private int prevTeamVotes = 0;
    private int lastBid = 10;
    private int turn = 0;
    private final int totalTurns = 3000;

    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    private void removeScoutDir(Direction dir) {
        for (int i = 0; i < numScoutDirections - 1; i++) {
            if (scoutDirections[i] == dir) {
                scoutDirections[i] = scoutDirections[numScoutDirections - 1];
                scoutDirectives[i] = scoutDirectives[numScoutDirections - 1];
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

        // Bid all on last turn
        if (turn == totalTurns - 1) {
            bid(getInfluence());
            return;
        }

        boolean wonLastVote = getTeamVotes() != prevTeamVotes;
        if (wonLastVote)
            prevTeamVotes = getTeamVotes();

        int enemyVotes = turn - getTeamVotes();
        int voteDiff = getTeamVotes() - enemyVotes;
        int turnsLeft = totalTurns - 1 - turn;
        double votePriority = Math.min(Math.pow((-voteDiff / turnsLeft) + (-voteDiff / turnsLeft), 2), 1);         // Between 0 and 1 with 1 being highest priority

        int newBid = (int) (getInfluence() * votePriority);
        if (wonLastVote && votePriority < .2)
            newBid = Math.min(lastBid, newBid);

        newBid = Math.max(newBid, 1);
        if (canBid(newBid))
            bid(newBid);


        lockFlag = false;
        if (scoutCooldown > 0)
            scoutCooldown--;
        if (!doneScouting) {
            if (spawnedScoutLastTurn) {
                lockFlag = true;
                spawnedScoutLastTurn = false;
            } else if (getInfluence() > 30 && scoutCooldown == 0) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildRobotSafe(RobotType.MUCKRAKER, dir, 30)) {
                        scouts.add(getLastBuiltID());
                        marsNet.broadcastRaw(scoutDirectives[currScoutDirection], 0);
                        spawnedScoutLastTurn = true;
                        lockFlag = true;
                        currScoutDirection++;
                        if (currScoutDirection >= numScoutDirections) {
                            scoutCooldown = 30;
                            currScoutDirection %= numScoutDirections;
                        }
                        break;
                    }
                }
            }
            for (int i = 0; i < scouts.size(); i++) {
                int scoutID = scouts.get(i);
                if (!canGetFlag(scoutID)) {
                    scouts.set(i, scouts.get(scouts.size() - 1));
                    scouts.remove(scouts.size() - 1);
                    i--;
                    continue;
                }
                final int fi = i;
                i = marsNet.getAndHandleSafe(scoutID, (p) -> {
                    handleInfo:
                    {
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
                        return fi - 1;
                    }
                    return fi;
                });
            }
        } else {

            int influence = Math.max(21, getInfluence() / 3);
            RobotType buildType = RobotType.SLANDERER;
            switch (spawnCycle) {
                case 1:
                    buildType = RobotType.MUCKRAKER;
                    break;
                case 3:
                case 5:
                case 7:
                    buildType = RobotType.POLITICIAN;
                    break;
            }
            if (getInfluence() > influence) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildRobotSafe(buildType, dir, influence)) {
                        spawnCycle = (spawnCycle + 1) & 7;
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
            } else {
                marsNet.broadcastRaw(MessageType.A_StopZerg, 0);
            }
        }

        turn++;
    }
}
