package First;

import battlecode.common.*;

public class Launcher extends Robot {
  private MapLocation randomLoc=null;
  public Launcher(RobotController rc) throws GameActionException{
    super(rc);
    randomLoc=getRandomLoc(rc);
  }
  public void run(RobotController rc) throws GameActionException {
    attack(rc);

    Direction go=movement(rc);
    if(rc.canMove(go)){
      rc.move(go);
    } else if (rc.canMove(go.rotateRight())) {
      rc.move(go.rotateRight());
    } else if (rc.canMove(go.rotateLeft())) {
      rc.move(go.rotateLeft());
    } else if (rc.canMove(go.opposite())) {
      rc.move(go.opposite());
    }
    //todo: later, make it able to run-n-gun (not just gun-n-run)
  }
  //does the physical attacking
  public void attack(RobotController rc) throws GameActionException{
    int radius = rc.getType().actionRadiusSquared;
    Team opponent = rc.getTeam().opponent();
    RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
    //look for the launcher with the lowest health
    int enemyIndex=targetFire(rc,RobotType.LAUNCHER);
    if(enemyIndex>=0){//kill launcher
      MapLocation toAttack = enemies[enemyIndex].getLocation();
      if (rc.canAttack(toAttack)) {
          rc.setIndicatorString("Pew");        
          rc.attack(toAttack);
      }
    }else{//look for any other unit
      enemyIndex=targetFire(rc,null);
      if(enemyIndex>=0){//kill other unit
        MapLocation toAttack = enemies[enemyIndex].getLocation();
        if (rc.canAttack(toAttack)) {
            rc.setIndicatorString("Pew");        
            rc.attack(toAttack);
        }
      }
    }
  }
  //looks for the lowest health robot of the given type, and returns its position on the senseNearbyRobots queue
  //can call type=null to find any type
  private int targetFire(RobotController rc, RobotType type) throws GameActionException{
    int radius = rc.getType().actionRadiusSquared;
    Team opponent = rc.getTeam().opponent();
    RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
    if (enemies.length >= 0) {
      int attackIndex=-1;
      int enemyHealth=69; //this just so happens to be larger than any enemy health
      for(int i=0;i<enemies.length;++i){//finds lowest health launcher
        if(type==null){//no specified type to target (don't target headquarters)
          if(enemies[i].getType()!=RobotType.HEADQUARTERS&&enemies[i].getHealth()<enemyHealth){
            attackIndex=i;
            enemyHealth=enemies[i].getHealth();
          }
        }else if(enemies[i].getType()==type&&enemies[i].getHealth()<enemyHealth){//specified type to target
          attackIndex=i;
          enemyHealth=enemies[i].getHealth();
        }
      }
      return attackIndex;
    }
    return -1;
  }
  public Direction movement(RobotController rc) throws GameActionException{
    //basic movement: retreat if lower health, pursue if healthy
    int radius = rc.getType().actionRadiusSquared;
    Team opponent = rc.getTeam().opponent();
    RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
    if(enemies.length>0){//enemies in range
      if(rc.getHealth()>6){//pursue (6 is how much a launcher hit does)
        int index=targetFire(rc,RobotType.LAUNCHER);
        if(index>=0){
          return rc.getLocation().directionTo(enemies[index].getLocation());
        }else{
          index=targetFire(rc,null);
          if(index>=0){
            return rc.getLocation().directionTo(enemies[index].getLocation());
          }
        }
      }
    } else {//clumping logic
      RobotInfo[] friends = rc.senseNearbyRobots(radius, rc.getTeam());
      int myId=rc.getID();
      int lowestId=myId;
      RobotInfo leaderFriend=null; //null means that this robot is the leader
      for(int i=0;i<friends.length;i++){
        if(friends[i].getType()==RobotType.LAUNCHER&&friends[i].getID()<lowestId){
          leaderFriend=friends[i];
          lowestId=friends[i].getID();
        }
      }
      if(leaderFriend!=null){
        return rc.getLocation().directionTo(leaderFriend.getLocation());
      }
    }
    //get out of the way of carriers with anchors
    RobotInfo[] adjacentAllies=rc.senseNearbyRobots(2,rc.getTeam());
    for(int i=0;i<adjacentAllies.length;++i){
      if(adjacentAllies[i].getTotalAnchors()>0){
        return adjacentAllies[i].getLocation().directionTo(rc.getLocation());
      }
    }
    //move towards islands
    int[] islands=rc.senseNearbyIslands();
    if(islands.length>0){
      return pathfind(rc,rc.senseNearbyIslandLocations(islands[rng.nextInt(islands.length)])[0]);
    }
    // Default: move randomly
    if (rc.getLocation().equals(randomLoc)||(rc.canSenseLocation(randomLoc)&&!rc.senseMapInfo(randomLoc).isPassable())) {
      randomLoc=getRandomLoc(rc);
      System.out.println("lol");
    }
    return pathfind(rc,randomLoc);
  }
}
