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
    if(rc.isActionReady()){ //try building
      //gets priority queue of locations to build at (its 29 long)
      MapLocation[] actionLocations=getActionLocations(rc,null);
      for(int i=0;i<actionLocations.length;++i){
        rc.setIndicatorDot(actionLocations[i],100,0,0);
      }
      //tries building at each of those locations
      for(int i=0;i<actionLocations.length;++i){
        if (rc.canBuildRobot(RobotType.CARRIER, actionLocations[i]) && (num_Carriers < 5 || num_Carriers < 1.5*num_Launcehrs)) {
          rc.buildRobot(RobotType.CARRIER, actionLocations[i]);
          break;
        }else if(rc.canBuildRobot(RobotType.LAUNCHER,actionLocations[i])){
          rc.buildRobot(RobotType.LAUNCHER, actionLocations[i]);
          break;
        }
      }
    }
  }

  //returns all MapLocations that are within the radius (includes its own location cuz I'm lazy)
  //prioritize=null will do a random direction
  private MapLocation[] getActionLocations(RobotController rc, Direction prioritize) throws GameActionException{
    if(prioritize==null){
      prioritize=directions[rng.nextInt(directions.length)];
    }
    Direction left=prioritize.rotateLeft().rotateLeft();
    Direction right=prioritize.rotateRight().rotateRight();
    //start with the maplocation furthest away (radius 9)
    //it's pretty much a square (except for the distance 9 squares)
    MapLocation[] actionLocations=new MapLocation[29];
    if(prioritize.dx*prioritize.dx+prioritize.dy*prioritize.dy==2){ //diagonal
      actionLocations[0]=rc.getLocation().add(prioritize).add(prioritize);

      actionLocations[1]=actionLocations[0].add(left.rotateLeft());
      actionLocations[2]=actionLocations[1].add(right);
      actionLocations[3]=actionLocations[1].add(left);
      actionLocations[4]=actionLocations[2].add(right);

      actionLocations[5]=actionLocations[2].add(left.rotateLeft());
      actionLocations[6]=actionLocations[5].add(right);
      actionLocations[7]=actionLocations[5].add(left);

      actionLocations[8]=actionLocations[5].add(left.rotateLeft());
      actionLocations[9]=actionLocations[8].add(right);
      actionLocations[10]=actionLocations[8].add(left);
      actionLocations[11]=actionLocations[9].add(right);

      actionLocations[12]=rc.getLocation();
      actionLocations[13]=actionLocations[12].add(right);
      actionLocations[14]=actionLocations[12].add(left);
      actionLocations[15]=actionLocations[13].add(right);
      actionLocations[16]=actionLocations[14].add(left);

      actionLocations[17]=actionLocations[12].add(left.rotateLeft());
      actionLocations[18]=actionLocations[17].add(right);
      actionLocations[19]=actionLocations[17].add(left);
      actionLocations[20]=actionLocations[18].add(right);

      actionLocations[21]=actionLocations[18].add(left.rotateLeft());
      actionLocations[22]=actionLocations[21].add(right);
      actionLocations[23]=actionLocations[21].add(left);

      actionLocations[24]=actionLocations[21].add(left.rotateLeft());
      actionLocations[25]=actionLocations[24].add(right);
      actionLocations[26]=actionLocations[24].add(left);
      actionLocations[27]=actionLocations[25].add(right);

      actionLocations[28]=actionLocations[25].add(left.rotateLeft());

    }else{  //cardinal
      actionLocations[0]=rc.getLocation().add(prioritize).add(prioritize).add(prioritize);
      actionLocations[1]=actionLocations[0].add(prioritize.opposite());
      for(int i=0;i<5;++i){
        actionLocations[2+i*5]=actionLocations[1+i*5].add(left);
        actionLocations[3+i*5]=actionLocations[1+i*5].add(right);
        actionLocations[4+i*5]=actionLocations[2+i*5].add(left);
        actionLocations[5+i*5]=actionLocations[3+i*5].add(right);
        actionLocations[6+i*5]=actionLocations[1+i*5].add(prioritize.opposite());
      }
      actionLocations[27]=rc.getLocation().add(left).add(left).add(left);
      actionLocations[28]=rc.getLocation().add(right).add(right).add(right);
    }
    return actionLocations;
  }
}