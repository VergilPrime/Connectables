package com.vergilprime.connectables;

import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class ConnectableMechanic extends Mechanic {
    private int middle;
    private int cornerInner;
    private int cornerOuter;
    private int endLeft;
    private int endRight;

    public ConnectableMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {

        // super(mechanicFactory, section, item -> item.setCustomTag(CONNECTABLE_Middle_KEY,
        //         PersistentDataType.INTEGER, section.getInt("Middle")));
        super(mechanicFactory,section);
        //TODO: Default to single if not configured.
        this.middle = section.getInt("Middle") != 0 ? section.getInt("Middle") : 0;
        this.cornerInner = section.getInt("InnerCorner") != 0 ? section.getInt("InnerCorner") : 0;
        this.cornerOuter = section.getInt("OuterCorner") != 0 ? section.getInt("OuterCorner") : 0;
        this.endLeft = section.getInt("LeftEnd") != 0 ? section.getInt("LeftEnd") : 0;
        this.endRight = section.getInt("RightEnd") != 0 ? section.getInt("RightEnd") : 0;
        Bukkit.broadcastMessage("Registered a connectable mechanic!");

    }

    public int getMiddle(){return middle;}
    public int getCornerInner(){return cornerInner;}
    public int getCornerOuter(){return cornerOuter;}
    public int getEndLeft(){return endLeft;}
    public int getEndRight(){return endRight;}
}
