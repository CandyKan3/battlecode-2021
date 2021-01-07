package communication.MarsNet;

import battlecode.common.RobotType;

public enum MessageType {
    // Add messages below at will! Maximum 128. Order here does not matter!.
    NONE,
    FoundEnemyEC(ComType.OTHER, DataType.LOCATION),
    FoundNeutralEC(ComType.OTHER, DataType.LOCATION),
    FoundFriendlyEC(ComType.OTHER, DataType.LOCATION),
    S_Zerg(ComType.BOT, DataType.LOCATION),
    P_Zerg(ComType.BOT, DataType.LOCATION),
    M_Zerg(ComType.BOT, DataType.LOCATION),
    A_Zerg(ComType.BOT, DataType.LOCATION)
    ; // Leave this here

    public final ComType dst;
    public final DataType dType;
    MessageType() {
        dst = ComType.NONE;
        dType = DataType.RAW;
    }

    MessageType(ComType dst, DataType dType) {
        this.dst = dst;
        this.dType = dType;
    }

    protected int getHeader(RobotType src) {
        switch (this.dst) {
            case NONE:
                return 0;
            case EC:
                return (0x80 | this.ordinal()) << 16;
            case ALL:
            case BOT:
                return this.ordinal() << 16;
            case OTHER:
                return ((src == RobotType.ENLIGHTENMENT_CENTER ? 0x00 : 0x80) | this.ordinal()) << 16;
        }
        return 0;
    }
}
