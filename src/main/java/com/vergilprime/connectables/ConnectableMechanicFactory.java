package com.vergilprime.connectables;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class ConnectableMechanicFactory extends BlockMechanicFactory {

    public static ConnectableMechanicFactory instance;
    public ConnectableMechanicFactory(ConfigurationSection configSection){
        super(configSection);
        MechanicsManager.registerListeners(OraxenPlugin.get(), new ConnectableMechanicManager(this));
        instance = this;
    }

    @Override
    public Mechanic parse(ConfigurationSection configSection){
        Mechanic mechanic = new ConnectableMechanic(this, configSection);
        addToImplemented(mechanic);
        return mechanic;
    }
}
