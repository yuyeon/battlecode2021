package geneticallymodifiedfreak;

import battlecode.common.*;

import static geneticallymodifiedfreak.GameUtils.randomDirection;
import static geneticallymodifiedfreak.GameUtils.tryMove;

public strictfp class Politician extends GenericRobot {

    private static final int CONVICTION_THRESHOLD = 10, EMPOWER_COST = 10, POWERUP_DELAY = 0;

    public Politician(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        if (isScouting()) {
            scoutingRun();
        } else {
            normalRun();
        }
    }

    private void scoutingRun() {
        // TODO: scouting
    }

    private void normalRun() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(actionRadius, team);
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);
        boolean canEmpower = rc.canEmpower(actionRadius);
        int empowerNum = friendlies.length + enemies.length;
        int conviction = rc.getConviction();
        int convictionReceivedIfEmpowered = (conviction - EMPOWER_COST) / empowerNum;

        // TODO: if no robots are present, maybe move?
        // TODO: strategy, if enemy in sensing distance, don't move

        RobotInfo bestBadPolitician = null, worstFriendly = null, bestFriendly = null;
        for (RobotInfo friendly : friendlies) {
            int fc = friendly.getConviction();
            if (fc < CONVICTION_THRESHOLD && friendly.getType() == RobotType.POLITICIAN
                    && (bestBadPolitician == null || fc > bestBadPolitician.getConviction())) {
                bestBadPolitician = friendly;
            }
            if (worstFriendly == null || fc < worstFriendly.getConviction()) {
                worstFriendly = friendly;
            }
            if (bestFriendly == null || fc > bestFriendly.getConviction()) {
                bestFriendly = friendly;
            }
        }

        RobotInfo worstEnemy = null, bestEnemy = null;
        for (RobotInfo enemy : enemies) {
            int ec = enemy.getConviction();
            if (worstEnemy == null || ec < worstEnemy.getConviction()) {
                worstEnemy = enemy;
            }
            if (bestEnemy == null || ec > bestEnemy.getConviction()) {
                bestEnemy = enemy;
            }
        }

        boolean shouldEmpower = shouldKms(conviction, bestFriendly)
                || shouldPowerUpFriendlyPolitician(conviction, convictionReceivedIfEmpowered, bestBadPolitician)
                || shouldAttack(conviction, convictionReceivedIfEmpowered, worstEnemy, friendlies.length);
        if (canEmpower && shouldEmpower) {
            rc.empower(actionRadius);
        }

        // TODO: implement flight (run away) logic
    }

    private boolean isScouting() {
        // TODO: return scouting
        return rc.getInfluence() == 1;
    }

    private static boolean shouldKms(int conviction, RobotInfo bestFriendly) {
        if (bestFriendly == null) {
            return false;
        }
        return conviction < CONVICTION_THRESHOLD
                && bestFriendly.getConviction() < CONVICTION_THRESHOLD;
    }

    private boolean shouldPowerUpFriendlyPolitician(int conviction, int received, RobotInfo worstFriendly) {
        if (worstFriendly == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && worstFriendly.getType() == RobotType.POLITICIAN
                && worstFriendly.getConviction() + received >= CONVICTION_THRESHOLD
                && (worstFriendly.getConviction() < CONVICTION_THRESHOLD || turnCount >= POWERUP_DELAY);
    }

    private static boolean shouldAttack(int conviction, int received, RobotInfo worstEnemy, int numFriendlies) {
        if (worstEnemy == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && (worstEnemy.getConviction() - received <= 0 || numFriendlies > 0);
    }
}