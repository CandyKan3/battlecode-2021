package olivertournament2;

import communication.MarsNet.DataType;
import communication.MarsNet.Filters.BotType;
import communication.MarsNet.Filters.IGetDestination;
import communication.MarsNet.IGetDataType;

public enum MessageType implements IGetDataType, IGetDestination {
    // Add messages below at will!
    NONE,
    M_ScoutNorth,
    M_ScoutNorthEast,
    M_ScoutNorthWest,
    M_ScoutEast,
    M_ScoutSouth,
    M_ScoutSouthEast,
    M_ScoutSouthWest,
    M_ScoutWest,
    FoundEnemyEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    FoundNeutralEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    P_Zerg(BotType.POLITICIAN, DataType.LOCATION),
    A_Zerg(BotType.UNIT, DataType.LOCATION),
    S_Turtle(BotType.SLANDERER, DataType.LOCATION)
    ; // Leave this here

    public final BotType dest;
    public final DataType dType;
    MessageType() {
        dest = BotType.NONE;
        dType = DataType.NONE;
    }

    MessageType(BotType dest, DataType dType) {
        this.dest = dest;
        this.dType = dType;
    }

    @Override
    public DataType getDataType() {
        return this.dType;
    }

    @Override
    public BotType getDestination() {
        return dest;
    }
}
