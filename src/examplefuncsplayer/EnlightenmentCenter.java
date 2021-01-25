package examplefuncsplayer;

import battlecode.common.*;

import java.util.HashSet;

import static examplefuncsplayer.GameUtils.spawnableRobot;

public strictfp class EnlightenmentCenter extends GenericRobot {
    private static final Direction[] cardinals = Direction.cardinalDirections();

    private HashSet<Integer> allies;
    private HashSet<Integer> enemies;
    private Team team;
    private int mode;


    public EnlightenmentCenter(RobotController rc) {
        super(rc);
        allies = new HashSet<>();
        team = rc.getTeam();
        mode = 0;
    }

    @Override
    public void run() throws GameActionException {
        int influence = rc.getInfluence();
        int round = rc.getRoundNum();

        if (round == 1) {
            rc.setFlag(0); //Set flag to 0 for scouting mode
            if (rc.canBuildRobot(spawnableRobot[1], Direction.NORTH, influence - 5)) {
                rc.buildRobot(spawnableRobot[1], Direction.NORTH, influence - 5); //build slanderer 1st turn
            }
        } else if (round <= 5) {
            if (rc.canBuildRobot(spawnableRobot[0], cardinals[round - 2], 1)) {
                rc.buildRobot(spawnableRobot[0], cardinals[round - 2], 1); //build politician in each direction for scouting
            }
        } else if (influence >= 100) {

        } else if (round <= 100) {
            if (rc.canBuildRobot(spawnableRobot[1], Direction.NORTHEAST, influence)) {
                rc.buildRobot(spawnableRobot[1], Direction.NORTHEAST, influence); //build slanderer 1st turn
            }
        }

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : nearbyRobots) {
            if (robot.team == team && !allies.contains(robot.ID)) {
                allies.add(robot.ID);
            } else if (robot.team == team.opponent() && !enemies.contains(robot.ID)) {
                enemies.add(robot.ID);
            }
        }
    }
}
