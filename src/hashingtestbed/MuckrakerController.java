package hashingtestbed;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomMuckrakerController;

import java.util.HashSet;
import java.util.Set;

public class MuckrakerController extends CustomMuckrakerController<MessageType> {
    private MapLocation attackLocation = null;
    private MapLocation scoutLocation = null;
    private Direction scoutDirection = null;
    private boolean scouting;
    private boolean lockFlag;
    private final Team enemy;
    private final int actionRadius;
    private Set<EnemyEC> foundEECs = new HashSet<EnemyEC>();
    int[] bigbrainfoundEECs = new int[11];
    private int currsize = 0;
    public MuckrakerController(MarsNet<MessageType> marsNet) {
        super(marsNet);
        enemy = getTeam().opponent();
        actionRadius = getType().actionRadiusSquared;

        scouting = marsNet.getAndHandleSafe(EC.ID, (p) -> {
            switch (p.mType) {
                case M_ScoutNorth:
                    scoutLocation = EC.location.translate(0, 64);
                    scoutDirection = Direction.NORTH;
                    return true;
                case M_ScoutEast:
                    scoutLocation = EC.location.translate(64, 0);
                    scoutDirection = Direction.EAST;
                    return true;
                case M_ScoutSouth:
                    scoutLocation = EC.location.translate(0, -64);
                    scoutDirection = Direction.SOUTH;
                    return true;
                case M_ScoutWest:
                    scoutLocation = EC.location.translate(-64, 0);
                    scoutDirection = Direction.WEST;
                    return true;
            }
            return false;
        });
    }

    private void stopScouting() {
        scouting = false;
    }

    @Override
    public void doTurn() throws GameActionException {
        lockFlag = false;
        if (scouting) {
            int dx = 5*scoutDirection.getDeltaX();
            int dy = 5*scoutDirection.getDeltaY();
            MapLocation peekLocation = getLocation().translate(dx, dy);
            if (!onTheMap(peekLocation)) {
                peekLocation = peekLocation.add(scoutDirection.opposite());
                while (!onTheMap(peekLocation)) {
                    peekLocation = peekLocation.add(scoutDirection.opposite());
                }
                MessageType mt = MessageType.FoundNorth;
                int coord = peekLocation.y;
                switch (scoutDirection) {
                    case SOUTH:
                        mt = MessageType.FoundSouth;
                        break;
                    case EAST:
                        mt = MessageType.FoundEast;
                        coord = peekLocation.x;
                        break;
                    case WEST:
                        mt = MessageType.FoundWest;
                        coord = peekLocation.x;
                        break;
                }
                marsNet.broadcastRaw(mt, coord);
                lockFlag = true;
                scouting = false;
            }
        } else {
            attackLocation = marsNet.getAndHandleSafe(EC.ID, (p) -> {
                switch (p.mType) {
                    case A_Zerg:
                    case M_Zerg:
                        return p.asLocation();
                    case A_StopZerg:
                    case M_StopZerg:
                        return null;
                }
                return attackLocation;
            });
        }

        boolean exposed = false;
        for (RobotInfo robot : senseNearbyRobots(actionRadius, enemy)) {
            if (!exposed && robot.type.canBeExposed() && canExpose(robot.location)) {
                expose(robot.location);
                exposed = true;
                continue;
            }
            if (!lockFlag && robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                boolean broadcast= false;

                int lochash = (robot.location.x << 16) | robot.location.y;
                int currcount= Clock.getBytecodeNum();
                broadcast=isin(lochash, currsize);
               System.out.println(Clock.getBytecodeNum()-currcount);


                if(!broadcast){

                    marsNet.broadcastLocation(MessageType.FoundEnemyEC, robot.location);
                    lockFlag = true;

                }
                else{
                    currsize++;
                    //System.out.println("BYTECODE"+ (Clock.getBytecodeNum()-currbytes));
                }


            }
        }

        if (scouting) {
            tryMoveToward(scoutLocation);
        } else {
            if (attackLocation != null) {
                MapLocation me = getLocation();
                if (me.isAdjacentTo(attackLocation)) {
                    for (Direction dir : Direction.allDirections()) {
                        if (me.add(dir).isAdjacentTo(attackLocation)) {
                            if (canMove(dir)) {
                                move(dir);
                                break;
                            }
                        }
                    }
                } else {
                    tryMoveToward(attackLocation);
                }
            } else {
                tryMoveRandom();
            }
        }

    }
    public boolean isin(int lochash, int count){
        if(bigbrainfoundEECs[0]==lochash||bigbrainfoundEECs[1]==lochash||bigbrainfoundEECs[2]==lochash||bigbrainfoundEECs[3]==lochash||bigbrainfoundEECs[4]==lochash||bigbrainfoundEECs[5]==lochash||bigbrainfoundEECs[6]==lochash
                ||bigbrainfoundEECs[7]==lochash||bigbrainfoundEECs[8]==lochash||bigbrainfoundEECs[9]==lochash||bigbrainfoundEECs[10]==lochash){
            return true;
        }
        else{
            bigbrainfoundEECs[count]=lochash;
            return false;
        }
    }


}
