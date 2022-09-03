package com.vergilprime.connectables;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.events.OraxenFurnitureBreakEvent;
import io.th0rgal.oraxen.events.OraxenFurniturePlaceEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.shaded.customblockdata.CustomBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class ConnectableMechanicManager extends FurnitureMechanic implements Listener {
    private final ConnectableMechanicFactory factory;

    public ConnectableMechanicManager(ConnectableMechanicFactory factory, ConfigurationSection section) {
        super(factory,section);
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOraxenFurniturePlaceEvent(final OraxenFurniturePlaceEvent event) {
        ItemFrame itemFrame = event.getItemFrame();
        Connect(itemFrame,1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakConnectedFurniture(OraxenFurnitureBreakEvent event) {
        String OID = event.getFurnitureMechanic().getItemID();
        Disconnect(OID, event.getBlock().getLocation());
    }

    private void Connect(ItemFrame frame, int recursions) {
        // Get the item from the frame
        ItemStack displayItem = frame.getItem();
        // Get the Oraxen ID of this item
        String OID = OraxenItems.getIdByItem(displayItem);
        // If it's null then this isn't an Oraxen item
        if (OID == null) {return;}
        if (factory.isNotImplementedIn(OID))
            return;
        // Get the mechanic for this particular type of item
        ConnectableMechanic mechanic = (ConnectableMechanic) factory.getMechanic(OID);
        // Get an itembuilder
        ItemBuilder itemBuilder = OraxenItems.getItemById(OID);

        DirectionMap directionMap = DirectionMap.getDirectionMap(frame);

        // If the directionmap is null, the item in the frme probably wasn't at a 90 degree angle.
        if (directionMap == null) {return;}

        HashMap<String,ItemFrame> neighborConnectables = getNeighborConnectables(frame.getLocation(),directionMap,OID);

        // FIGURE OUT WHICH CONNECTIONS WORK

        boolean can_connect_left = false;
        boolean can_connect_right = false;
        boolean can_connect_front = false;
        boolean can_connect_back = false;

        DirectionMap front_directionMap;
        DirectionMap right_directionMap;
        DirectionMap back_directionMap;
        DirectionMap left_directionMap;

        // If left frame was found and set in the previous for loop
        if (neighborConnectables.get("left") != null) {
            // get the facing array for the left frame
            left_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("left"));
            // if the left frame is facing a cardinal direction
            if (left_directionMap != null) {
                // if the left frame is facing forward, left or right.
                if (left_directionMap.getBack() != directionMap.getFront()) {
                    can_connect_left = true; // These two frames can connect!
                }
            }
        }

        // Same story but with the right frame
        if (neighborConnectables.get("right") != null) {
            // get the facing array for the right frame
            right_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("right"));
            // if the right frame is facing a cardinal direction
            if (right_directionMap != null) {
                // if the right frame is facing forward, left or right.
                if (right_directionMap.getBack() != directionMap.getFront()) {
                    can_connect_right = true; // These two frames can connect!
                }
            }
        }

        if (neighborConnectables.get("back") != null) {
            back_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("back"));
            if (back_directionMap != null) {
                if (
                        back_directionMap.getRight() == directionMap.getFront() ||
                                back_directionMap.getLeft() == directionMap.getFront()
                ) {
                    can_connect_back = true;
                }
            }
        }

        if (neighborConnectables.get("front") != null) {
            front_directionMap = DirectionMap.getDirectionMap(neighborConnectables.get("front"));
            if (front_directionMap != null) {
                if (front_directionMap.getRight() == directionMap.getFront() ||
                        front_directionMap.getLeft() == directionMap.getFront()) {
                    can_connect_front = true;
                }
            }
        }

        // Enumerator containing the type which will be associated with a new model, defaults to SINGLE
        ConnectableType type = ConnectableType.SINGLE;
        // If the type is a corner, it can be rotated 90 degrees to the right.
        DirectionMap new_directionMap = directionMap;

        // If can connect left and right, this is a middle
        if (can_connect_left) {
            if (can_connect_right){

                //System.out.println("Determined to be a middle.");
                type = ConnectableType.MIDDLE;
                if (recursions > 0) {
                    Connect(neighborConnectables.get("left"), recursions - 1);
                    Connect(neighborConnectables.get("right"), recursions - 1);
                }
            // If can connect left and front, this is an inner corner
            } else if (can_connect_front) {

                //System.out.println("Determined to be an inner corner (front and left).");
                type = ConnectableType.CORNER_IN;
                if (recursions > 0) {
                    Connect(neighborConnectables.get("front"), recursions - 1);
                }

            // If can connect left and back, this is an outer corner
            } else if (can_connect_back) {

                //System.out.println("Determined to be an outer corner (back and left).");
                new_directionMap = DirectionMap.getDirectionMap(directionMap.getRight());
                type = ConnectableType.CORNER_OUT;
                if (recursions > 0) {
                    Connect(neighborConnectables.get("back"), recursions - 1);
                }
            } else {

                //System.out.println("Can only connect on the left, therefore this is a right end.");
                type = ConnectableType.END_RIGHT;
            }
            if (recursions > 0) {
                Connect(neighborConnectables.get("left"), recursions - 1);
            }
        } else if (can_connect_right) {
            if (can_connect_front) {

                //Rotate the directionmap 90 degrees clockwise.
                //System.out.println("Determined to be an inner corner (front and right).");
                new_directionMap = DirectionMap.getDirectionMap(directionMap.getRight());
                type = ConnectableType.CORNER_IN;
                if (recursions > 0) {
                    Connect(neighborConnectables.get("front"), recursions - 1);
                }
                // inner
            } else if (can_connect_back) {

                //System.out.println("Determined to be an outer corner (back and right).");
                type = ConnectableType.CORNER_OUT;
                if (recursions > 0) {
                    Connect(neighborConnectables.get("back"), recursions - 1);
                }
                // outer
            } else {
                //System.out.println("Can only connect on the right, therefore this is a left end.");
                type = ConnectableType.END_LEFT;
            }
            if (recursions > 0) {
                Connect(neighborConnectables.get("right"), recursions - 1);
            }
        }

        if (new_directionMap != directionMap) {

            //System.out.println("Rotating twice clockwise.");
            // I guess this rotates 90 directions according to the docs, which makes no sense but whatever.
            // Edit: Docs are wrong, rotates 45 degrees. I called it.
            frame.setRotation(frame.getRotation().rotateClockwise());
            frame.setRotation(frame.getRotation().rotateClockwise());
        }

        // Get the CustomModelData of the default or "single" item
        int customModelData = itemBuilder.getOraxenMeta().getCustomModelData();

        // If the type is anything other than signal then we can look to the config for the right customModelData
        customModelData = switch (type) {
            case MIDDLE ->      mechanic.getMiddle();
            case CORNER_IN ->   mechanic.getCornerInner();
            case CORNER_OUT ->  mechanic.getCornerOuter();
            case END_LEFT ->    mechanic.getEndLeft();
            case END_RIGHT ->   mechanic.getEndRight();
            default -> itemBuilder.getOraxenMeta().getCustomModelData();
        };

        //System.out.println("Connectable is of type " + type.toString() + ", new model: " + customModelData + ".");

        // Set the CustomModelData of the item in the frame.
        if(type != ConnectableType.SINGLE) {
            ItemStack item = frame.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
            frame.setItem(item);
        }
    }


    private void Disconnect(String OID, Location location) {

        // Get nearby entities
        HashMap<String,ItemFrame> neighborConnectables = getNeighborConnectables(location,DirectionMap.getDirectionMap(BlockFace.NORTH),OID);

        // For every nearby entity
        neighborConnectables.forEach((direction,itemFrame) -> {
            Connect(itemFrame,0);
        });
    }

    private HashMap<String,ItemFrame> getNeighborConnectables(Location location, DirectionMap directionMap, String oid){
        HashMap<String, ItemFrame> itemFrames = new HashMap<>();
        itemFrames.put("front", null);
        itemFrames.put("right", null);
        itemFrames.put("back", null);
        itemFrames.put("left", null);

        // For every direction in the directionMap
        directionMap.getFaces().forEach((direction,blockface) -> {
            // Get the block space off the face of the central block
            Block neighbor = location.getBlock().getRelative(blockface);

            // Get the exact location where the frame would be
            Location frameLocation = neighbor.getLocation();
            frameLocation.add(0.5,0.03125,0.5);

            // get a list of frames at that exact point (should only be one)
            List<ItemFrame> neighborFrames = (List<ItemFrame>) frameLocation.getWorld().getNearbyEntities(frameLocation,0,0,0).stream().filter(new Predicate<Entity>() {
                @Override
                public boolean test(Entity entity) {
                    return  entity instanceof ItemFrame &&
                            !OraxenItems.getIdByItem(((ItemFrame) entity).getItem()).equals(null) &&
                            OraxenItems.getIdByItem(((ItemFrame) entity).getItem()) == oid;
                }
            });

            // If there are no frames in the list, nothing else to do.
            if(neighborFrames.size() < 1){ return; }

            // Matching item found!
            itemFrames.put(direction,neighborFrames.get(0));
        });

        return itemFrames;
    }

    private ConnectableMechanic getConnectableMechanic(Block block) {
        if (block.getType() != Material.BARRIER) return null;
        final PersistentDataContainer customBlockData = new CustomBlockData(block, OraxenPlugin.get());
        if (!customBlockData.has(FURNITURE_KEY, PersistentDataType.STRING)) return null;
        final String mechanicID = customBlockData.get(FURNITURE_KEY, PersistentDataType.STRING);
        return (ConnectableMechanic) factory.getMechanic(mechanicID);
    }
}
