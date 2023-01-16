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
    //todo: anchors
    if(first_turn){//on the first turn look at the entire surrounding
      updatePersonalMapFull(rc);
      writeSharedArray(rc);
      first_turn=false;
    }

    //these three methods handle the cycling of data in the shared array
    //they DO NOT write the HQ's personal findings to the array
    arrayToStorage(rc); //takes data from the shared array and stores it in HQ's personal files
    removeDuplicatesFromArray();
    updateCycles(rc);   //adds new cycles if needed
    storageToArray(rc); //posts the next cycle of data (if any)

    //printArrayData(rc);

    //tries to build a unit
    build(rc);
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
      for(int i=0;i<actionLocations.length;++i){
        rc.setIndicatorDot(actionLocations[i],100,0,0);
      }
      //tries building at each of those locations
      for(int i=0;i<actionLocations.length;++i){
        if (rc.canBuildRobot(RobotType.DESTABILIZER, actionLocations[i])) {
          rc.buildRobot(RobotType.DESTABILIZER, actionLocations[i]);
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
  private void updateCycles(RobotController rc) throws GameActionException{
    if(updateIslandCycles(rc)){
      addEmptyCycle(islands,rc.getRoundNum());
    }
    if(updateImpassableCycles(rc)){
      addEmptyCycle(impassables,rc.getRoundNum());
    }
    if(updateWellCycles(rc)){
      addEmptyCycle(wells,rc.getRoundNum());
    }
  }
  //returns true if Island storage needs a new cycle
  private boolean updateIslandCycles(RobotController rc) throws GameActionException{
    for(int i=0;i<ISLANDSTORAGELENGTH;++i){
      if(rc.readSharedArray(i)==0){ //if any slot is empty
        return false;               //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }

  //returns true if Impassable storage needs a new cycle
  private boolean updateImpassableCycles(RobotController rc) throws GameActionException{
    for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){
      if(rc.readSharedArray(i+ISLANDSTORAGELENGTH)==0){ //if any slot is empty
        return false;                                   //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }
  //returns true if Well storage needs a new cycle
  private boolean updateWellCycles(RobotController rc) throws GameActionException{
    for(int i=0;i<WELLSTORAGELENGTH;++i){
      if(rc.readSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH)==0){ //if any slot is empty
        return false;               //no new cycle is needed
      }
    }
    return true;  //a new cycle is needed (no empty slots)
  }

  //adds an empty cycle to one of the storages
  //hopefully this doesn't take a billion bytecode
  private void addEmptyCycle(int[][] storage,int roundNum){ //note: cuz this is java, this passes by reference
    int[][] temp=new int[storage.length+1][storage[0].length];
    int storageIndex=0;
    //makes sure to display the empty cycle on this turn
    for(int i=0;i<storage.length;++i){
      if(roundNum%(storage.length+1)==i){//
        //add in the empty cycle
        temp[i]=new int[storage[0].length];
      }else{
        //copies over the previous values
        temp[i]=storage[storageIndex];
        storageIndex++;
      }
    }
    storage=temp;
  }

  private void storageToArray(RobotController rc) throws GameActionException{
    if(islands.length>1){//update islands
      for(int i=0;i<ISLANDSTORAGELENGTH;++i){
        rc.writeSharedArray(i,islands[rc.getRoundNum()%islands.length][i]);
      }
    }
    if(impassables.length>1){//update impassables
      for(int i=0;i<IMPASSABLESTORAGELENGTH;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH,impassables[rc.getRoundNum()%impassables.length][i]);
      }
    }
    if(wells.length>1){//update wells
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
          int k=i;
          int l=j+1;
          if(j==ISLANDSTORAGELENGTH-1){
            if(i==islands.length-1){//last element (nothing to compare it to)
              return;
            }
            k=i+1;
            l=0;
          }
          for(;k<islands.length;++k){
            for(;l<ISLANDSTORAGELENGTH;++l){
              if(islands[k][l]!=0&&(islands[i][j]%4096)==(islands[k][l]%4096)){//duplicate entries
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
        if(wells[i][j]!=0){
          int k=i;
          int l=j+1;
          if(j==WELLSTORAGELENGTH-1){
            if(i==wells.length-1){//last element (nothing to compare it to)
              return;
            }
            k=i+1;
            l=0;
          }
          for(;k<wells.length;++k){
            for(;l<WELLSTORAGELENGTH;++l){
              if(wells[k][l]!=0&&(wells[i][j]%4096)==(wells[k][l]%4096)){//duplicate entries
                wells[i][j]=wells[k][l];  //put the new entry in the original slot
                wells[k][l]=0;  //clear the new slot
              }
            }
          }
        }
      }
    }
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
    System.out.println("Islands:");
    for(int i=0;i<islands.length;++i){
      for(int j=0;j<ISLANDSTORAGELENGTH;++j){
        System.out.println("  Team "+islands[i][j]/4096+" ("+islands[i][j]%4096%rc.getMapWidth()+","+islands[i][j]%4096/rc.getMapWidth()+") ");
      }
    }
    System.out.println("Impassables:");
    for(int i=0;i<impassables.length;++i){
      for(int j=0;j<IMPASSABLESTORAGELENGTH;++j){
        System.out.println("  ("+impassables[i][j]%4096%rc.getMapWidth()+","+impassables[i][j]%4096/rc.getMapWidth()+") ");
      }
    }
    System.out.println("Wells:");
    for(int i=0;i<wells.length;++i){
      for(int j=0;j<WELLSTORAGELENGTH;++j){
        System.out.println("  Type "+wells[i][j]/4096+" ("+wells[i][j]%4096%rc.getMapWidth()+","+wells[i][j]%4096/rc.getMapWidth()+") ");
      }
    }
  }
}