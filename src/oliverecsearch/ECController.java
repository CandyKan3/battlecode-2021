package oliverecsearch;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import communication.MarsNet.MarsNet;
import communication.MarsNet.MessageType;
import controllers.CustomECController;

public class ECController extends CustomECController {

    private boolean searchCornerToggleBotLeft = true;
    private MapLocation botLeft = new MapLocation(0, 0);
    private final int maxBoardSize = 30000 + 64;
    private MapLocation topRight = new MapLocation(maxBoardSize, maxBoardSize);

    @Override
    public void doTurn() throws GameActionException {

        int influence = 20 * getRobotCount();

        // Locate enemy EC early
        if (turn < 50) {
            if (getInfluence() >= 1) {
                for (Direction dir : Direction.allDirections()) {
                    if (buildRobotSafe(RobotType.MUCKRAKER, dir, 1)) {
                        // Start bots to find corners of grid
                        MapLocation corner = searchCornerToggleBotLeft ? botLeft : topRight;
                        searchCornerToggleBotLeft = !searchCornerToggleBotLeft;
                        MarsNet.broadcastLocation(MessageType.M_Search, corner);
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
        if (toAttack != null)
            MarsNet.broadcastLocation(MessageType.S_Zerg, toAttack);
    }
}
