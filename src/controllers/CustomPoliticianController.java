package controllers;

import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

public strictfp abstract class CustomPoliticianController<E extends Enum<E> & IGetDataType> extends CustomUnitController<E> {

    public CustomPoliticianController(MarsNet<E> marsNet) {
        super(marsNet);
    }

    public CustomPoliticianController(CustomUnitController<E> cbc) {
        super(cbc);
    }
}
