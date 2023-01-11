package First;

import javax.lang.model.util.ElementScanner6;
import java.util.LinkedList;
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
  /*  map format
   *  0 = unknown
   *  1 = tempest (unpassable)
   *  2 = no current
   *  3 = current north
   *  4 = current northeast
   *  5 = current east
   *  6 = current southeast
   *  7 = current south
   *  8 = current southwest
   *  9 = current west
   *  10 = current northwest
   */
  protected int[] map;
  private MapLocation loc;
  protected RobotInfo hqInfo;
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
  public Robot(RobotController rc) throws GameActionException{
    //initialize map on robot creation
    map=new int[rc.getMapHeight()*rc.getMapWidth()];
    loc=rc.getLocation();
    RobotInfo[] robots = rc.senseNearbyRobots();
    if (robots.length == 0) {
      hqInfo = rc.senseRobot(rc.getID());
    } else {
      hqInfo = robots[0];
      for (RobotInfo r : robots) {
        if (r.type == RobotType.HEADQUARTERS) { hqInfo = r; break; }
      }
    }
  }

  // Every subclass must define their own run function.
  public abstract void run(RobotController rc) throws GameActionException;

  public Direction makeDir(int dx, int dy) {
    if (dx == -1) {
      if (dy == -1)   { return Direction.SOUTHWEST; }
      if (dy == 0)    { return Direction.WEST; }
      if (dy == 1)    { return Direction.NORTHWEST; }
    } else if (dx == 0) {
      if (dy == -1)   { return Direction.SOUTH; }
      if (dy == 0)    { return Direction.CENTER; }
      if (dy == 1)    { return Direction.NORTH; }
    } else if (dx == 1) {
      if (dy == -1)   { return Direction.SOUTHEAST; }
      if (dy == 0)    { return Direction.EAST; }
      if (dy == 1)    { return Direction.NORTHEAST; }
    }
    return Direction.CENTER;
  }

  public WellInfo getClosest(RobotController rc, WellInfo[] wells) throws GameActionException {
    assert(wells.length > 0);
    WellInfo closest = wells[0];
    for (int i = 1; i < wells.length; i++) {
      if (wells[i].getMapLocation().distanceSquaredTo(rc.getLocation()) < closest.getMapLocation().distanceSquaredTo(rc.getLocation())) {
        closest = wells[i];
      }
    }
    return closest;
  }

  //dumbass version
  /*
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
  */
  //dfs
  public Direction pathfind(RobotController rc, MapLocation goal) throws GameActionException{
    LinkedList<MapLocation> stack=new LinkedList<MapLocation>();
    int[] discovered=new int[rc.getMapHeight()*rc.getMapWidth()];
    stack.push(rc.getLocation());
    while(!stack.isEmpty()){  //while loop that runs dfs
      MapLocation m=stack.pop();
      if(discovered[m.y*rc.getMapWidth()+m.x]==0){
        discovered[m.y*rc.getMapWidth()+m.x]=1;
        Direction dirToGoal=rc.getLocation().directionTo(m);
        Direction[] pushOrder={//this is the priority order that we add stuff to the dfs queue
          dirToGoal,
          dirToGoal.rotateRight(),
          dirToGoal.rotateLeft(),
          dirToGoal.rotateRight().rotateRight(),
          dirToGoal.rotateLeft().rotateLeft(),
          dirToGoal.rotateRight().rotateRight().rotateRight(),
          dirToGoal.rotateLeft().rotateLeft().rotateLeft(),
          dirToGoal.opposite(),

        };
        for(int i=0;i<8;++i){//go through the priority order
          MapLocation toPush=m.add(pushOrder[i]);
          if(toPush.x>=0&&toPush.y>=0&&toPush.x<rc.getMapWidth()&&toPush.y<rc.getMapHeight()){//inbounds
            if(map[toPush.y*rc.getMapWidth()+toPush.x]!=1){//passable
              if(toPush==goal){ //break if you find the destination
                return pushOrder[i].opposite();
              }
              stack.push(m.add(pushOrder[i])); //and add them all to the stack
            }
          }
        }
      }
    }
    //backup (in case dfs doesn't work)
    return rc.getLocation().directionTo(goal);

  }
  //returns a list of MapLocations that make up the edge of the robot's vision (assumes 20 vision)
  private MapLocation[] getEdgeMapLocations(RobotController rc,Direction dir) throws GameActionException{
    Direction twoLeft=dir.rotateLeft().rotateLeft();
    Direction twoRight=dir.rotateRight().rotateRight();
    Direction threeLeft=dir.rotateLeft().rotateLeft().rotateLeft();
    Direction threeRight=dir.rotateRight().rotateRight().rotateRight();
    MapLocation center=rc.getLocation();
    if(dir.dx*dir.dx+dir.dy*dir.dy==2){//diagonal
      center=center.add(dir).add(dir).add(dir);
      MapLocation[] edge=new MapLocation[11];
      edge[0]=center;
      edge[1]=center.add(threeLeft);
      edge[2]=center.add(threeRight);
      edge[3]=edge[1].add(twoLeft);
      edge[4]=edge[2].add(twoRight);
      edge[5]=edge[3].add(threeLeft);
      edge[6]=edge[4].add(threeRight);
      edge[7]=edge[5].add(threeLeft);
      edge[8]=edge[6].add(threeRight);
      edge[9]=edge[7].add(threeLeft);
      edge[10]=edge[8].add(threeRight);
      return edge;
    }else{//cardinal
      center=center.add(dir).add(dir).add(dir).add(dir);
      MapLocation[] edge=new MapLocation[9];
      edge[0]=center;
      edge[1]=center.add(twoLeft);
      edge[2]=center.add(twoRight);
      edge[3]=edge[1].add(twoLeft);
      edge[4]=edge[2].add(twoRight);
      edge[5]=edge[3].add(threeLeft);
      edge[6]=edge[4].add(threeRight);
      edge[7]=edge[5].add(threeLeft);
      edge[8]=edge[6].add(threeRight);
      return edge;
    }
  }

  //careful, this crashes the client (overflows the heap)
  public void printMap(RobotController rc) throws GameActionException{
    rc.setIndicatorString("printing...");
    for(int i=0;i<rc.getMapWidth();++i){
      for(int j=0;j<rc.getMapHeight();++j){
        MapLocation m=new MapLocation(i,j);
        switch(map[i*rc.getMapWidth()+j]){
          case 0: rc.setIndicatorDot(m,0,0,100); break;
          case 1: rc.setIndicatorDot(m,100,0,0); break;
          case 2: rc.setIndicatorDot(m,0,100,0); break;
          case 3: rc.setIndicatorDot(m,0,100,0); break;
          case 4: rc.setIndicatorDot(m,0,100,0); break;
          case 5: rc.setIndicatorDot(m,0,100,0); break;
          case 6: rc.setIndicatorDot(m,0,100,0); break;
          case 7: rc.setIndicatorDot(m,0,100,0); break;
          case 8: rc.setIndicatorDot(m,0,100,0); break;
          case 9: rc.setIndicatorDot(m,0,100,0); break;
          case 10: rc.setIndicatorDot(m,0,100,0); break;
        }
      }
    }
    rc.setIndicatorString("done printing");
  }
  
  //each robot has its own personal map. This updates it every turn (~1000 bytecode)
  public void updatePersonalMap(RobotController rc) throws GameActionException{
    MapLocation newLocation=rc.getLocation();
    Direction dirMoved=loc.directionTo(newLocation);
    //rc.setIndicatorString("@Robot.java->updatePersonalMap moved: "+dirMoved);
    if(dirMoved!=Direction.CENTER){
      MapLocation[] visibleLocations=getEdgeMapLocations(rc, dirMoved);
      for(int i=0;i<visibleLocations.length;++i){ //go through all visible locations
        if(rc.canSenseLocation(visibleLocations[i])&&rc.onTheMap(visibleLocations[i])){ //filter out ones not on the map
          rc.setIndicatorDot(visibleLocations[i],0,0,100);
          int setMap=0;
          if(!rc.sensePassability(visibleLocations[i])){ //tempests (impassable)
            setMap=1;
          }else{ //empty squares or currents
            setMap=2;
            MapInfo currentInfo=rc.senseMapInfo(visibleLocations[i]);
            Direction current=currentInfo.getCurrentDirection();
            //switch-case every direction
            switch(current){
              case NORTH: setMap=3; break;
              case NORTHEAST: setMap=4; break;
              case EAST: setMap=5; break;
              case SOUTHEAST: setMap=6; break;
              case SOUTH: setMap=7; break;
              case SOUTHWEST: setMap=8; break;
              case WEST: setMap=9; break;
              case NORTHWEST: setMap=10; break;
            }
          }
          map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=setMap;
        }
      }
    }
    loc=rc.getLocation();
  }

  //takes up tons of bytecode
  //only meant to be used by Headquarters on turn 1
  public void updatePersonalMapFull(RobotController rc) throws GameActionException{
    MapLocation[] visibleLocations=rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),rc.getType().visionRadiusSquared);
    for(int i=0;i<visibleLocations.length;++i){ //go through all visible locations
      if(rc.canSenseLocation(visibleLocations[i])&&rc.onTheMap(visibleLocations[i])){ //filter out ones not on the map
        rc.setIndicatorDot(visibleLocations[i],0,0,100);
        int setMap=0;
        if(!rc.sensePassability(visibleLocations[i])){ //tempests (impassable)
          setMap=1;
        }else{ //empty squares or currents
          setMap=2;
          MapInfo currentInfo=rc.senseMapInfo(visibleLocations[i]);
          Direction current=currentInfo.getCurrentDirection();
          //switch-case every direction
          switch(current){
            case NORTH: setMap=3; break;
            case NORTHEAST: setMap=4; break;
            case EAST: setMap=5; break;
            case SOUTHEAST: setMap=6; break;
            case SOUTH: setMap=7; break;
            case SOUTHWEST: setMap=8; break;
            case WEST: setMap=9; break;
            case NORTHWEST: setMap=10; break;
          }
        }
        map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=setMap;
      }
    }
  }
  //todo: the map is stored in the shared array, this pushes updates to it
  public void updateGlobalMap(RobotController rc) throws GameActionException{

  }
}
