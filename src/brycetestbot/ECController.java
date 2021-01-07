package brycetestbot;

import battlecode.common.*;
import communication.MarsNet.MarsNet;
import communication.MarsNet.MessageType;
import controllers.CustomECController;

public class ECController extends CustomECController {
public int turncount=0;
public boolean prepping= false;
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
        if(toAttack==null){
            return;
        }

        //going to rally the troops here
        //System.out.println("Preparing to zerg...");
        //calculating the midpoint between the target location and the ECCenter
        MapLocation curr = this.getLocation();
        int x = (curr.x + toAttack.x)/2;
        int y = (curr.y + toAttack.y)/2;
        MapLocation loc = new MapLocation(x,y);
        //System.out.println("Broadcasting "+ loc.x + " " + loc.y);

        if(turncount<100){
            MarsNet.broadcastLocation(MessageType.Pre_Zerg, loc);
            turncount++;

        }

        if (toAttack != null&&turncount>=100){
           // System.out.println("ZERGING BABY" + getRoundNum());
            MarsNet.broadcastLocation(MessageType.S_Zerg, toAttack);


        }

    }
}
