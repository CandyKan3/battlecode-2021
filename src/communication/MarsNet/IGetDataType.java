package communication.MarsNet;

import communication.HeaderProtocol.IGetNumBits;

public interface IGetDataType extends IGetNumBits {
    DataType getDataType();

    @Override
    default int getNumBits() {
        return getDataType().getNumBits();
    }
}
