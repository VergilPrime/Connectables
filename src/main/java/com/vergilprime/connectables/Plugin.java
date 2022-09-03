package com.vergilprime.connectables;

import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Plugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            MechanicsManager.registerMechanicFactory("connectable", ConnectableMechanicFactory::new);
            getLogger().log(Level.INFO, "Connectables enabled Dennis.");
        }catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while enabling:", e);
            getLogger().severe("Failed to register ConnectableMechanicFactory, disabling...");
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
