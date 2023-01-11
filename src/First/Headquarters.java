package First;

import battlecode.common.*;
import java.util.Random;
public class Headquarters extends Robot {
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
    if(first_turn){
      updatePersonalMapFull(rc);
      first_turn=false;
    }

    // Pick a direction to build in.
    Direction dir = directions[rng.nextInt(directions.length)];
    MapLocation newLoc = rc.getLocation().add(dir);
    // Let's try to build a carrier.
    rc.setIndicatorString("Trying to build a carrier");
    if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
        rc.buildRobot(RobotType.CARRIER, newLoc);
    }
    for(int i=0;i<100;++i){
      MapLocation m=new MapLocation(i,i);
    }
  }
}