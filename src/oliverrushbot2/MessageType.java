package oliverrushbot2;

import communication.MarsNet.DataType;
import communication.MarsNet.Filters.BotType;
import communication.MarsNet.Filters.IGetDestination;
import communication.MarsNet.IGetDataType;

public enum MessageType implements IGetDataType, IGetDestination {
    NONE,
    M_ScoutNorth,
    M_ScoutEast,
    M_ScoutSouth,
    M_ScoutWest,
    P_StopZerg,
    S_StopZerg,
    M_StopZerg,
    A_StopZerg,
    FoundNorth(BotType.ENLIGHTENMENT_CENTER, DataType.YCOORD),
    FoundEast(BotType.ENLIGHTENMENT_CENTER, DataType.XCOORD),
    FoundSouth(BotType.ENLIGHTENMENT_CENTER, DataType.YCOORD),
    FoundWest(BotType.ENLIGHTENMENT_CENTER, DataType.XCOORD),
    FoundEnemyEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    FoundNeutralEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    P_Zerg(BotType.POLITICIAN, DataType.LOCATION),
    S_Zerg(BotType.SLANDERER, DataType.LOCATION),
    M_Zerg(BotType.MUCKRAKER, DataType.LOCATION),
    A_Zerg(BotType.UNIT, DataType.LOCATION)
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
