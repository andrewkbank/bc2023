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
}
