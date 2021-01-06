package examplefuncsplayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;


                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

    }

    static void runEnlightenmentCenter() throws GameActionException {

        int inf = 60;
        for(int i=0; i<2; i++){
            turnCount += 1;
            RobotType startbuild = spawnableRobot[1];
            for (Direction dir : directions) {
                if (rc.canBuildRobot(startbuild, dir, inf)) {
                    rc.buildRobot(startbuild, dir, inf);
                } else {
                    break;
                }
            }
        }
        for(int i=0; i<4; i++){
            inf=10;
            turnCount += 1;
            RobotType startbuild = spawnableRobot[2];
            for (Direction dir : directions) {
                if (rc.canBuildRobot(startbuild, dir, inf)) {
                    rc.buildRobot(startbuild, dir, inf);
                } else {
                    break;
                }
            }
        }

        //going to ramp here
        while (true) {
            turnCount += 1;
            RobotType toBuild = spawnableRobot[1];
            int influence = 100;
            for (Direction dir : directions) {
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                } else {
                    break;
                }
            }
            Clock.yield();
        }
    }

    static void runPolitician() throws GameActionException {
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze

            Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
            if (tryMove(randomDirection()))
                System.out.println("I moved!");
            Clock.yield();
        }

    }

    static void runSlanderer() throws GameActionException {
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            if (tryMove(randomDirection()))
                System.out.println("I moved!");
            Clock.yield();
        }
    }

    static void runMuckraker() throws GameActionException {
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze

            Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
            if (tryMove(randomDirection()))
                System.out.println("I moved!");
            Clock.yield();
        }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
