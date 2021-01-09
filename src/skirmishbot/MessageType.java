package skirmishbot;

import communication.MarsNet.DataType;
import communication.MarsNet.Filters.BotType;
import communication.MarsNet.Filters.IGetDestination;
import communication.MarsNet.IGetDataType;

public enum MessageType implements IGetDataType, IGetDestination {
    // Add messages below at will!
    NONE,
    M_Search(BotType.UNIT, DataType.LOCATION),
    Pre_Zerg(BotType.UNIT, DataType.LOCATION),
    FoundEnemyEC(BotType.ALL, DataType.LOCATION),
    FoundNeutralEC(BotType.ALL, DataType.LOCATION),
    S_Zerg(BotType.UNIT, DataType.LOCATION),
    M_Zerg(BotType.UNIT, DataType.LOCATION),

    ; // Leave this here

    public final BotType dest;
    public final DataType dType;

    MessageType() {
        dest = BotType.NONE;
        dType = DataType.RAW;
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
