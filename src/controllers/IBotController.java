package controllers;

import battlecode.common.GameActionException;

public interface IBotController {
    void doTurn() throws GameActionException;
}
