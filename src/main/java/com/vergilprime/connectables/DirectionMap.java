package com.vergilprime.connectables;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;

import java.util.HashMap;

public class DirectionMap {
    private HashMap<String,BlockFace> faces;

    public DirectionMap(String front, String right, String back, String left) {
        this.faces.put(front, BlockFace.valueOf(front));
        this.faces.put(right, BlockFace.valueOf(right));
        this.faces.put(back, BlockFace.valueOf(back));
        this.faces.put(left, BlockFace.valueOf(left));
    }

    public static DirectionMap getDirectionMap(ItemFrame frame){
        switch(frame.getRotation()){
            case FLIPPED -> { return new DirectionMap("NORTH","EAST","SOUTH","WEST");}
            case COUNTER_CLOCKWISE -> { return new DirectionMap("EAST", "SOUTH", "WEST", "NORTH");}
            case NONE -> { return new DirectionMap("SOUTH", "WEST", "NORTH", "EAST");}
            case CLOCKWISE -> { return new DirectionMap("WEST", "NORTH", "EAST", "SOUTH");}
            default -> {return null;}
        }
    }

    public static DirectionMap getDirectionMap(BlockFace front) {
        switch(front){
            case NORTH -> { return new DirectionMap("NORTH","EAST","SOUTH","WEST");}
            case EAST -> { return new DirectionMap("EAST", "SOUTH", "WEST", "NORTH");}
            case SOUTH -> { return new DirectionMap("SOUTH", "WEST", "NORTH", "EAST");}
            case WEST -> { return new DirectionMap("WEST", "NORTH", "EAST", "SOUTH");}
            default -> {return null;}
        }
    }

    public HashMap<String,BlockFace> getFaces(){
        return faces;
    }

    public BlockFace getFront(){
        return faces.get("front");
    }

    public BlockFace getRight(){
        return faces.get("right");
    }

    public BlockFace getBack(){
        return faces.get("back");
    }

    public BlockFace getLeft(){
        return faces.get("left");
    }
}
