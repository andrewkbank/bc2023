package First;

import battlecode.common.*;

public class Headquarters extends Robot {
  public void run(RobotController rc) throws GameActionException {
    //todo: anchors
    // Pick a direction to build in.
    Direction dir = directions[rng.nextInt(directions.length)];
    MapLocation newLoc = rc.getLocation().add(dir);
    // Let's try to build a carrier.
    rc.setIndicatorString("Trying to build a carrier");
    if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
        rc.buildRobot(RobotType.CARRIER, newLoc);
    }
  }
}