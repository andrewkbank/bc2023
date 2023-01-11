package First;

import battlecode.common.*;
import java.util.Random;
public class Headquarters extends Robot {

  // Keeps track of how many we have made
  private static int num_Carriers = 0;
  private static int num_Launcehrs = 0;

  //stuff I stole from examplefuncsplayer
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
  static final Random rng = new Random(6147);
  private static boolean first_turn;
  public Headquarters(RobotController rc) throws GameActionException{
    super(rc);
    first_turn = true;
  }
  public void run(RobotController rc) throws GameActionException {
    //todo: anchors
    if(first_turn){//on the first turn look at the entire surrounding
      updatePersonalMapFull(rc);
      first_turn=false;
    }

    // Pick a direction to build in.
    Direction dir = directions[rng.nextInt(directions.length)];
    MapLocation newLoc = rc.getLocation().add(dir);
    // Let's try to build a carrier.
    rc.setIndicatorString("Trying to build a carrier");
    if (rc.canBuildRobot(RobotType.CARRIER, newLoc) && (num_Carriers < 5 || num_Carriers < 1.5*num_Launcehrs)) {
      rc.buildRobot(RobotType.CARRIER, newLoc);
    }else if(rc.canBuildRobot(RobotType.LAUNCHER,newLoc)){
      rc.buildRobot(RobotType.LAUNCHER, newLoc);
    }
  }
}