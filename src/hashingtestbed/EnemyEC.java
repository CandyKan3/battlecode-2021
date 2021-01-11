package hashingtestbed;

import battlecode.common.MapLocation;

public class EnemyEC {
    public MapLocation loc;
    public EnemyEC(int x, int y){
       loc = new MapLocation(x,y);
    }
    @Override
    public int hashCode(){
        int val = this.loc.x;
        val= (val<<5)-val; //Multiply by 31 to reduce collisions
        val+=this.loc.y;
        return val;
    }
}
