package me.bottleofglass.econxpintegrator.listeners;

import com.Zrips.CMI.events.CMIAnvilItemRenameEvent;
import com.Zrips.CMI.events.CMIAnvilItemRepairEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import me.bottleofglass.econxpintegrator.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EventListener implements Listener {
    Logger log = Main.getLog();
    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    List<Player> list = new ArrayList<>();
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBalanceChange(CMIUserBalanceChangeEvent evt) {
        Player p = evt.getUser().getPlayer();
        if(list.contains(p)) {
            list.remove(p);
            log.info("BalanceChangeEvent Ignored");
        } else {
            p.setLevel(0);
            p.setExp(0);
            p.setTotalExperience(0);
            p.giveExp((int) evt.getTo());
        }
    }
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent evt) {

        Player p = evt.getPlayer();

        setMoney(p, (getPlayerExperience(p) + evt.getAmount()));
        //logs
        log.info(p.getName() + "'s balance changed to " + (getPlayerExperience(p) + evt.getAmount()) + "from " + getPlayerExperience(p) + "due to xp change(NATURAL)");
        p.sendMessage(p.getName() + "'s balance changed to " + (getPlayerExperience(p) + evt.getAmount()) + "from " + getPlayerExperience(p) + "due to xp change(NATURAL)");
    }
    @EventHandler
    public void onEnchant(EnchantItemEvent evt) {

        Player p = evt.getEnchanter();

        int newlevel = (p.getLevel() - (evt.whichButton()+1));
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));

        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience +" from "+ getPlayerExperience(p) + "due to xp change(ENCHANT) by pressing" + evt.whichButton());

        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience +" from "+ getPlayerExperience(p) + "due to xp change(ENCHANT)");
    }



    @EventHandler
    public void onAnvilRepair(CMIAnvilItemRepairEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getRepairCost();
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + getPlayerExperience(p)+  "due to xp change(REPAIR)");
        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience+ " from "+ getPlayerExperience(p) + "due to xp change(REPAIR)");
    }
    @EventHandler
    public void onAnvilRename(CMIAnvilItemRenameEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getCost();
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + getPlayerExperience(p)+  "due to xp change(REPAIR)");
        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience+ " from "+ getPlayerExperience(p) + "due to xp change(REPAIR)");
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent evt) {
        Player p = evt.getEntity();
        if(!(p.hasPermission("cmi.keepex"))) {
            setMoney(p, 0);
        }
    }
    //sets player money in a method
    public boolean setMoney(Player p , double value) {
        double balance = Main.getEconomy().getBalance(p);
        EconomyResponse er;
        if (value > balance) {
            er = Main.getEconomy().depositPlayer(p, (value - balance));
            if(!(list.contains(p))) list.add(p);
        } else if (value < balance) {
             er = Main.getEconomy().withdrawPlayer(p, (balance - value));
            if(!(list.contains(p))) list.add(p);
        } else {
            return true;
        }
        return er.transactionSuccess();
    }
    // total xp calculation based by lvl
    public static int levelToExp(int level) {

        if (level <= 15) {
            return (int) (level * level + 6 * level);
        } else if (level <= 30) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    // xp calculation for one current lvl
    public static int deltaLevelToExp(int level) {
        if (version.contains("1_7")) {
            if (level <= 15) {
                return 17;
            } else if (level <= 30) {
                return 3 * level - 31;
            } else {
                return 7 * level - 155;
            }
        } else {
            if (level <= 15) {
                return 2 * level + 7;
            } else if (level <= 30) {
                return 5 * level - 38;
            } else {
                return 9 * level - 158;
            }
        }
    }
    // xp calculation for one current lvl
    public static int currentlevelxpdelta(Player player) {
        int levelxp = deltaLevelToExp(player.getLevel()) - ((levelToExp(player.getLevel()) + (int) (deltaLevelToExp(player.getLevel()) * player.getExp())) - levelToExp(player.getLevel()));
        return levelxp;
    }
    public static int getPlayerExperience(Player player) {
        int bukkitExp = (levelToExp(player.getLevel()) + (int) (deltaLevelToExp(player.getLevel()) * player.getExp()));
        return bukkitExp;
    }





}