package hashingtestbed;

import battlecode.common.MapLocation;

public class EnemyEC {
    int x;
    int y;
    public EnemyEC(int x, int y){
       this.x =x;
       this.y =y;
    }
    @Override
    public boolean equals(Object obj){
        return (this.hashCode()==obj.hashCode());

    }
    @Override
    public int hashCode(){
        return (x << 16) | y;
    }
}
