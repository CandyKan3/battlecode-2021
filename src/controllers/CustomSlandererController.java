package controllers;

import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

public strictfp abstract class CustomSlandererController<E extends Enum<E> & IGetDataType> extends CustomUnitController<E> {

    public CustomSlandererController(MarsNet<E> marsNet) {
        super(marsNet);
    }
}
