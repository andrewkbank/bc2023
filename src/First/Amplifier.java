package First;

import battlecode.common.*;

public class Amplifier extends Robot {
  private MapLocation g=null;
  public Amplifier(RobotController rc) throws GameActionException{
    super(rc);
  }
  public void run(RobotController rc) throws GameActionException {
    while (g == null||rc.getLocation().equals(g)) {
      g=getRandomLoc(rc);
    }
    //read and write to shared array
    int bytecodeBefore=Clock.getBytecodeNum();
    readSharedArray(rc);
    int readArrayBytecode=Clock.getBytecodeNum();
    writeSharedArray(rc);
    int writeArrayBytecode=Clock.getBytecodeNum();
    Direction d=pathfind(rc,g);
    int pathfindBytecode=Clock.getBytecodeNum();
    if(rc.canMove(d)){
      rc.move(d);
    }else{
      g=getRandomLoc(rc);
    }
    int moveBytecode=Clock.getBytecodeNum();
    updatePersonalMap(rc);
    rc.setIndicatorString("read: "+(readArrayBytecode-bytecodeBefore)+" write: "+(writeArrayBytecode-readArrayBytecode)+" pathfind: "+(pathfindBytecode-writeArrayBytecode)+" map: "+(Clock.getBytecodeNum()-moveBytecode));
  }
}
