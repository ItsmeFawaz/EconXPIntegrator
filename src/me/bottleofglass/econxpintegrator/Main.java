package me.bottleofglass.econxpintegrator;

import me.bottleofglass.econxpintegrator.commands.EconExpPlayerLogCommand;
import me.bottleofglass.econxpintegrator.listeners.EventListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static Economy econ = null;
    private static final Logger log = Logger.getLogger("Minecraft");
    public static Set<Player> p = new HashSet<>();
    public static boolean isLogsEnabled;
    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        //first time run to sync XP with Balance
        if(!(getConfig().getBoolean("integrated"))) {
            for(Player p : getServer().getOnlinePlayers()) {
                double balance = econ.getBalance(p);
                p.setLevel(0);
                p.setExp(0);
                p.giveExp((int) balance);
            }
            getConfig().set("integrated", true);
            saveConfig();
        }
        isLogsEnabled = getConfig().getBoolean("console-logs");
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getCommand("econlogs").setExecutor(new EconExpPlayerLogCommand(this));

    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public static Economy getEconomy() {
        return econ;
    }
    public static Logger getLog() {
        return log;
    }
}
