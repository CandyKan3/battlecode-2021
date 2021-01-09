package jacobtestbot3;

import communication.MarsNet.DataType;
import communication.MarsNet.Filters.BotType;
import communication.MarsNet.Filters.IGetDestination;
import communication.MarsNet.IGetDataType;

public enum MessageType implements IGetDataType, IGetDestination {
    // Add messages below at will! Maximum 128. Order here does not matter!.
    //NONE,
    FoundEnemyEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    FoundNeutralEC(BotType.ENLIGHTENMENT_CENTER, DataType.LOCATION),
    S_Zerg(BotType.SLANDERER, DataType.LOCATION),
    //Test(BotType.NONE, DataType.ID),
    F1(BotType.NONE, DataType.LOCATION),
    F2(BotType.NONE, DataType.LOCATION),
    F3(BotType.NONE, DataType.LOCATION),
    F4(BotType.NONE, DataType.LOCATION),
    F5(BotType.NONE, DataType.LOCATION),
    F6(BotType.NONE, DataType.LOCATION),
    F7(BotType.NONE, DataType.LOCATION),
    F8(BotType.NONE, DataType.LOCATION),
    F9(BotType.NONE, DataType.LOCATION),
    F10(BotType.NONE, DataType.LOCATION),
    F11(BotType.NONE, DataType.LOCATION)
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
