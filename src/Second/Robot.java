package Second;

import java.util.Random;
import java.util.LinkedList;

import battlecode.common.*;

public abstract class Robot {

  // MEMBER VARIABLES ==========================================================

  // ROBOT MEMORY
  protected static Map map;                         // Information about map
  protected static RobotInfo hqInfo;                // HQ closest at spawn
  protected static MapLocation lastLoc;             // Location before last move
  protected static Random rng;                      // Randomness

  // COMMUNICATION UPLOAD QUEUES
  protected static LinkedList<Integer> islandQueue;
  protected static LinkedList<Integer> impassableQueue;
  protected static LinkedList<Integer> wellQueue;

  // STORAGE VALUES FOR EXPENSIVE METHODS
  private static RobotInfo[] nearbyRobots;          // senseNearbyRobots()
  private static int[] nearbyIslandIDs;             // senseNearbyIslands() 
  private static MapLocation[] nearbyIslandLocs;    // senseNearbyIslandLocations(int idx)
  private static WellInfo[] nearbyWells;            // senseNearbyWells()
  private static MapInfo[] nearbyMapInfos;          // senseNearbyMapInfos()

  // USEFUL CONSTANTS
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

  // CONSTRUCTOR / RUN =========================================================

  public Robot(RobotController rc) throws GameActionException {
    lastLoc = rc.getLocation();
    rng = new Random(69);
    // Initialize map on robot creation
    map = new Map(rc.getMapWidth(), rc.getMapHeight());
    // Initialize HQ Info
    hqInfo = rc.senseRobot(rc.getID());
    if (rc.getType() != RobotType.HEADQUARTERS) {
      RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
      for (int i = 0; i < robots.length; ++i) {
        if (robots[i].type == RobotType.HEADQUARTERS) { hqInfo = robots[i]; break; }
      }
    }
    // Initialize Commmunication upload queues
    islandQueue = new LinkedList<Integer>();
    impassableQueue = new LinkedList<Integer>();
    wellQueue = new LinkedList<Integer>();
  }

  // Evert subclass must implement their own run function
  protected abstract void run(RobotController rc) throws GameActionException;

  protected void run_super(RobotController rc) throws GameActionException {
    nearbyRobots = null;
    nearbyIslandIDs = null;
    nearbyIslandLocs = null;
    nearbyWells = null;
    nearbyMapInfos = null;
    run(rc);
  }

  // REDEFINITIONS =============================================================

  // These functions avoid recalculating values for the most expensive functions
  // USE THESE IN PLACE OF THE RC VERSIONS, WILL SAVE 100-200 BYTECODE PER CALL
  protected RobotInfo[] my_senseNearbyRobots(RobotController rc) throws GameActionException {
    if (nearbyRobots == null) { nearbyRobots = rc.senseNearbyRobots(); }
    return nearbyRobots;
  }
  protected int[] my_senseNearbyIslands(RobotController rc) throws GameActionException {
    if (nearbyIslandIDs == null) { nearbyIslandIDs = rc.senseNearbyIslands(); }
    return nearbyIslandIDs;
  }
  protected MapLocation[] my_senseNearbyIslandLocations(RobotController rc, int idx) throws GameActionException{
    if (nearbyIslandLocs == null) { nearbyIslandLocs = rc.senseNearbyIslandLocations(idx); }
    return nearbyIslandLocs;
  }
  protected WellInfo[] my_senseNearbyWells(RobotController rc) throws GameActionException {
    if (nearbyWells == null) { nearbyWells = rc.senseNearbyWells(); }
    return nearbyWells;
  }
  protected MapInfo[] my_senseNearbyMapInfos(RobotController rc) throws GameActionException {
    if (nearbyMapInfos == null) { nearbyMapInfos = rc.senseNearbyMapInfos(); }
    return nearbyMapInfos;
  }

  // USE IN PLACE OF RC VERSION, OTHERWISE LAST LOCATION WILL NOT BE UPDATED
  protected void my_move(RobotController rc, Direction dir) throws GameActionException {
    lastLoc = rc.getLocation();
    rc.move(dir);
  }

  // OTHER METHODS =============================================================

  // Creates direction from any two ints dx and dy (Dont have to be -1 to 1)
  protected Direction makeDir(int dx, int dy) {
    return new MapLocation(0, 0).directionTo(new MapLocation(dx, dy));
  }

