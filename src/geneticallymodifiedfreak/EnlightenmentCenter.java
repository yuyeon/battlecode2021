package geneticallymodifiedfreak;

import battlecode.common.*;

import java.util.HashSet;

import static geneticallymodifiedfreak.GameUtils.*;

public strictfp class EnlightenmentCenter extends GenericRobot {
    private static final Direction[] cardinals = Direction.cardinalDirections();
    public static final int POL_SCOUTING_INF = 1, POL_NORMAL_INF = 100;

    private HashSet<Integer> allies;
    private HashSet<Integer> enemies;
    private Team team;
    private int mode;
    private Direction enemyDir;


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

        if(round <= 100){ // very early game
            if(round == 1){
                rc.setFlag(0); //Set flag to 0 for scouting mode
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTH, influence)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTH, influence); //build slanderer 1st turn
                }
            }
            else if(round <= 5){
                if(rc.canBuildRobot(RobotType.POLITICIAN, cardinals[round - 2], POL_SCOUTING_INF)){
                    rc.buildRobot(RobotType.POLITICIAN, cardinals[round - 2], POL_SCOUTING_INF); //build politician in each direction for scouting
                }
            }
            else if(influence >= 85){
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 85)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 85); //build slanderer whenever possible
                }
            }
        }
        else if(round <= 750){
            if(round % 10 == 0 || round % 10 == 1){
                Direction spawnDir = randomDirection();
                if(enemyDir != null) spawnDir = enemyDir;
                if(rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF)){
                    rc.buildRobot(RobotType.POLITICIAN, spawnDir, POL_NORMAL_INF); //build politician pair every 10 rounds
                }
            }
            else if(influence <= 949){
                if(rc.canBuildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949)){
                    rc.buildRobot(RobotType.SLANDERER, Direction.NORTHEAST, 949); //build slanderer whenever possible
                }
            }
        }

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo robot : nearbyRobots){
            if(robot.team.equals(team) && !allies.contains(robot.ID)){
                allies.add(robot.ID);
            }
            else if(robot.team.equals(team.opponent()) && !enemies.contains(robot.ID) ){
                enemies.add(robot.ID);
            }
        }

        // int bytecodes = Clock.getBytecodesLeft();

        for(Integer robotID : allies){
            if(rc.canGetFlag(robotID)){
                int flag = rc.getFlag(robotID);
                if(enemyAt(flag)){
                    MapLocation enemyLoc = getLocation(rc, flag);
                    enemyDir = rc.getLocation().directionTo(enemyLoc);
                }
            }
            else {
                allies.remove(robotID);
            }
        }
    }
}
