package First;
import java.util.Random;

import battlecode.common.*;
import java.util.LinkedList;
public class Carrier extends Robot {
  //these two booleans determine the role of the carrier
  private boolean resourceCollecting=true;
  private boolean elixirMaker=false;
  private MapLocation g=null;
  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
  }

  public void run(RobotController rc) throws GameActionException {
    while (g == null||rc.getLocation().equals(g)) {
      g=getRandomLoc(rc);
    }
    //read and write to shared array
    readSharedArray(rc);
    writeSharedArray(rc);

    if(resourceCollecting){
      if(elixirMaker){
        //call elixir making method
      }else{
        //call resource collecting method

      }
    }else{
      //call anchor delivering method
    }


    Direction[] dirs=pathfindCarrier(rc,g);
    int index=0;
    while(rc.isMovementReady()&&!rc.getLocation().equals(g)){
      if(dirs[index]==Direction.CENTER){ break;}
      if(rc.canMove(dirs[index])){
        rc.move(dirs[index]);
        index=1;
      }else{ break;}
    }
    //System.out.println("we made it: "+Clock.getBytecodeNum());
    updatePersonalMap(rc);
  }

  //since carrier can move 2 spaces at a time, it needs its own version of pathfind that returns 2 directions
  public Direction[] pathfindCarrier(RobotController rc, MapLocation goal) throws GameActionException{
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
              rc.setIndicatorString("DFS to "+goal+" success, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
              //Direction[] toReturn={pushOrder[i].opposite(),directions[(int)path]};
              Direction[] toReturn={pushOrder[i].opposite(),pushOrder[i].opposite()};
              return toReturn;
            }
            stack=(char)toPush+stack; //10 bytecode
            //old: stack2+=directionToChar(pushOrder[i]);  //24 bytecode for the pushes
          }
        }
        //rc.setIndicatorString("for bytecode: "+(Clock.getBytecodeNum()-bytecodeBefore));
      }
      //bytecodeBefore=Clock.getBytecodeNum();
    }
    rc.setIndicatorString("DFS to "+goal+" failed, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
    //backup (in case dfs doesn't work)
    Direction[] toReturn={rc.getLocation().directionTo(goal),rc.getLocation().directionTo(goal)};
    return toReturn;

  }

/*
  private void getNearestIsland(RobotController rc) throws GameActionException {
    int closestDist = Integer.MAX_VALUE;
    for (int i = 0; i < ISLANDSTORAGELENGTH; i++) {
      int sharedArrayValue = rc.readSharedArray(i);
      if (sharedArrayValue != 0&&sharedArrayValue<4096) {
//        System.out.println("shared array value: " + sharedArrayValue);
        // checks if it is a neutral island (not our team and not other team)
        int locationValue = sharedArrayValue % 4096;
//        System.out.println("location value: " + locationValue);

        int x = locationValue % rc.getMapWidth();
//        System.out.println("x: " + x);
        int y = locationValue / rc.getMapWidth();
//        System.out.println("y: " + y);

        MapLocation neutralIsland = new MapLocation(x, y);
        int dist = rc.getLocation().distanceSquaredTo(neutralIsland);
        if (dist < closestDist) {
          closestDist = dist;
          this.nearestNeutralIsland = neutralIsland;
          rc.setIndicatorString("arrVal: "+sharedArrayValue+" pathfinding to x: " + nearestNeutralIsland.x + " y: " + nearestNeutralIsland.y);
        }
      }
    }
  }
  */
}