  // Returns the closest of a given list of things
  protected MapInfo getClosest(RobotController rc, MapInfo[] infos) throws GameActionException, IllegalArgumentException {
    if (infos.length == 0) { throw new IllegalArgumentException("Array of size 0 passed"); }
    MapInfo closest = infos[0];
    for (int i = 0; i < infos.length; i++) {
      if (infos[i].getMapLocation().distanceSquaredTo(rc.getLocation()) < closest.getMapLocation().distanceSquaredTo(rc.getLocation())) {
        closest = infos[0];
      }
    }
    return closest;
  }
  protected MapLocation getClosest(RobotController rc, MapLocation[] locs) throws GameActionException, IllegalArgumentException {
    if (locs.length == 0) { throw new IllegalArgumentException("Array of size 0 passed"); }
    MapLocation closest = locs[0];
    for (int i = 0; i < locs.length; i++) {
      if (locs[i].distanceSquaredTo(rc.getLocation()) < closest.distanceSquaredTo(rc.getLocation())) {
        closest = locs[0];
      }
    }
    return closest;
  }
  protected RobotInfo getClosest(RobotController rc, RobotInfo[] infos) throws GameActionException, IllegalArgumentException {
    if (infos.length == 0) { throw new IllegalArgumentException("Array of size 0 passed"); }
    RobotInfo closest = infos[0];
    for (int i = 0; i < infos.length; i++) {
      if (infos[i].getLocation().distanceSquaredTo(rc.getLocation()) < closest.getLocation().distanceSquaredTo(rc.getLocation())) {
        closest = infos[0];
      }
    }
    return closest;
  }
  protected WellInfo getClosest(RobotController rc, WellInfo[] infos) throws GameActionException, IllegalArgumentException {
    if (infos.length == 0) { throw new IllegalArgumentException("Array of size 0 passed"); }
    WellInfo closest = infos[0];
    for (int i = 0; i < infos.length; i++) {
      if (infos[i].getMapLocation().distanceSquaredTo(rc.getLocation()) < closest.getMapLocation().distanceSquaredTo(rc.getLocation())) {
        closest = infos[0];
      }
    }
    return closest;
  }

  // TODO: OPTIMIZE AND CLEAN UP, DIRECTLY COPY PASTED
  // Pathfinding (dfs)
  protected Direction pathfind(RobotController rc, MapLocation goal) throws GameActionException{
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
            /*
             * THIS NEXT LINE IS THE ONLY ONE I CHANGED WHEN MOVING, BECAUSE OLD
             * LINE DIDNT WORK WITH NEW MAP OBJECT IN ROBOT CLASS
             * 
             * Old line: if(map[toPush.y*rc.getMapWidth()+toPush.x]!=1){//passable
             */
            if(map.readData(toPush, Map.TEMPEST) == 0){//passable
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
  
  // TODO: OPTIMIZE AND CLEAN UP, DIRECTLY COPY PASTED
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

  //each robot has its own personal map. This updates it every turn (~1000 bytecode)
  /*
   * REWRITTEN BY JUSTIN, NO LONGER PREPARES INFO FOR COMM ARRAY, BUT ILL FIX IT
   */
  protected void updatePersonalMap(RobotController rc) throws GameActionException{
    if (lastLoc == rc.getLocation()) { return; }
    MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), -1);
    for (int i = 0; i < locs.length; ++i) {
      if (!rc.onTheMap(locs[i]) || !rc.canSenseLocation(lastLoc)) { continue; }
      map.writeData(locs[i], Map.SET_KNOWN);
      MapInfo info = rc.senseMapInfo(locs[i]);
      if (!info.isPassable()) { map.writeData(locs[i], Map.TEMPEST); }
      if (rc.senseIsland(lastLoc) != -1) {
        Team islandTeam = rc.senseTeamOccupyingIsland(rc.senseIsland(locs[i]));
        if (islandTeam == Team.NEUTRAL) {  map.writeData(locs[i], Map.SET_UNCLAIMED_ISLAND); }
        else if (islandTeam == rc.getTeam()) { map.writeData(locs[i], Map.SET_OUR_ISLAND); }
        else { map.writeData(locs[i], Map.SET_OTHER_ISLAND); }
      }
      switch (info.getCurrentDirection()) {
        case NORTH:       map.writeData(locs[i], Map.SET_CURRENT_N);    break;
        case NORTHEAST:   map.writeData(locs[i], Map.SET_CURRENT_NE);   break;
        case EAST:        map.writeData(locs[i], Map.SET_CURRENT_E);    break;
        case SOUTHEAST:   map.writeData(locs[i], Map.SET_CURRENT_SE);   break;
        case SOUTH:       map.writeData(locs[i], Map.SET_CURRENT_S);    break;
        case SOUTHWEST:   map.writeData(locs[i], Map.SET_CURRENT_SW);   break;
        case WEST:        map.writeData(locs[i], Map.SET_CURRENT_W);    break;
        case NORTHWEST:   map.writeData(locs[i], Map.SET_CURRENT_NW);   break;
        case CENTER:      map.unsetData(locs[i], Map.SET_CURRENT_NW);   break;
      }
    }
    WellInfo[] wells = my_senseNearbyWells(rc);
    for (int i = 0; i < wells.length; ++i) {
      switch (wells[i].getResourceType()) {
        case NO_RESOURCE: map.unsetData(wells[i].getMapLocation(), Map.WELL);         break;
        case ADAMANTIUM:  map.writeData(wells[i].getMapLocation(), Map.SET_AD_WELL);  break;
        case MANA:        map.writeData(wells[i].getMapLocation(), Map.SET_MN_WELL);  break;
        case ELIXIR:      map.writeData(wells[i].getMapLocation(), Map.SET_EX_WELL);  break;
      }
    }
  }

