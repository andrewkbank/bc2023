package First;

import battlecode.common.*;
import java.util.Random;

public class Headquarters extends Robot {

  // Keeps track of how many we have made
  private static int num_Carriers = 0;
  private static int num_Launchers = 0;

  // Keeps track of all data in the array
  private static String islands="";
  private static String impassables="";
  private static String wells="";

  // Keeps track of how many spots in the shared array are currently full
  private static int fullIslands=0;
  private static int fullImpassables=0;
  private static int fullWells=0;


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
    int bytecodeBefore=Clock.getBytecodeNum();
    //tries to build a unit
    build(rc);
    int buildBytecode=Clock.getBytecodeNum();
    if(first_turn){//on the first turn look at the entire surrounding
      updatePersonalMapFull(rc);
      writeSharedArray(rc);
      first_turn=false;
    }
    //these three methods handle the cycling of data in the shared array
    //they DO NOT write the HQ's personal findings to the array
    String newItems=arrayToStorage(rc); //takes data from the shared array and stores it in HQ's personal files
    int arrayToStorageBytecode=Clock.getBytecodeNum();
    removeDuplicatesFromArray(newItems);  //removes update-duplicates from the array
    int removeDuplicatesBytecode=Clock.getBytecodeNum();
    storageToArray(rc); //posts the next cycle of data (if any)
    int storageToArrayBytecode=Clock.getBytecodeNum();
    
