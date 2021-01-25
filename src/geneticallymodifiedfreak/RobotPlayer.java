package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.Random;

import static geneticallymodifiedfreak.GameUtils.directions;

public strictfp class RobotPlayer {
    static RobotController rc;
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

        GenericRobot robot = null;
        RobotType lastType = null;

        while (true) {
            turnCount++;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                if (lastType == null || lastType != rc.getType()) {
                    lastType = rc.getType();
                    switch (lastType) {
                        case ENLIGHTENMENT_CENTER:
                            robot = new EnlightenmentCenter(rc);
                            break;
                        case POLITICIAN:
                            robot = new Politician(rc);
                            break;
                        case SLANDERER:
                            robot = new Slanderer(rc);
                            break;
                        case MUCKRAKER:
                            robot = new Muckraker(rc);
                            break;
                    }
                }
                if (robot != null) {
                    robot.turnCount = turnCount;
                    robot.run();
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
