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
    Direction dirToDestination=rc.getLocation().directionTo(goal);
    int i=3;
    while(!rc.canMove(dirToDestination)&&i>0){
      if(rc.getRoundNum()%2==0){//roundnum serves as rng (hopefully)
        dirToDestination=dirToDestination.rotateLeft();
      }else{
        dirToDestination=dirToDestination.rotateRight();
      }
      --i; //i makes sure it doesn't infinite loop
    }
    return dirToDestination;
  }
}
