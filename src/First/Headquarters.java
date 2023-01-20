package First;

import battlecode.common.*;
import java.util.Random;

import javax.management.monitor.GaugeMonitor;
public class Headquarters extends Robot {

  // Keeps track of how many we have made
  private static int num_Carriers = 0;
  private static int num_Launchers = 0;

  // Keeps track of all data in the array
  private static int[][] islands=new int[1][ISLANDSTORAGELENGTH];
  private static int[][] impassables=new int[1][IMPASSABLESTORAGELENGTH];
  private static int[][] wells=new int[1][WELLSTORAGELENGTH];

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
    //tries to build a unit
    build(rc);
    
    if(first_turn){//on the first turn look at the entire surrounding
      updatePersonalMapFull(rc);
      writeSharedArray(rc);
      first_turn=false;
    }
    //these three methods handle the cycling of data in the shared array
    //they DO NOT write the HQ's personal findings to the array
    arrayToStorage(rc); //takes data from the shared array and stores it in HQ's personal files
    removeDuplicatesFromArray();  //removes update-duplicates from the array
    updateCycles();   //adds new cycles if needed
    storageToArray(rc); //posts the next cycle of data (if any)
    
    printArrayData(rc);
  }

  //builds units
  private void build(RobotController rc) throws GameActionException{
    if(rc.isActionReady()){ //try building
      //anchor building is priority
      if(availableIsland()){
        if(rc.canBuildAnchor(Anchor.ACCELERATING)&&rc.getResourceAmount(ResourceType.ELIXIR)>600){ //build accelerating anchor
          rc.buildAnchor(Anchor.ACCELERATING);
        }
        if(rc.canBuildAnchor(Anchor.STANDARD)&&rc.getResourceAmount(ResourceType.ADAMANTIUM)>200&&rc.getResourceAmount(ResourceType.MANA)>200){
          rc.buildAnchor(Anchor.STANDARD);
        }
      }
      //gets priority queue of locations to build at (its 29 long)
      MapLocation[] actionLocations=getActionLocations(rc,null);
      //for(int i=0;i<actionLocations.length;++i){
        //rc.setIndicatorDot(actionLocations[i],100,0,0);
      //}
      //tries building at each of those locations
      for(int i=0;i<actionLocations.length;++i){
        if (rc.canBuildRobot(RobotType.AMPLIFIER, actionLocations[i])) {
          rc.buildRobot(RobotType.AMPLIFIER, actionLocations[i]);
          break;
        }
        else if (rc.canBuildRobot(RobotType.CARRIER, actionLocations[i]) && (num_Carriers < 5 || num_Carriers < 1.5*num_Launchers) && (num_Carriers < 60)) {
          rc.buildRobot(RobotType.CARRIER, actionLocations[i]);
          num_Carriers++;
          break;
        }else if(rc.canBuildRobot(RobotType.LAUNCHER,actionLocations[i]) && (num_Launchers < 60)){
          rc.buildRobot(RobotType.LAUNCHER, actionLocations[i]);
          num_Launchers++;
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
      //as you can see, diagonal doesn't have a nice pattern, so I had to hard code it
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
      //for cardinal, you can kinda abuse the square
      actionLocations[0]=rc.getLocation().add(prioritize).add(prioritize).add(prioritize);
      actionLocations[1]=actionLocations[0].add(prioritize.opposite());
      for(int i=0;i<5;++i){
        actionLocations[2+i*5]=actionLocations[1+i*5].add(left);
        actionLocations[3+i*5]=actionLocations[1+i*5].add(right);
        actionLocations[4+i*5]=actionLocations[2+i*5].add(left);
        actionLocations[5+i*5]=actionLocations[3+i*5].add(right);
        actionLocations[6+i*5]=actionLocations[1+i*5].add(prioritize.opposite());
      }
      //although the left and rightmost point get left behind
      actionLocations[27]=rc.getLocation().add(left).add(left).add(left);
      actionLocations[28]=rc.getLocation().add(right).add(right).add(right);
    }
    return actionLocations;
  }


  private void arrayToStorage(RobotController rc) throws GameActionException{
    //update islands
    for(int i=0;i<ISLANDSTORAGELENGTH;++i){
      islands[(rc.getRoundNum()-1)%islands.length][i]=rc.readSharedArray(i);
    }
    for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){
      impassables[(rc.getRoundNum()-1)%impassables.length][i]=rc.readSharedArray(i+ISLANDSTORAGELENGTH);
    }
    for(int i=0;i<WELLSTORAGELENGTH;++i){
      wells[(rc.getRoundNum()-1)%wells.length][i]=rc.readSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH);    
    }
  }
  //looks at all storage types and sees if any of them needs a new cycle
  private void updateCycles(){
    if(updateIslandCycles()){
      int[][] temp=new int[islands.length+1][ISLANDSTORAGELENGTH];
      System.arraycopy(islands, 0, temp, 0, islands.length);
      islands=temp;
    }
    if(updateImpassableCycles()){
      int[][] temp=new int[impassables.length+1][IMPASSABLESTORAGELENGTH];
      System.arraycopy(impassables, 0, temp, 0, impassables.length);
      impassables=temp;
    }
    if(updateWellCycles()){
      int[][] temp=new int[wells.length+1][WELLSTORAGELENGTH];
      System.arraycopy(wells,0,temp,0,wells.length);
      wells=temp;
    }
  }
  //returns true if Island storage needs a new cycle
  private boolean updateIslandCycles(){
    for(int i=0;i<ISLANDSTORAGELENGTH;++i){
      if(islands[islands.length-1][i]==0){ //if any slot is empty
        return false;               //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }

  //returns true if Impassable storage needs a new cycle
  private boolean updateImpassableCycles(){
    for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){
      if(impassables[impassables.length-1][i]==0){ //if any slot is empty
        return false;                                   //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }
  //returns true if Well storage needs a new cycle
  private boolean updateWellCycles(){
    for(int i=0;i<WELLSTORAGELENGTH;++i){
      if(wells[wells.length-1][i]==0){ //if any slot is empty
        return false;               //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }

  private void storageToArray(RobotController rc) throws GameActionException{
    if(islands.length>=1){//update islands
      for(int i=0;i<ISLANDSTORAGELENGTH;++i){
        rc.writeSharedArray(i,islands[rc.getRoundNum()%islands.length][i]);
      }
    }
    if(impassables.length>=1){//update impassables
      for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH,impassables[rc.getRoundNum()%impassables.length][i]);
      }
    }
    if(wells.length>=1){//update wells
      for(int i=0;i<WELLSTORAGELENGTH;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH,wells[rc.getRoundNum()%wells.length][i]);
      }
    }
  }

  //when islands and wells get updated, they're added as new entries instead of having previous entries modified
  //the hq will be in charge of removing the old entries
  private void removeDuplicatesFromArray(){
    removeIslandDuplicates();
    removeWellDuplicates();
    //once duplicates are removed, pack down the array
    //in other words, get rid of the empty "bubbles" that result from removing items in the array
    packDownIslandArray();
    packDownWellArray();
  }
  private void removeIslandDuplicates(){
    //we can achieve this by comparing each entry to every other entry
    //luckily, we don't have to do this for impassables (since they can't be updated)
    for(int i=0;i<islands.length;++i){
      for(int j=0;j<ISLANDSTORAGELENGTH;++j){
        if(islands[i][j]!=0){
          for(int k=i;k<islands.length;++k){
            for(int l=0;l<ISLANDSTORAGELENGTH;++l){
              if((i!=k||j!=l)&&islands[k][l]!=0&&(islands[i][j]%4096)==(islands[k][l]%4096)){//duplicate entries
                islands[i][j]=islands[k][l];  //put the new entry in the original slot
                islands[k][l]=0;  //clear the new slot
              }
            }
          }
        }
      }
    }
  }
  private void removeWellDuplicates(){
    //we can achieve this by comparing each entry to every other entry
    //luckily, we don't have to do this for impassables (since they can't be updated)
    for(int i=0;i<wells.length;++i){
      for(int j=0;j<WELLSTORAGELENGTH;++j){
        //System.out.println("Looking at ("+wells[i][j]%4096%20+","+wells[i][j]%4096/20+")");
        if(wells[i][j]!=0){
          for(int k=i;k<wells.length;++k){
            for(int l=0;l<WELLSTORAGELENGTH;++l){
              //System.out.println("comparing with  ("+wells[k][l]%4096%20+","+wells[k][l]%4096/20+")");
              //System.out.println("from ("+k+","+l+")");
              if((i!=k||j!=l)&&wells[k][l]!=0&&(wells[i][j]%4096)==(wells[k][l]%4096)){//duplicate entries
                //System.out.println("removed duplicate: ("+wells[i][j]%4096%20+","+wells[i][j]%4096/20+") ("+wells[k][l]%4096%20+","+wells[k][l]%4096/20+")");
                //System.out.println("from ("+i+","+j+")"+" and ("+k+","+l+")");
                wells[i][j]=wells[k][l];  //put the new entry in the original slot
                wells[k][l]=0;  //clear the new slot
              }
            }
          }
        }
      }
    }
    //System.out.println("------------------------------");
  }
  
  //remove empty "bubbles" from the shared array (empty space should only be at the end of the array)
  private void packDownIslandArray(){
    //note: probably could make more efficient from returning the "bubble" locations from removeIslandDuplicates
    //another note, we run this only after we remove duplicates first,
    //so each items location in the array doesn't matter
    int i=0;
    int j=0;
    int lastI=islands.length-1;
    int lastJ=ISLANDSTORAGELENGTH-1;
    while(i<lastI&&j<lastJ){
      while(islands[i][j]!=0){//increment position in the island array by 1
        ++j;
        if(j>=ISLANDSTORAGELENGTH){
          ++i;
          j=0;
          if(i>=islands.length){//full array, no bubbles
            return;
          }
        }
      }
      while(islands[lastI][lastJ]==0){//decrement position in the island array by 1
        --lastJ;
        if(lastJ<=0){
          --lastI;
          lastJ=ISLANDSTORAGELENGTH-1;
          if(lastI<=0){//empty array, no bubbles
            return;
          }
        }
      }
      //if lastI,lastJ and i,j aren't consecutive, there's a bubble
      if(!((lastI==i&&(lastJ+1)==j)||((lastI+1)==i&&j==0&&lastJ==(ISLANDSTORAGELENGTH-1)))){
        islands[i][j]=islands[lastI][lastJ];
        islands[lastI][lastJ]=0;
      }
    }
  }
  //remove empty "bubbles" from the shared array (empty space should only be at the end of the array)
  private void packDownWellArray(){
    //note: probably could make more efficient from returning the "bubble" locations from removeIslandDuplicates
    //another note, we run this only after we remove duplicates first,
    //so each items location in the array doesn't matter
    int i=0;
    int j=0;
    int lastI=wells.length-1;
    int lastJ=WELLSTORAGELENGTH-1;
    while(i<lastI&&j<lastJ){
      while(wells[i][j]!=0){//increment position in the island array by 1
        ++j;
        if(j>=WELLSTORAGELENGTH){
          ++i;
          j=0;
          if(i>=wells.length){//full array, no bubbles
            return;
          }
        }
      }
      while(wells[lastI][lastJ]==0){//decrement position in the island array by 1
        --lastJ;
        if(lastJ<=0){
          --lastI;
          lastJ=WELLSTORAGELENGTH-1;
          if(lastI<=0){//empty array, no bubbles
            return;
          }
        }
      }
      //if lastI,lastJ and i,j aren't consecutive, there's a bubble
      if(!((lastI==i&&(lastJ+1)==j)||((lastI+1)==i&&j==0&&lastJ==(WELLSTORAGELENGTH-1)))){
        wells[i][j]=wells[lastI][lastJ];
        wells[lastI][lastJ]=0;
      }
    }
  }
  private boolean availableIsland(){
    for(int i=0;i<islands.length;++i){
      for(int j=0;j<ISLANDSTORAGELENGTH;++j){
        if((islands[i][j]/4096)%4==0){
          return true;
        }
      }
    }
    return false;
  }

  private void printArrayData(RobotController rc) throws GameActionException{
    //System.out.println("Islands:");
    for(int i=0;i<islands.length;++i){
      for(int j=0;j<ISLANDSTORAGELENGTH;++j){
        //System.out.println("  Team "+islands[i][j]/4096+" ("+islands[i][j]%4096%rc.getMapWidth()+","+islands[i][j]%4096/rc.getMapWidth()+") ");
        if(islands[i][j]/4096==0){
          rc.setIndicatorDot(new MapLocation(islands[i][j]%4096%rc.getMapWidth(),islands[i][j]%4096/rc.getMapWidth()),255,0,0);
        }else{
          rc.setIndicatorDot(new MapLocation(islands[i][j]%4096%rc.getMapWidth(),islands[i][j]%4096/rc.getMapWidth()),0,0,255);
        }
      }
    }
    //System.out.println("Impassables:");
    for(int i=0;i<impassables.length;++i){
      for(int j=0;j<IMPASSABLESTORAGELENGTH;++j){
        //System.out.println("  ("+impassables[i][j]%4096%rc.getMapWidth()+","+impassables[i][j]%4096/rc.getMapWidth()+") ");
        rc.setIndicatorDot(new MapLocation(impassables[i][j]%4096%rc.getMapWidth(),impassables[i][j]%4096/rc.getMapWidth()),10,10,10);
      }
    }
    //System.out.println("Wells:");
    for(int i=0;i<wells.length;++i){
      for(int j=0;j<WELLSTORAGELENGTH;++j){
        //System.out.println("  Type "+wells[i][j]/4096+" ("+wells[i][j]%4096%rc.getMapWidth()+","+wells[i][j]%4096/rc.getMapWidth()+") ");
        if(wells[i][j]/4096==0){
          rc.setIndicatorDot(new MapLocation(wells[i][j]%4096%rc.getMapWidth(),wells[i][j]%4096/rc.getMapWidth()),100,100,100);
        }else{
          rc.setIndicatorDot(new MapLocation(wells[i][j]%4096%rc.getMapWidth(),wells[i][j]%4096/rc.getMapWidth()),255,0,255);

        }
      }
    }
    //System.out.println("----------");
  }
}