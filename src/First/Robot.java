package First;

import java.util.Random;
import javax.lang.model.util.ElementScanner6;
import java.util.LinkedList;
import java.util.ListIterator;
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
  //constants
  public final static int ISLANDSTORAGELENGTH=10;
  public final static int IMPASSABLESTORAGELENGTH=20;
  public final static int WELLSTORAGELENGTH=2;

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
   *  11 = adamantium well
   *  12 = mana well
   *  13 = elixir well
   *  14 = unclaimed island
   *  15 = our island
   *  16 = other team's island
   */
  protected int[] map;
  //these queues are the new info the bot has found and needs to upload to the shared array
  protected String islandQueueTentative="";
  protected String impassableQueueTentative="";
  protected String wellQueueTentative="";
  protected String islandQueue="";
  protected String impassableQueue="";
  protected String wellQueue="";

  private MapLocation loc;
  
  protected RobotInfo hqInfo;
  static final Direction[] directions = {
    Direction.SOUTHWEST,
    Direction.SOUTH,
    Direction.SOUTHEAST,
    Direction.WEST,
    Direction.CENTER,
    Direction.EAST,
    Direction.NORTHWEST,
    Direction.NORTH,
    Direction.NORTHEAST,
  };
  protected static Random rng;
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
    rng=new Random(rc.getRoundNum());
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
  //dfs
  public Direction pathfind(RobotController rc, MapLocation goal) throws GameActionException{
    if(goal.equals(rc.getLocation())){return Direction.CENTER;}
    //rc.setIndicatorString("DFS to "+goal);
    int bytecodeBefore=Clock.getBytecodeNum();
    //stack starts with the goal
    String stack=""+(char)(goal.y*rc.getMapWidth()+goal.x);
    boolean[] discovered=new boolean[rc.getMapHeight()*rc.getMapWidth()];
    while(!stack.isEmpty()&&Clock.getBytecodesLeft()>2000){  //while loop that runs dfs
      int c=(int)(stack.charAt(0));
      stack=stack.substring(1);
      if(!discovered[c]){
        //rc.setIndicatorDot(new MapLocation(c%rc.getMapWidth(),c/rc.getMapWidth()),Clock.getBytecodeNum()/50,0,0);
        discovered[c]=true;
        Direction dirToGoal=(new MapLocation(c%rc.getMapWidth(),c/rc.getMapWidth())).directionTo(rc.getLocation());
        //System.out.println("")
        Direction[] pushOrder={//this is the priority order that we add stuff to the dfs queue
          dirToGoal.opposite(),
          dirToGoal.rotateLeft().rotateLeft().rotateLeft(),
          dirToGoal.rotateRight().rotateRight().rotateRight(),
          dirToGoal.rotateLeft().rotateLeft(),
          dirToGoal.rotateRight().rotateRight(),
          dirToGoal.rotateLeft(),
          dirToGoal.rotateRight(),
          dirToGoal
        };
        //bytecodeBefore=Clock.getBytecodeNum();
        for(int i=0;i<8;++i){//go through the priority order, 52 bytecode total
          int toPush=c+rc.getMapWidth()*pushOrder[i].dy+pushOrder[i].dx;  //16 bytecode
          //note that by removing "onesDigit", many locations out of bounds in the x direction map to spots on the map. Hopefully this won't fuck everything up.
          //int onesDigit=c%mapWidth+pushOrder[i].dx; //11 bytecode
          if(toPush>0&&toPush<(rc.getMapWidth()*rc.getMapHeight())&&map[toPush]!=1/*&&onesDigit>0&&onesDigit<10*/){  //16 bytecode (used to be 21)
            //inbounds and passable
            if(toPush==rc.getLocation().y*rc.getMapWidth()+rc.getLocation().x){ //13 bytecode
              //rc.setIndicatorString("DFS to "+goal+" success, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
              //Direction[] toReturn={pushOrder[i].opposite(),directions[(int)path]};
              return pushOrder[i].opposite();
            }
            stack=(char)toPush+stack; //10 bytecode
            //old: stack2+=directionToChar(pushOrder[i]);  //24 bytecode for the pushes
          }
        }
        //rc.setIndicatorString("for bytecode: "+(Clock.getBytecodeNum()-bytecodeBefore));
      }
      //bytecodeBefore=Clock.getBytecodeNum();
    }
    //rc.setIndicatorString("DFS to "+goal+" failed, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
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

    if(dirMoved!=Direction.CENTER){ //only need to update the personal map if we moved
      MapLocation[] visibleLocations=getEdgeMapLocations(rc, dirMoved);
      for(int i=0;i<visibleLocations.length;++i){ //go through all visible locations
        if(rc.canSenseLocation(visibleLocations[i])&&rc.onTheMap(visibleLocations[i])){ //filter out ones not on the map
          rc.setIndicatorDot(visibleLocations[i],0,0,100);
          if(!rc.sensePassability(visibleLocations[i])){ //tempests (impassable)
            if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=1){
              map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=1;
              impassableQueueTentative+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x);
            }
          }else if(rc.senseIsland(visibleLocations[i])!=-1){ //island
            Team islandTeam=rc.senseTeamOccupyingIsland(rc.senseIsland(visibleLocations[i]));
            if(islandTeam==Team.NEUTRAL){//neutral island
              if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=14){
                map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=14;
                islandQueueTentative+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x);
              }
            }else if (islandTeam==rc.getTeam()){//our island
              if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=15){
                map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=15;
                islandQueueTentative+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x+4096);
              }
            }else{//their island
              if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=16){
                map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=16;
                islandQueueTentative+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x+4096*2);
              }
            }
          }else{ //empty squares or currents
            int setMap=2;
            MapInfo currentInfo=rc.senseMapInfo(visibleLocations[i]);
            Direction current=currentInfo.getCurrentDirection();
            //switch-case every direction
            switch(current){
              case CENTER: setMap=2; break;
              case NORTH: setMap=3; break;
              case NORTHEAST: setMap=4; break;
              case EAST: setMap=5; break;
              case SOUTHEAST: setMap=6; break;
              case SOUTH: setMap=7; break;
              case SOUTHWEST: setMap=8; break;
              case WEST: setMap=9; break;
              case NORTHWEST: setMap=10; break;
            }
            map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=setMap;
          }
        }
      }
      WellInfo[] wellsInRange=rc.senseNearbyWells();
      for(int i=0;i<wellsInRange.length;++i){
        int setMap=0;
        switch(wellsInRange[i].getResourceType()){
          case ADAMANTIUM: setMap=11; break;
          case MANA: setMap=12; break;
          case ELIXIR: setMap=13; break;
          case NO_RESOURCE: setMap=2; break;
        }
        if(map[wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x]!=setMap){
          map[wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x]=setMap;
          wellQueueTentative+=(char)(wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x+4096*(setMap-11));
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
        if(!rc.sensePassability(visibleLocations[i])){ //tempests (impassable)
          if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=1){
            map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=1;
            impassableQueue+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x);
          }
        }else if(rc.senseIsland(visibleLocations[i])!=-1){ //island
          Team islandTeam=rc.senseTeamOccupyingIsland(rc.senseIsland(visibleLocations[i]));
          if(islandTeam==Team.NEUTRAL){//neutral island
            if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=14){
              map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=14;
              islandQueue+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x);
            }
          }else if (islandTeam==rc.getTeam()){//our island
            if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=15){
              map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=15;
              islandQueue+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x+4096);
            }
          }else{//their island
            if(map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]!=16){
              map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=16;
              islandQueue+=(char)(visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x+4096*2);
            }
          }
        }else{ //empty squares or currents
          int setMap=2;
          MapInfo currentInfo=rc.senseMapInfo(visibleLocations[i]);
          Direction current=currentInfo.getCurrentDirection();
          //switch-case every direction
          switch(current){
            case CENTER: setMap=2; break;
            case NORTH: setMap=3; break;
            case NORTHEAST: setMap=4; break;
            case EAST: setMap=5; break;
            case SOUTHEAST: setMap=6; break;
            case SOUTH: setMap=7; break;
            case SOUTHWEST: setMap=8; break;
            case WEST: setMap=9; break;
            case NORTHWEST: setMap=10; break;
          }
          map[visibleLocations[i].y*rc.getMapWidth()+visibleLocations[i].x]=setMap;
        }
      }
    }
    WellInfo[] wellsInRange=rc.senseNearbyWells();
    for(int i=0;i<wellsInRange.length;++i){
      int setMap=0;
      switch(wellsInRange[i].getResourceType()){
        case ADAMANTIUM: setMap=11; break;
        case MANA: setMap=12; break;
        case ELIXIR: setMap=13; break;
        case NO_RESOURCE: setMap=2; break;
      }
      if(map[wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x]!=setMap){
        map[wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x]=setMap;
        wellQueue+=(char)(wellsInRange[i].getMapLocation().y*rc.getMapWidth()+wellsInRange[i].getMapLocation().x+4096*(setMap-11));
      }
    }
  }
  
  //reads info from shared array and updates personal map
  public void readSharedArray(RobotController rc) throws GameActionException{
    //todo: add ability to ignore data we already know (maybe impossible)
    int data;
    for(int i=0;i<ISLANDSTORAGELENGTH;++i){ //go through every island storage slot
      data=rc.readSharedArray(i); //read shared array
      if(data==0){ //if there aren't any contents in the slot...
        break;
      }
      //store data from shared array into personal map
      int loc=data%4096;
      int team=(data/4096)%4;
      map[loc]=14+team;
      //removes data found in the array from the upload queue
      int indexOf=islandQueueTentative.indexOf((char)data);
      if(indexOf>=0){
        //islandQueue.removeFirstOccurrence(data);
        islandQueueTentative=islandQueueTentative.substring(0,indexOf)+islandQueueTentative.substring(indexOf+1);
      }
    }
    for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){ //go through every island storage slot
      data=rc.readSharedArray(i+ISLANDSTORAGELENGTH); //read shared array
      if(data==0){ //if there aren't any contents in the slot...
        break;
      }
      //store data from shared array into personal map
      int loc=data%4096;  //just for safety (we should just be able to put map[data] next line)
      map[loc]=1;
      //removes data found in the array from the upload queue
      int indexOf=impassableQueueTentative.indexOf((char)data);
      if(indexOf>=0){
        //impassableQueue.removeFirstOccurrence(data);
        impassableQueueTentative=impassableQueueTentative.substring(0,indexOf)+impassableQueueTentative.substring(indexOf+1);
      }
    }
    for(int i=0;i<WELLSTORAGELENGTH;++i){ //go through every island storage slot
      data=rc.readSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH); //read shared array
      if(data==0){ //if there aren't any contents in the slot...
        break;
      }
      //store data from shared array into personal map
      int loc=data%4096;
      int wellType=(data/4096)/4;
      map[loc]=11+wellType;
      //removes data found in the array from the upload queue
      int indexOf=wellQueueTentative.indexOf((char)data);
      if(indexOf>=0){
        //wellQueue.removeFirstOccurrence(data);
        wellQueue=wellQueueTentative.substring(0,indexOf)+wellQueueTentative.substring(indexOf+1);
      }
    }
  }

  //writes info from the queues to the array (if possible)
  public void writeSharedArray(RobotController rc) throws GameActionException{
    boolean decrement=false;
    boolean canWrite=rc.canWriteSharedArray(0,0);
    for(int i=0;i<ISLANDSTORAGELENGTH;++i){ //go through every island storage slot
      if(rc.readSharedArray(i)==0){ //if there aren't any contents in the slot...
        if(canWrite&&!islandQueue.isEmpty()){ //check if we can write
          rc.writeSharedArray(i,(int)(islandQueue.charAt(0))); //we write our own contents
          islandQueue=islandQueue.substring(1); //and remove it from the queue
        }
        decrement=true;
      }
    }
    if(decrement){
      islandQueue+=islandQueueTentative;
      islandQueueTentative="";
    }
    decrement=false;
    for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){ //go through every impassable storage slot
      if(rc.readSharedArray(i+ISLANDSTORAGELENGTH)==0){ //if there aren't any contents in the slot...
        if(canWrite&&!impassableQueue.isEmpty()){ //check if we can write
          rc.writeSharedArray(i+ISLANDSTORAGELENGTH,(int)(impassableQueue.charAt(0))); //we write our own contents
          impassableQueue=impassableQueue.substring(1); //(and remove it from the queue)
        }
        decrement=true;
      }
    }
    if(decrement){
      impassableQueue+=impassableQueueTentative;
      impassableQueueTentative="";
    }
    decrement=false;
    for(int i=0;i<WELLSTORAGELENGTH;++i){ //go through every well storage slot
      if(rc.readSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH)==0){ //if there aren't any contents in the slot...
        if(canWrite&&!wellQueue.isEmpty()){ //check if we can write
          rc.writeSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH,(int)(wellQueue.charAt(0))); //we write our own contents
          wellQueue=wellQueue.substring(1); //(and remove it from the queue)
        }
        decrement=true;
      }
    }
    if(decrement){
      wellQueue+=wellQueueTentative;
      wellQueueTentative="";
    }
  }

  public void moveRandom(RobotController rc) throws GameActionException {
    Direction direction = directions[rng.nextInt(directions.length)];

    if (rc.canMove(direction)) {
      rc.move(direction);
    }
    else if (rc.canMove(direction.rotateRight())) {
      rc.move(direction.rotateRight());
    }
    else if (rc.canMove(direction.rotateLeft())) {
      rc.move(direction.rotateLeft());
    }
    else if (rc.canMove(direction.opposite())) {
      rc.move(direction.opposite());
    }
    else {
      if (rc.canMove(Direction.CENTER)) {
        rc.move(Direction.CENTER);
      }
    }
  }
  public MapLocation getRandomLoc(RobotController rc) throws GameActionException{
    return new MapLocation(rng.nextInt(rc.getMapWidth()),rng.nextInt(rc.getMapHeight()));
  }
}