    //printArrayData(rc);
    rc.setIndicatorString("build: "+(buildBytecode-bytecodeBefore)+" arrStor: "+(arrayToStorageBytecode-buildBytecode)+" dup: "+(removeDuplicatesBytecode-arrayToStorageBytecode)+" storArr: "+(storageToArrayBytecode-removeDuplicatesBytecode)+" print: "+(Clock.getBytecodeNum()-storageToArrayBytecode));
  }

  //builds units
  private void build(RobotController rc) throws GameActionException{
    if(rc.isActionReady()){ //try building
      //anchor building is priority
      //if(availableIsland()){
        if(rc.canBuildAnchor(Anchor.ACCELERATING)&&rc.getResourceAmount(ResourceType.ELIXIR)>600){ //build accelerating anchor
          rc.buildAnchor(Anchor.ACCELERATING);
        }
        if(rc.canBuildAnchor(Anchor.STANDARD)&&rc.getResourceAmount(ResourceType.ADAMANTIUM)>200&&rc.getResourceAmount(ResourceType.MANA)>200){
          rc.buildAnchor(Anchor.STANDARD);
        }
      //}
      //gets priority queue of locations to build at (its 29 long)
      MapLocation[] actionLocations=getActionLocations(rc,null);
      //for(int i=0;i<actionLocations.length;++i){
        //rc.setIndicatorDot(actionLocations[i],100,0,0);
      //}
      //tries building at each of those locations
      if(resourcesToBuildRobot(rc,RobotType.AMPLIFIER)){
        for(int i=0;i<actionLocations.length;++i){
          if (rc.canBuildRobot(RobotType.AMPLIFIER, actionLocations[i])) {
            rc.buildRobot(RobotType.AMPLIFIER, actionLocations[i]);
            return;
          }
        }
      }
      if(resourcesToBuildRobot(rc,RobotType.CARRIER) && (num_Carriers < 5 || num_Carriers < 1.5*num_Launchers) && (num_Carriers < 60)){
        for(int i=0;i<actionLocations.length;++i){
          if (rc.canBuildRobot(RobotType.CARRIER, actionLocations[i])) {
            rc.buildRobot(RobotType.CARRIER, actionLocations[i]);
            num_Carriers++;
            return;
          }
        }
      }
      if(resourcesToBuildRobot(rc,RobotType.LAUNCHER) && (num_Launchers < 60)){
        for(int i=0;i<actionLocations.length;++i){
          if(rc.canBuildRobot(RobotType.LAUNCHER,actionLocations[i])){
            rc.buildRobot(RobotType.LAUNCHER, actionLocations[i]);
            num_Launchers++;
            return;
          }
        }
      }  
    }
  }

  private boolean resourcesToBuildRobot(RobotController rc, RobotType robot) throws GameActionException{
    return ((rc.getResourceAmount(ResourceType.ADAMANTIUM)>=robot.buildCostAdamantium)&&(rc.getResourceAmount(ResourceType.MANA)>=robot.buildCostMana)&&(rc.getResourceAmount(ResourceType.ELIXIR)>=robot.buildCostElixir));
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


  private String arrayToStorage(RobotController rc) throws GameActionException{
    String newItems="";
    int read;
    //update islands
    for(int i=fullIslands;i<ISLANDSTORAGELENGTH;++i){
      read=rc.readSharedArray(i);
      if(read!=0){
        islands+=(char)read;
        newItems+=(char)1;
        newItems+=(char)i;
      }
    }
    for(int i=fullImpassables;i<IMPASSABLESTORAGELENGTH;++i){
      read=rc.readSharedArray(i+ISLANDSTORAGELENGTH);
      if(read!=0){
        impassables+=(char)read;
        newItems+=(char)2;
        newItems+=(char)i;
      }
    }
    for(int i=fullWells;i<WELLSTORAGELENGTH;++i){
      read=rc.readSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH);
      if(read!=0){
        wells+=(char)read;
        newItems+=(char)3;
        newItems+=(char)i;
      }
    }
    rc.setIndicatorString("newItems: "+newItems);
    return newItems;
  }
  private void storageToArray(RobotController rc) throws GameActionException{
    String toWrite;
    int cycle;
    int length;
    if(!islands.isEmpty()){//update islands
      length=islands.length();
      cycle=rc.getRoundNum()%(length/ISLANDSTORAGELENGTH+1);
      if((cycle+1)*ISLANDSTORAGELENGTH<length){
        toWrite=islands.substring(cycle*ISLANDSTORAGELENGTH,(cycle+1)*ISLANDSTORAGELENGTH);
      }else{
        toWrite=islands.substring(cycle*ISLANDSTORAGELENGTH);
      }
      fullIslands=toWrite.length();
      for(int i=0;i<fullIslands;++i){
        rc.writeSharedArray(i,(int)(toWrite.charAt(i)));
      }
      for(int i=fullIslands;i<ISLANDSTORAGELENGTH;++i){
        rc.writeSharedArray(i,0);
      }
    }
    if(!impassables.isEmpty()){//update impassables
      length=impassables.length();
      cycle=rc.getRoundNum()%(length/IMPASSABLESTORAGELENGTH+1);
      if((cycle+1)*IMPASSABLESTORAGELENGTH<length){
        toWrite=impassables.substring(cycle*IMPASSABLESTORAGELENGTH,(cycle+1)*IMPASSABLESTORAGELENGTH);
      }else{
        toWrite=impassables.substring(cycle*IMPASSABLESTORAGELENGTH);
      }
      fullImpassables=toWrite.length();
      for(int i=0;i<fullImpassables;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH,(int)(toWrite.charAt(i)));
      }
      for(int i=fullImpassables;i<IMPASSABLESTORAGELENGTH;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH,0);
      }
    }
    if(!wells.isEmpty()){//update wells
      length=wells.length();
      cycle=rc.getRoundNum()%(length/WELLSTORAGELENGTH+1);
      if((cycle+1)*WELLSTORAGELENGTH<length){
        toWrite=wells.substring(cycle*WELLSTORAGELENGTH,(cycle+1)*WELLSTORAGELENGTH);
      }else{
        toWrite=wells.substring(cycle*WELLSTORAGELENGTH);
      }
      fullWells=toWrite.length();
      for(int i=0;i<fullWells;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH,(int)(toWrite.charAt(i)));
      }
      for(int i=fullWells;i<WELLSTORAGELENGTH;++i){
        rc.writeSharedArray(i+ISLANDSTORAGELENGTH+IMPASSABLESTORAGELENGTH,0);
      }
    }
    //rc.setIndicatorString("fullIslands: "+fullIslands+" fullImpassables: "+fullImpassables+" fullWells: "+fullWells);
  }

  //when islands and wells get updated, they're added as new entries instead of having previous entries modified
  //the hq will be in charge of removing the old entries
  private void removeDuplicatesFromArray(String newItems){
    int type;
    int index;
    int data;
    int type1;
    int type2;
    int type3;
    int islandsRemoved=0;
    int impassablesRemoved=0;
    int wellsRemoved=0;
    for(int i=0;i<newItems.length();i+=2){
      type=(int)(newItems.charAt(i));
      index=(int)(newItems.charAt(i+1));
      if(type==1){//island
        index-=islandsRemoved;
        data=(int)islands.charAt(index);
        type1=data%4096;
        type1=islands.indexOf(type1);
        if(type1!=-1&&type1!=index){
          //remove duplicate
          islands=islands.substring(0,type1)+(char)data+islands.substring(type1+1,index)+islands.substring(index+1);
          islandsRemoved++;
          continue;
        }
        type2=4096+data%4096;
        type2=islands.indexOf(type2);
        if(type2!=-1&&type2!=index){
          //remove duplicate
          islands=islands.substring(0,type2)+(char)data+islands.substring(type2+1,index)+islands.substring(index+1);
          islandsRemoved++;
          continue;
        }
        type3=8192+data%4096;
        type3=islands.indexOf(type3);
        if(type3!=-1&&type3!=index){
          //remove duplicate
          islands=islands.substring(0,type3)+(char)data+islands.substring(type3+1,index)+islands.substring(index+1);
          islandsRemoved++;
          continue;
        }
      }else if(type==2){//impassable
        index-=impassablesRemoved;
        data=(int)impassables.charAt(index);
        if(impassables.indexOf(data)!=index){
          //remove duplicate
          impassables=impassables.substring(0,index)+impassables.substring(index+1);
          impassablesRemoved++;
        }
      }else{//well
        index-=wellsRemoved;
        data=(int)wells.charAt(index);
        type1=data%4096;
        type1=wells.indexOf(type1);
        if(type1!=-1&&type1!=index){
          //remove duplicate
          wells=wells.substring(0,type1)+(char)data+wells.substring(type1+1,index)+wells.substring(index+1);
          wellsRemoved++;
          continue;
        }
        type2=4096+data%4096;
        type2=wells.indexOf(type2);
        if(type2!=-1&&type2!=index){
          //remove duplicate
          wells=wells.substring(0,type2)+(char)data+wells.substring(type2+1,index)+wells.substring(index+1);
          wellsRemoved++;
          continue;
        }
        type3=8192+data%4096;
        type3=wells.indexOf(type3);
        if(type3!=-1&&type3!=index){
          //remove duplicate
          wells=wells.substring(0,type3)+(char)data+wells.substring(type3+1,index)+wells.substring(index+1);
          wellsRemoved++;
          continue;
        }
      }
    }
  }

  private void printArrayData(RobotController rc) throws GameActionException{
    //System.out.println("Islands:");
    rc.setIndicatorDot(rc.getLocation(),255,255,255);
    for(int i=0;i<islands.length();++i){
      //System.out.println("  Team "+islands[i][j]/4096+" ("+islands[i][j]%4096%rc.getMapWidth()+","+islands[i][j]%4096/rc.getMapWidth()+") ");
      if((int)(islands.charAt(i))/4096==0){
        rc.setIndicatorDot(new MapLocation((int)(islands.charAt(i))%4096%rc.getMapWidth(),(int)(islands.charAt(i))%4096/rc.getMapWidth()),255,0,0);
      }else{
        rc.setIndicatorDot(new MapLocation((int)(islands.charAt(i))%4096%rc.getMapWidth(),(int)(islands.charAt(i))%4096/rc.getMapWidth()),0,0,255);
      }
    }
    //System.out.println("Impassables:");
    for(int i=0;i<impassables.length();++i){
      //System.out.println("  ("+impassables[i][j]%4096%rc.getMapWidth()+","+impassables[i][j]%4096/rc.getMapWidth()+") ");
      rc.setIndicatorDot(new MapLocation((int)(impassables.charAt(i))%4096%rc.getMapWidth(),(int)(impassables.charAt(i))%4096/rc.getMapWidth()),10,10,10);
    }
    //System.out.println("Wells:");
    for(int i=0;i<wells.length();++i){
      //System.out.println("  Type "+wells[i][j]/4096+" ("+wells[i][j]%4096%rc.getMapWidth()+","+wells[i][j]%4096/rc.getMapWidth()+") ");
      if((int)(wells.charAt(i))/4096==0){
        rc.setIndicatorDot(new MapLocation((int)(wells.charAt(i))%4096%rc.getMapWidth(),(int)(wells.charAt(i))%4096/rc.getMapWidth()),100,100,100);
      }else{
        rc.setIndicatorDot(new MapLocation((int)(wells.charAt(i))%4096%rc.getMapWidth(),(int)(wells.charAt(i))%4096/rc.getMapWidth()),255,0,255);
      }
    }
    //System.out.println("----------");
  }
}