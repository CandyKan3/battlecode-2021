package controllers;

import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

public strictfp abstract class CustomMuckrakerController<E extends Enum<E> & IGetDataType> extends CustomUnitController<E> {

    public CustomMuckrakerController(MarsNet<E> marsNet) {
        super(marsNet);
    }
}
