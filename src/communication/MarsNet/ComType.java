package communication.MarsNet;

import battlecode.common.RobotType;

public enum ComType {
    NONE,
    EC,
    BOT,
    OTHER,
    ALL;

    public RobotType getRType(RobotType rType) {
        switch (this) {
            case NONE:
            case ALL:
                return null;
            case EC:
                return RobotType.ENLIGHTENMENT_CENTER;
            case BOT:
                return RobotType.POLITICIAN;
            case OTHER:
                return rType == RobotType.ENLIGHTENMENT_CENTER ? RobotType.POLITICIAN : RobotType.ENLIGHTENMENT_CENTER;
        }
        return null;
    }
}