  //takes up tons of bytecode
  //only meant to be used by Headquarters on turn 1
  /*
   * REWRITTEN BY JUSTIN, NO LONGER PREPARES INFO FOR COMM ARRAY, BUT ILL FIX IT
   */
  protected void updatePersonalMapFull(RobotController rc) throws GameActionException{
    if (lastLoc == rc.getLocation()) { return; }
    MapLocation[] locs = getEdgeMapLocations(rc, lastLoc.directionTo(rc.getLocation()));
    for (int i = 0; i < locs.length; ++i) {
      if (!rc.onTheMap(locs[i]) || !rc.canSenseLocation(lastLoc)) { continue; }
      map.writeData(locs[i], Map.SET_KNOWN);
      MapInfo info = rc.senseMapInfo(locs[i]);
      if (!info.isPassable()) { map.writeData(locs[i], Map.TEMPEST); }
      if (rc.senseIsland(lastLoc) != -1) {
        Team islandTeam = rc.senseTeamOccupyingIsland(rc.senseIsland(locs[i]));
        if (islandTeam == Team.NEUTRAL) {  map.writeData(locs[i], Map.SET_UNCLAIMED_ISLAND); }
        else if (islandTeam == rc.getTeam()) { map.writeData(locs[i], Map.SET_OUR_ISLAND); }
        else { map.writeData(locs[i], Map.SET_OTHER_ISLAND); }
      }
      switch (info.getCurrentDirection()) {
        case NORTH:       map.writeData(locs[i], Map.SET_CURRENT_N);    break;
        case NORTHEAST:   map.writeData(locs[i], Map.SET_CURRENT_NE);   break;
        case EAST:        map.writeData(locs[i], Map.SET_CURRENT_E);    break;
        case SOUTHEAST:   map.writeData(locs[i], Map.SET_CURRENT_SE);   break;
        case SOUTH:       map.writeData(locs[i], Map.SET_CURRENT_S);    break;
        case SOUTHWEST:   map.writeData(locs[i], Map.SET_CURRENT_SW);   break;
        case WEST:        map.writeData(locs[i], Map.SET_CURRENT_W);    break;
        case NORTHWEST:   map.writeData(locs[i], Map.SET_CURRENT_NW);   break;
        case CENTER:      break;
      }
    }
    WellInfo[] wells = my_senseNearbyWells(rc);
    for (int i = 0; i < wells.length; ++i) {
      switch (wells[i].getResourceType()) {
        case NO_RESOURCE: break;
        case ADAMANTIUM:  map.writeData(wells[i].getMapLocation(), Map.SET_AD_WELL);  break;
        case MANA:        map.writeData(wells[i].getMapLocation(), Map.SET_MN_WELL);  break;
        case ELIXIR:      map.writeData(wells[i].getMapLocation(), Map.SET_EX_WELL);  break;
      }
    }
  }

  // Move in a random direction if possible, if not doesn't move
  protected void moveRandom(RobotController rc) throws GameActionException {
    Direction start = directions[rng.nextInt(directions.length)];
    Direction dir = start;
    do {
      if (rc.canMove(dir)) { my_move(rc, dir); return; }
      dir = dir.rotateRight();
    } while (dir != start);
  }
}
