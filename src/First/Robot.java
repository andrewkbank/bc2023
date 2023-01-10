package First;

import battlecode.common.*;

/*
 * Every robot type inherits from this base Robot class
 * 
 * Define all methods in this class, call them in the run function of the 
 * subclass it is being used in.
 * 
 * Basically, think of the run function as the "main" of each subclass.
 * for code reuse, all implementations should be here, only the run function
 * should be in the subclass
 */
public abstract class Robot {
  // Every subclass must define their own run function.
  public abstract void run(RobotController rc) throws GameActionException;

  public MapLocation getClosest(RobotController rc, MapLocation[] locs) throws GameActionException {
    assert(locs.length > 0);
    MapLocation closest = locs[0];
    for (int i = 1; i < locs.length; i++) {
      if (locs[i].distanceSquaredTo(rc.getLocation()) < closest.distanceSquaredTo(rc.getLocation())) {
        closest = locs[i];
      }
    }
    return closest;
  }

  public MapLocation getClosest(RobotController rc, WellInfo[] wells) throws GameActionException {
    assert(wells.length > 0);
    MapLocation closest = wells[0].getMapLocation();
    for (int i = 1; i < wells.length; i++) {
      if (wells[i].getMapLocation().distanceSquaredTo(rc.getLocation()) < closest.distanceSquaredTo(rc.getLocation())) {
        closest = wells[i].getMapLocation();
      }
    }
    return closest;
  }

  public Direction pathfind(RobotController rc, MapLocation goal) throws GameActionException {
    
  }
}
