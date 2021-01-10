package bot.tournament.sprint;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;

public class ECController extends CustomECController<MessageType> {
    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    public int turncount = 0;

    private MapLocation location = getLocation();
    private MapLocation[] potentialEnemyEC = {
            new MapLocation(location.x + 64, location.y),
            new MapLocation(location.x - 64, location.y),
            new MapLocation(location.x , location.y + 64),
            new MapLocation(location.x - 64, location.y - 64)
    };

    private int enemyECLocationsSearched = 0;
    private final int initialScoutingInfluence;


    @Override
    public void doTurn() throws GameActionException {
        int influence = 20 * getRobotCount();
        if (enemyECLocationsSearched < potentialEnemyEC.length) {
            if (getInfluence() >= initialScoutingInfluence) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildRobotSafe(RobotType.MUCKRAKER, dir, initialScoutingInfluence)) {
                        // Start bots to find enemy EC
                        marsNet.broadcastLocation(MessageType.M_Search, potentialEnemyEC[enemyECLocationsSearched++]);
                        break;
                    }
                }
            }
        } else if (getInfluence() > influence) {
            for (Direction dir : Direction.allDirections()) {
                if (buildRobotSafe(RobotType.SLANDERER, dir, influence))
                    break;
            }
        }

        handleBots((p) -> {
            MapLocation loc;
            switch (p.mType) {
                case FoundEnemyEC:
                    loc = p.asLocation();
                    neutralEC.add(loc);
                    enemyEC.add(loc);
                    break;
                case FoundNeutralEC:
                    loc = p.asLocation();
                    enemyEC.remove(loc);
                    neutralEC.add(loc);
                    break;
            }
            return null;
        });

        MapLocation toAttack = enemyEC.peek();
        if (toAttack == null) {
            return;
        }

        //going to rally the troops here
        //System.out.println("Preparing to zerg...");
        //calculating the midpoint between the target location and the ECCenter
        MapLocation curr = this.getLocation();
        int x = (curr.x + toAttack.x) / 2;
        int y = (curr.y + toAttack.y) / 2;
        MapLocation loc = new MapLocation(x, y);
        //System.out.println("Broadcasting "+ loc.x + " " + loc.y);

        if (turncount < 100) {
            marsNet.broadcastLocation(MessageType.Pre_Zerg, loc);
            turncount++;

        }

        if (toAttack != null && turncount >= 100) {
            // System.out.println("ZERGING BABY" + getRoundNum());
            marsNet.broadcastLocation(MessageType.S_Zerg, toAttack);


        }

    }
}
