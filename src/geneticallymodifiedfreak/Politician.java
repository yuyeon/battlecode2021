package geneticallymodifiedfreak;

import battlecode.common.*;

public strictfp class Politician extends GenericRobot {

    private static final int CONVICTION_THRESHOLD = 10;

    public Politician(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team team = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(actionRadius, team);
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);
        boolean canEmpower = rc.canEmpower(actionRadius);
        int empowerNum = friendlies.length + enemies.length;
        int conviction = rc.getConviction();
        int convictionReceivedIfEmpowered = conviction / empowerNum;

        // TODO: if no robots are present, maybe move?

        RobotInfo bestBadPolitician = null, worstFriendly = null, bestFriendly = null;
        for (RobotInfo friendly : friendlies) {
            int fc = friendly.conviction;
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
                || shouldConvertEnemy(conviction, convictionReceivedIfEmpowered, worstEnemy);
        if (canEmpower && shouldEmpower) {
            rc.empower(actionRadius);
        }

        // TODO: implement flight (run away) logic
    }

    private static boolean shouldKms(int conviction, RobotInfo bestFriendly) {
        if (bestFriendly == null) {
            return false;
        }
        return conviction < CONVICTION_THRESHOLD
                && bestFriendly.getConviction() < CONVICTION_THRESHOLD;
    }

    private static boolean shouldPowerUpFriendlyPolitician(int conviction, int received, RobotInfo worstFriendly) {
        if (worstFriendly == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && worstFriendly.getType() == RobotType.POLITICIAN
                && worstFriendly.getConviction() < CONVICTION_THRESHOLD
                && worstFriendly.getConviction() + received >= CONVICTION_THRESHOLD;
    }

    private static boolean shouldConvertEnemy(int conviction, int received, RobotInfo worstEnemy) {
        if (worstEnemy == null) {
            return false;
        }
        return conviction >= CONVICTION_THRESHOLD
                && worstEnemy.getConviction() - received <= 0;
    }
}