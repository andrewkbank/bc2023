package First;
import java.util.Random;

import battlecode.common.*;
import java.util.LinkedList;
public class Carrier extends Robot {
  private MapLocation g;

  public Carrier(RobotController rc) throws GameActionException {
    super(rc);
    g=null;
  }

  public void run(RobotController rc) throws GameActionException {
    if (g == null||rc.getLocation()==g) {
      g=getRandomLoc(rc);
    }
    //read and write to shared array
    readSharedArray(rc);
    writeSharedArray(rc);

    Direction[] dirs=pathfindCarrier(rc,g);
    int index=0;
    while(rc.isMovementReady()){
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
    int bytecodeBefore=Clock.getBytecodeNum();
    LinkedList<MapLocation> stack=new LinkedList<MapLocation>();
    LinkedList<Direction> stack2=new LinkedList<Direction>();
    boolean[] discovered=new boolean[rc.getMapHeight()*rc.getMapWidth()];
    Direction[] path={null,null};
    stack.push(goal);
    stack2.push(Direction.CENTER);
    while(!stack.isEmpty()){  //while loop that runs dfs
      rc.setIndicatorDot(stack.peek(),100,0,0);
      MapLocation m=stack.pop();
      path[1]=stack2.pop();
      if(!discovered[m.y*rc.getMapWidth()+m.x]){
        discovered[m.y*rc.getMapWidth()+m.x]=true;
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
        int before=0;
        for(int i=0;i<8;++i){//go through the priority order
          MapLocation toPush=m.add(pushOrder[i]);
          int locVal=toPush.y*rc.getMapWidth()+toPush.x;
          if(rc.onTheMap(toPush)&&map[locVal]!=1&&!discovered[locVal]){
            //inbounds, passable, and not yet discovered by dfs
            if(toPush.equals(rc.getLocation())){ //break if you find the destination
              rc.setIndicatorString("DFS to "+goal+" success, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
              path[0]=pushOrder[i].opposite();
              return path;
            }
            stack.push(m.add(pushOrder[i])); //and add them all to the stack
            stack2.push(pushOrder[i]);
            rc.setIndicatorDot(m.add(pushOrder[i]),0,100,0);
          }
        }
        //rc.setIndicatorString("for bytecode: "+(Clock.getBytecodeNum()-bytecodeBefore));
      }
      //bytecodeBefore=Clock.getBytecodeNum();
    }
    rc.setIndicatorString("DFS to "+goal+" failed, bytecode used: "+(Clock.getBytecodeNum()-bytecodeBefore));
    //backup (in case dfs doesn't work)
    path[1]=rc.getLocation().directionTo(goal);
    path[0]=rc.getLocation().directionTo(goal);
    return path;

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
