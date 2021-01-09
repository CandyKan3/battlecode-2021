package jacobtestbot3;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import controllers.CustomECController;

public class ECController extends CustomECController<MessageType> {

    public ECController(MarsNet<MessageType> marsNet) {
        super(marsNet);
    }

    @Override
    public void doTurn() throws GameActionException {
        int influence = 20*getRobotCount();
        //int influence = (((getInfluence()) / 40) + 1)*20;
        if (getInfluence() > influence) {
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
            marsNet.broadcastLocation(MessageType.S_Zerg, toAttack);
    }
}
