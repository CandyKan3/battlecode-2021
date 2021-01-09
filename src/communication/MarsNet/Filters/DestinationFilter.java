package communication.MarsNet.Filters;

import battlecode.common.RobotType;
import communication.MarsNet.IGetDataType;
import communication.MarsNet.MarsNet;

public class DestinationFilter {
    private static RobotType getRType() {
        return MarsNet.rc.getType();
    }
    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean EC(E mType) {
        if (getRType() != RobotType.ENLIGHTENMENT_CENTER)
            return false;
        switch (mType.getDestination()) {
            case ENLIGHTENMENT_CENTER:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean Politician(E mType) {
        if (getRType() != RobotType.POLITICIAN)
            return false;
        switch (mType.getDestination()) {
            case POLITICIAN:
            case UNIT:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean Slanderer(E mType) {
        if (getRType() != RobotType.SLANDERER)
            return false;
        switch (mType.getDestination()) {
            case SLANDERER:
            case UNIT:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean Muckraker(E mType) {
        if (getRType() != RobotType.MUCKRAKER)
            return false;
        switch (mType.getDestination()) {
            case MUCKRAKER:
            case UNIT:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean None(E mType) {
        return false;
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean Unit(E mType) {
        if (getRType() == RobotType.ENLIGHTENMENT_CENTER)
            return false;
        switch (mType.getDestination()) {
            case POLITICIAN:
            case SLANDERER:
            case MUCKRAKER:
            case UNIT:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public static <E extends Enum<E> & IGetDataType & IGetDestination> boolean All(E mType) {
        return mType.getDestination() != BotType.NONE;
    }

    private DestinationFilter() { }
}
