package First;

import java.util.Random;
import battlecode.common.*;

public class Launcher extends Robot {
  static final Random rng = new Random(69);
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
  public Launcher(RobotController rc) throws GameActionException{
    super(rc);
  }
  public void run(RobotController rc) throws GameActionException {
    attack(rc);
    movement(rc);
    //todo: later, make it able to run-n-gun (not just gun-n-run)
  }
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
    }else{//kill any other unit
      enemyIndex=targetFire(rc,null);
      MapLocation toAttack = enemies[enemyIndex].getLocation();
      if (rc.canAttack(toAttack)) {
          rc.setIndicatorString("Pew");        
          rc.attack(toAttack);
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
        if(type==null){//no specified type to target
          if(enemies[i].getHealth()<enemyHealth){
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
    }
    // Default: move randomly
    return directions[rng.nextInt(directions.length)];
  }
}
